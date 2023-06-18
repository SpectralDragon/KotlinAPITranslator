import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCallableReference
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUInt
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isStatic
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
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
            irFile.accept(IrToSwiftVisitor(context), null)
            val content = context.toString()

            val fileName = irFile.name.substringBefore('.')
            val swiftFile = File(outputDirectory.toFile(), "$fileName.swift")
            swiftFile.writeText(content)
            swiftFile.createNewFile()
        }
    }
}

// TODO: (Vlad) Remove test code
fun Path.printFileContent() {
    println("-> $name -> content:\n***\n${readBytes().toString(Charsets.UTF_8)}\n***")
}

class IrToSwiftVisitor(
    private val context: SwiftRenderContext
): IrElementVisitorVoid {

    override fun visitFile(declaration: IrFile) {
        declaration.declarations.forEach {
            it.accept(this, null)
        }
    }

    override fun visitFunction(declaration: IrFunction) {

        // TODO: (Vlad) Maybe should be a constant
        if (declaration.isInline)
            context.put("@inline(__always)")

        context.put(if (declaration.visibility.isPublicAPI) SwiftKeywords.PUBLIC else SwiftKeywords.PRIVATE)
        context.putWhitespace()

        if (!declaration.isConstructor()) {
            if (declaration.isStatic) {
                context.put(SwiftKeywords.STATIC)
                context.putWhitespace()
            }

            context.put(SwiftKeywords.FUNC)
            context.putWhitespace()
            context.put(declaration.name.toString())
        } else {
            context.put(SwiftKeywords.CONSTRUCTOR)
        }

        context.put("(")

        declaration.valueParameters.forEachIndexed { index, value ->
            context.put(value.name.toString())
            context.put(": ")
            context.put(SwiftIrType(value.type).getSwiftType())

            if (index < declaration.valueParameters.count() - 1) {
                context.put(", ")
            }
        }

        context.put(')')
        context.putWhitespace()

        if (declaration.isSuspend) {
            context.put(SwiftKeywords.ASYNC)
            context.putWhitespace()
        }

        if (!declaration.returnType.isUnit() && !declaration.isConstructor()) {
            context.put("-> ")
            context.put(SwiftIrType(declaration.returnType).getSwiftType())
            context.putWhitespace()
        }

        context.put('{')
        declaration.body?.let {
            context.pushIndent()
            context.newLine()
            it.acceptChildren(this, null)
            context.popIndent()
            context.newLine()
        }

        context.put('}')
        context.newLine()
    }

    override fun visitBlockBody(body: IrBlockBody) {
        body.acceptChildren(this, null)
    }

    override fun visitReturn(expression: IrReturn) {
        context.put(SwiftKeywords.RETURN)

        if (expression.type.isUnit())
            return

        context.put(" ")

        expression.value.accept(this, null)
    }

    override fun visitConst(expression: org.jetbrains.kotlin.ir.expressions.IrConst<*>) {
        when (val kind = expression.kind) {
            IrConstKind.Null -> context.put(SwiftKeywords.NIL)
            IrConstKind.String -> {
                context.put('"')
                context.put(kind.valueOf(expression).toString())
                context.put('"')
            }
            else -> {
                context.put(kind.valueOf(expression).toString())
            }
        }
    }

    override fun visitCall(expression: IrCall) {
        if (expression.isSuspend) {
            context.put(SwiftKeywords.AWAIT)
            context.putWhitespace()
        }

        expression.symbol.owner.parent.let {
            context.put(it.kotlinFqName.toString())
            context.put('.')
        }

        context.put(expression.symbol.owner.name.toString())
        context.put('(')

        val size = expression.valueArgumentsCount

        val arguments = (0 until size).map { index ->
            expression.getValueArgument(index)
        }

        arguments.forEach {
            it?.accept(this, null)
        }

        context.put(')')
    }

    override fun visitGetValue(expression: IrGetValue) {
        println(expression)
    }

//    override fun visitStringConcatenation(expression: IrStringConcatenation) {
//        expression.arguments.forEach { irExpression ->
//            irExpression.accept(this, null)
//        }
//    }

    // CLASS

    override fun visitClass(declaration: IrClass) {
        if (declaration.modality != Modality.FINAL)
            return

        context.put("${SwiftKeywords.CLASS} ${declaration.fqNameWhenAvailable.toString()} ")
        context.put('{')
        context.pushIndent()
        context.newLine()

        declaration.acceptChildren(this, null)

        context.popIndent()
        context.put('}')
    }

//    override fun visitValueParameter(declaration: IrValueParameter) {
//        context.put("val")
//        context.put(declaration.name.toString())
//        context.put(": ${SwiftIrType(declaration.type).getSwiftType()}")
//        context.newLine()
//    }

    override fun visitVariable(declaration: IrVariable) {
        println(declaration)
    }

    override fun visitBlock(expression: IrBlock) {
        expression.acceptChildren(this, null)
    }

    override fun visitCallableReference(expression: IrCallableReference<*>) {
        println(expression)
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression) {
        println(expression)
    }

    override fun visitProperty(declaration: IrProperty) {
        declaration.backingField?.let { field ->
            context.put(if (declaration.visibility.isPublicAPI) SwiftKeywords.PUBLIC else SwiftKeywords.PRIVATE)
            context.putWhitespace()

            if (field.isStatic) {
                context.put(SwiftKeywords.STATIC)
                context.putWhitespace()
            }

            context.put(if (field.isFinal) SwiftKeywords.CONST else SwiftKeywords.VAR)
            context.putWhitespace()
            context.put(declaration.name.toString())
            context.put(": ${SwiftIrType(field.type).getSwiftType()}")
            context.newLine()
        }
    }
}

class SwiftIrType(
    private val type: IrType
) {

    private fun isNullable(): Boolean = type.isNullable()

    // TODO: (Vlad) Looks like we should support 32bit arch?
    // TODO: (Vlad) Type for functions
    fun getSwiftType(): String {
        val swiftType = if (type.isArray()) {
            "Array<>"
        } else if (type.isUnit()) {
            "Void"
        } else if (type.isInt()) {
            "Int32"
        } else if (type.isLong()) {
            "Int"
        } else if (type.isULong()) {
            "UInt"
        } else if (type.isUInt()) {
            "UInt32"
        } else if (type.isAny()) {
            "Any"
        } else if (type.isBoolean()) {
            "Bool"
        } else if (type.isByte()) {
            "Int8"
        } else if (type.isChar()) {
            "UInt8"
        } else if (type.isDouble()) {
            "Double"
        } else if (type.isFloat()) {
            "Float"
        } else if (type.isString()) {
            "String"
        } else if (type.isKClass()) {
            type.classFqName.toString()
        } else {
            "Any" // TODO: (Vlad) Maybe we should failed here?
        }

        return if (isNullable()) {
            "$swiftType?"
        } else {
            swiftType
        }
    }

}

fun IrFunction.isConstructor() = this is IrConstructor

object SwiftKeywords {
    const val RETURN = "return"
    const val FUNC = "func"
    const val STATIC = "static"
    const val CLASS = "class"

    const val AWAIT = "await"
    const val ASYNC = "async"

    const val NIL = "nil"
    const val IF = "if"
    const val ELSE = "else"

    const val PRIVATE = "private"
    const val PUBLIC = "public"

    const val VAR = "var"
    const val CONST = "let"

    const val CONSTRUCTOR = "init"
}