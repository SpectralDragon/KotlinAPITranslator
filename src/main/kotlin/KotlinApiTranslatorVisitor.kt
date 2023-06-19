import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import swift.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readBytes

internal class KotlinApiTranslatorVisitor(
    private val outputDirectory: Path,
) : IrElementVisitorVoid {

    override fun visitModuleFragment(declaration: IrModuleFragment) {
        // Start here. Feel free to change the method's body any way you like.
        declaration.files.forEach { irFile ->
            val context = SwiftRenderContext()

            val visitorContext = SwiftVisitorContext(irFile)
            val element = irFile.accept(IrToSwiftFileVisitor(), visitorContext)
            val visitor = SwiftRenderVisitor(context)
            element.acceptChildren(visitor)

            val content = context.toString()
            val fileName = irFile.name.substringBefore('.')
            val swiftFile = File(outputDirectory.toFile(), "$fileName.swift")
            swiftFile.writeText(content)
            swiftFile.createNewFile()
        }
    }
}

open class IrToSwiftVisitor<E: SwiftElement>(): IrElementVisitor<E, SwiftVisitorContext> {
    override fun visitElement(element: IrElement, data: SwiftVisitorContext): E {
        TODO("Not implemented")
    }

}

class IrToSwiftFileVisitor(): IrToSwiftVisitor<SwiftFile>() {
    override fun visitFile(declaration: IrFile, data: SwiftVisitorContext): SwiftFile {
        val declarations = declaration.declarations.map {
            it.accept(IrDeclarationToSwiftVisitor(), data)
        }

        return SwiftFile(declarations)
    }
}

class IrDeclarationToSwiftVisitor(): IrToSwiftVisitor<SwiftExpression>() {
    override fun visitClass(declaration: IrClass, data: SwiftVisitorContext): SwiftExpression {
        return declaration.toSwiftClass(data)
    }

    override fun visitFunction(declaration: IrFunction, data: SwiftVisitorContext): SwiftExpression {
        return declaration.accept(IrFunctionToSwiftVisitor(), data.copy(currentFunction = declaration))
    }

    override fun visitConstructor(
        declaration: IrConstructor,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return declaration.accept(IrElementToSwiftExpressionVisitor(), data.copy(currentFunction = declaration))
    }

    override fun visitField(declaration: IrField, data: SwiftVisitorContext): SwiftExpression {
        return declaration.accept(IrElementToSwiftExpressionVisitor(), data)
    }

    override fun visitProperty(
        declaration: IrProperty,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return declaration.accept(IrElementToSwiftExpressionVisitor(), data)
    }
}

class IrElementToSwiftExpressionVisitor(): IrToSwiftVisitor<SwiftExpression>() {
    override fun visitElement(element: IrElement, data: SwiftVisitorContext): SwiftExpression {
        return SwiftExpression()
    }

    override fun visitConst(expression: IrConst<*>, data: SwiftVisitorContext): SwiftExpression {
        val value = when (val kind = expression.kind) {
            IrConstKind.Null -> SwiftNullLiteral()
            IrConstKind.String -> SwiftStringLiteral(kind.valueOf(expression).toString())
            IrConstKind.Boolean -> SwiftBooleanLiteral(kind.valueOf(expression) as Boolean)
            IrConstKind.Double -> SwiftDoubleLiteral(kind.valueOf(expression) as Double)
            IrConstKind.Float -> SwiftFloatLiteral(kind.valueOf(expression) as Float)
            IrConstKind.Int -> SwiftIntegerLiteral(kind.valueOf(expression) as Int)
            else -> {
                SwiftStringLiteral(kind.valueOf(expression).toString())
            }
        }

        return SwiftConst(value, SwiftType(expression.type))
    }

    override fun visitGetValue(expression: IrGetValue, data: SwiftVisitorContext): SwiftExpression {
        return SwiftGetValue(expression.symbol.owner.name.toString(), SwiftType(expression.type))
    }

    override fun visitCall(expression: IrCall, data: SwiftVisitorContext): SwiftExpression {
        val function = expression.symbol.owner
        val swiftFunction = function.accept(IrFunctionToSwiftVisitor(), data)
        val values = expression.valueArguments.map {
            it?.accept(this, data)
        }
        val receiver = expression.extensionReceiver?.accept(this, data)
        return SwiftFunctionCall(swiftFunction, values, receiver)
    }

