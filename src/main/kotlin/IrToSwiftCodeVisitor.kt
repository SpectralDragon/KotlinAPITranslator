import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUInt
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isStatic
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import swift.SwiftKeywords

/**
 * First iteration of Kotlin 2 Swift translator.
 * Simple, sometimes easy to read and can use all Kotlin IR.
 * For me it was first iteration to deep dive to IR Visitor.
 *
 * Pros:
 *  - I understand how to work with this sweet API :)
 *
 * Cons:
 *  - Not flexible
 *  - Not ready to complex edges.
 */
class IrToSwiftCodeVisitor(
    private val context: SwiftRenderContext
): IrElementVisitorVoid {

    override fun visitFile(declaration: IrFile) {
        declaration.declarations.forEachIndexed { index, value ->
            value.accept(this, null)

            if (index < declaration.declarations.count() - 1)
                context.newLine()
        }
    }

    override fun visitFunction(declaration: IrFunction) {
        if (declaration.isInline)
            context.put(SwiftKeywords.INLINE)

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

        context.pushIndent()
        context.newLine()
        if (declaration.body != null) {
            declaration.body!!.acceptChildren(this, null)
        } else {
            context.put("fatalError(\"Method not implemented yet.\")")
        }
        context.popIndent()
        context.newLine()

        context.put('}')
        context.newLine()
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

        expression.extensionReceiver?.let {
            it.accept(this, null)

            if (it.type.isMarkedNullable())
                context.put("?")

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
        context.put(expression.symbol.owner.name.toString())
    }

    override fun visitStringConcatenation(expression: IrStringConcatenation) {
        context.put('"')

        expression.arguments.forEach {
            if (it is IrConstImpl<*>) {
                context.put('+') // FIXME: That hack, fix it in future
                context.put('"')
                it.accept(this, null)
                context.put('"')
            } else {
                context.put("\\(")
                it.accept(this, null)
                context.put(")")
            }
        }
        context.put('"')
    }

    override fun visitConstructorCall(expression: IrConstructorCall) {
        val function = expression.symbol.owner
        val clazz = function.parentAsClass

        context.put(clazz.name.toString())
        context.put('(')

        expression.valueArguments.forEachIndexed { index, argument ->
            val parameter = function.valueParameters[index]
            context.put("${parameter.name}: ")
            argument?.accept(this, null)

            if (index < expression.valueArgumentsCount - 1) {
                context.put(", ")
            }
        }

        context.put(')')
    }

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
        context.newLine()
    }

    override fun visitBlock(expression: IrBlock) {
        expression.statements.forEach { it.accept(this, null) }
    }

    override fun visitBlockBody(body: IrBlockBody, data: Nothing?) {
        body.statements.forEach { it.accept(this, null) }
    }

    override fun visitProperty(declaration: IrProperty) {
        declaration.backingField?.let { field ->
            context.put(if (declaration.visibility.isPublicAPI) SwiftKeywords.PUBLIC else "")
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

fun IrFunction.isConstructor() = this is IrConstructor

class SwiftIrType(
    private val type: IrType
) {

    private fun isNullable(): Boolean = type.isNullable()

    // TODO: (Vlad) Looks like we should support 32bit arch?
    // TODO: (Vlad) Type for functions
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun getSwiftType(): String {
        val swiftType = if (type.isUnit()) {
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
        } else {
            if (type is IrSimpleTypeImpl && type.isMutableCollection()) {
                "NS" + type.classifier.owner.symbol.descriptor.name.toString()
            } else {
                type.classFqName.toString()
            }
        }

        return if (isNullable()) {
            "$swiftType?"
        } else {
            swiftType
        }
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrType.isMutableCollection(): Boolean {
    return (this as IrSimpleTypeImpl).classifier.descriptor.name.toString().startsWith("Mutable")
}