    override fun visitConstructorCall(
        expression: IrConstructorCall,
        data: SwiftVisitorContext
    ): SwiftExpression {
        val function = expression.symbol.owner
        val swiftFunction = function.accept(IrFunctionToSwiftVisitor(), data)
        val values = expression.valueArguments.map {
            it?.accept(this, data)
        }

        expression.symbol.owner.parentAsClass

        val receiver = expression.extensionReceiver?.accept(this, data)
        return SwiftFunctionCall(swiftFunction, values, receiver)
    }

    override fun visitStringConcatenation(
        expression: IrStringConcatenation,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return SwiftExpression()
    }

    override fun visitProperty(declaration: IrProperty, data: SwiftVisitorContext): SwiftExpression {
        return declaration.backingField?.let { field ->
            val getter = declaration.getter?.let {
                it.accept(IrFunctionToSwiftVisitor(), data)
            }
            val setter = declaration.setter?.let {
                it.accept(IrFunctionToSwiftVisitor(), data)
            }

            SwiftProperty(
                name = declaration.name.toString(),
                type = SwiftType(field.type),
                isStatic = field.isStatic,
                isPublicAPI = declaration.visibility.isPublicAPI,
                isVar = declaration.isVar,
                isConst = declaration.isConst,
                getter = getter,
                setter = setter
            )
        } ?: SwiftExpression()
    }
}

class IrFunctionToSwiftVisitor(): IrToSwiftVisitor<SwiftFunction>() {
    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: SwiftVisitorContext): SwiftFunction {
        val parameters = declaration.valueParameters.map {
            SwiftFunctionParameter(it.name.toString(), SwiftType(it.type), null)
        }

        val block = declaration.body?.accept(IrElementToSwiftStatementVisitor(), data) as? SwiftBlock
        return SwiftFunction(declaration.name.toString(), parameters, block, SwiftType(declaration.returnType))
    }

    override fun visitConstructor(declaration: IrConstructor, data: SwiftVisitorContext): SwiftFunction {
        val parameters = declaration.valueParameters.map {
            SwiftFunctionParameter(it.name.toString(), SwiftType(it.type), null)
        }

        val block = declaration.body?.accept(IrElementToSwiftStatementVisitor(), data) as? SwiftBlock

        // TODO: Smells like hack...
        val swiftClass = if (data.currentClass?.irClass == declaration.symbol.owner.parentAsClass) {
            data.currentClass
        } else {
            declaration.symbol.owner.parentAsClass.accept(IrDeclarationToSwiftVisitor(), data) as? SwiftClass
        }

        return SwiftConstructor(parameters, block, SwiftType(declaration.returnType), requireNotNull(swiftClass))
    }
}

class IrElementToSwiftStatementVisitor(): IrToSwiftVisitor<SwiftStatement>() {
    override fun visitReturn(expression: IrReturn, data: SwiftVisitorContext): SwiftStatement {
        return SwiftReturn(expression.value.accept(IrElementToSwiftExpressionVisitor(), data), SwiftType(expression.type))
    }

    override fun visitBlockBody(body: IrBlockBody, data: SwiftVisitorContext): SwiftStatement {
        val expressions = body.statements.map {
            it.accept(this, data)
        }
        return SwiftBlock(expressions)
    }

    override fun visitBlock(expression: IrBlock, data: SwiftVisitorContext): SwiftStatement {
        val expressions = expression.statements.map {
            it.accept(this, data)
        }
        return SwiftBlock(expressions)
    }

    override fun visitElement(element: IrElement, data: SwiftVisitorContext): SwiftStatement {
        // Currently we don't support any side effects, we should return something to avoid crash
        return SwiftStatement()
    }

}

fun IrClass.toSwiftClass(context: SwiftVisitorContext): SwiftClass {
    val declorations: MutableList<SwiftExpression> = mutableListOf()
    val methods: MutableList<SwiftFunction> = mutableListOf()

    val clazz = SwiftClass(this.name.toString(), methods = methods, declorations = declorations, modality = modality, irClass = this)
    val newContext = context.copy(currentClass = clazz)

    declarations.forEach {
        when (it) {
            is IrConstructor -> {
                methods.add(it.accept(IrFunctionToSwiftVisitor(), newContext))
            }
            is IrSimpleFunction -> {
                methods.add(it.accept(IrFunctionToSwiftVisitor(), newContext))
            }
            else -> {
                declorations.add(it.accept(IrDeclarationToSwiftVisitor(), newContext))
            }
        }
    }

    return clazz
}

// TODO: (Vlad) Remove test code
fun Path.printFileContent() {
    println("-> $name -> content:\n***\n${readBytes().toString(Charsets.UTF_8)}\n***")
}

data class SwiftVisitorContext(
    var currentFile: IrFile,
    var currentClass: SwiftClass? = null,
    var currentFunction: IrFunction? = null
)