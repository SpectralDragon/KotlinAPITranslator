import org.jetbrains.kotlin.descriptors.Modality
import swift.SwiftBlock
import swift.SwiftBooleanLiteral
import swift.SwiftClass
import swift.SwiftConcatinating
import swift.SwiftConst
import swift.SwiftConstructor
import swift.SwiftDoubleLiteral
import swift.SwiftElement
import swift.SwiftFloatLiteral
import swift.SwiftFunction
import swift.SwiftFunctionCall
import swift.SwiftFunctionParameter
import swift.SwiftGetValue
import swift.SwiftIntegerLiteral
import swift.SwiftKeywords
import swift.SwiftLiteral
import swift.SwiftNullLiteral
import swift.SwiftPrimitiveKind
import swift.SwiftProperty
import swift.SwiftReturn
import swift.SwiftStringLiteral
import swift.SwiftVisitor

/**
 * Visitor for rendering all Swift IR tree to code.
 *
 * Pros:
 *  - Flexible enough to handle swift specific statements and expressions.
 *  - Simple API similar to Kotlin IR.
 *
 *  Cons:
 *  - Extra IR for Swift
 *  - Maybe difficult to read for the first time.
 */
class SwiftRenderVisitor(private val context: SwiftRenderContext): SwiftVisitor() {
    override fun visitClass(declaration: SwiftClass) {
        if (declaration.modality != Modality.FINAL)
            return

        if (declaration.irClass.visibility.isPublicAPI)
            context.put("${SwiftKeywords.PUBLIC} ")

        context.put("${SwiftKeywords.CLASS} ${declaration.name} ")
        context.put('{')
        context.pushIndent()
        context.newLine()

        declaration.acceptChildren(this)

        context.popIndent()
        context.put('}')
        context.newLine()
    }

    override fun visitReturn(expression: SwiftReturn) {
        if (expression.returnType.kind == SwiftPrimitiveKind.VOID)
            return

        context.put(SwiftKeywords.RETURN)
        context.putWhitespace()
        expression.value.accept(this)
    }

    override fun visitBlock(body: SwiftBlock) {
        body.acceptChildren(this)
    }

    override fun visitFunction(function: SwiftFunction) {
        // TODO: (Vlad) Maybe should be a constant
        if (function.isInline)
            context.put("@inline(__always)")

        if (function.isPublicAPI) {
            context.put(SwiftKeywords.PUBLIC)
            context.putWhitespace()
        }

        if (function !is SwiftConstructor) {
            if (function.isStatic) {
                context.put(SwiftKeywords.STATIC)
                context.putWhitespace()
            }

            context.put(SwiftKeywords.FUNC)
            context.putWhitespace()
            context.put(function.name)
        } else {
            context.put(SwiftKeywords.CONSTRUCTOR)
        }

        context.put("(")

        function.parameters.forEachIndexed { index, value ->
            value.accept(this)

            if (index < function.parametersCount - 1) {
                context.put(", ")
            }
        }

        context.put(')')
        context.putWhitespace()

        if (function.isSuspend) {
            context.put(SwiftKeywords.ASYNC)
            context.putWhitespace()
        }

        function.returnType.let {
            if ((function is SwiftConstructor) || it.kind == SwiftPrimitiveKind.VOID)
                return@let

            context.put("-> ")
            context.put(function.returnType.toString())
            context.putWhitespace()
        }

        context.put('{')

        context.pushIndent()
        context.newLine()
        if (function.body != null) {
            function.body.acceptChildren(this)
        } else {
            context.put("fatalError(\"Method not implemented yet.\")")
        }
        context.popIndent()
        context.newLine()

        context.put('}')
        context.newLine()
    }

    override fun visitElement(element: SwiftElement) {
        println(element)
    }

    override fun visitFunctionParameter(parameter: SwiftFunctionParameter) {
        context.put("${parameter.name}: ${parameter.type.getNameForFunctionParameter(parameter)}")

        parameter.defaultValue.let {
            when (it) {
                is SwiftConst -> {
                    context.put("= ${it.value}")
                }
            }
        }
    }

    override fun visitProperty(property: SwiftProperty) {
        if (property.isPublicAPI) {
            context.put(SwiftKeywords.PUBLIC)
            context.putWhitespace()
        }

        if (property.isStatic) {
            context.put(SwiftKeywords.STATIC)
            context.putWhitespace()
        }

        context.put(if (property.isConst && !property.isComputedProperty) SwiftKeywords.CONST else SwiftKeywords.VAR)
        context.putWhitespace()
        context.put(property.name)
        context.put(": ${property.type}")

        if (property.isComputedProperty) {
            renderClosureBlock {
                if (property.setter != null) {

                    // Render getter first
                    context.put(SwiftKeywords.GETTER)
                    renderClosureBlock {
                        property.getter?.acceptChildren(this)
                    }

                    context.newLine()

                    // Render setter
                    context.put(SwiftKeywords.SETTER)
                    renderClosureBlock {
                        property.setter.acceptChildren(this)
                    }

                } else {
                    // We don't any setter, render only getter without get block
                    property.getter?.acceptChildren(this)
                }
            }
        }

        context.newLine()
    }

    override fun visitLiteral(literal: SwiftLiteral) {
        when (literal) {
            is SwiftStringLiteral -> {
                context.put("\"${literal.value}\"")
            }
            is SwiftIntegerLiteral -> {
                context.put(literal.value.toString())
            }
            is SwiftBooleanLiteral -> {
                context.put(if (literal.value) "true" else "false")
            }
            is SwiftDoubleLiteral -> {
                context.put(literal.value.toString())
            }
            is SwiftFloatLiteral -> {
                context.put(literal.value.toString())
            }
            is SwiftNullLiteral -> {
                context.put(SwiftKeywords.NIL)
            }
            else -> {}
        }
    }

    override fun visitFunctionCall(expression: SwiftFunctionCall) {
        if (expression.functionDeclaration.isSuspend) {
            context.put(SwiftKeywords.AWAIT)
            context.putWhitespace()
        }

        when (expression.functionDeclaration) {
            is SwiftConstructor -> {
                context.put(expression.functionDeclaration.owner.name)
            }
            else -> {
                expression.extensionReceiver?.let {
                    it.accept(this)
                    context.put('.')
                }

                // Starts day with great hack for getters!
                context.put(expression.functionDeclaration.getFunctionNameForCall())

                if (expression.functionDeclaration.isSystemFunction())
                    return
            }
        }

        context.put('(')

        expression.functionDeclaration.parameters.forEachIndexed { index, param ->
            context.put("${param.name}: ")
            expression.values[index]?.accept(this)

            if (index < expression.functionDeclaration.parametersCount - 1)
                context.put(", ")
        }

        context.put(')')
    }

    override fun visitStringConcatinating(concatinating: SwiftConcatinating) {
        concatinating.expressions.forEachIndexed { index, expression ->
            if ((expression is SwiftConst) && (expression.value is SwiftStringLiteral)) {
                expression.value.accept(this)
            } else {
                context.put('"')
                context.put("\\(")
                expression.accept(this)
                context.put(")")
                context.put('"')
            }

            if (index < concatinating.expressions.count() - 1)
                context.put(" + ")
        }
    }

    override fun visitConst(const: SwiftConst) {
        const.value.accept(this)
    }

    override fun visitGetValue(value: SwiftGetValue) {
        context.put(value.valueName)
    }

    // region Helpers

    private fun renderClosureBlock(block: () -> Unit) {
        context.put(" {")
        context.pushIndent()
        context.newLine()

        block()
        context.newLine()
        context.popIndent()
        context.put("}")
    }

    // endregion
}

/**
 * Hack for understand was it a system call
 */
fun SwiftFunction.isSystemFunction(): Boolean {
    return name.startsWith("<") && name.endsWith(">")
}

/**
 * My excellent solution to get property name from getter function, otherwise return declared name.
 */
fun SwiftFunction.getFunctionNameForCall(): String {
    if (!isSystemFunction())
        return name

    return if (name.startsWith("<get")) {
        name.removePrefix("<get-").dropLast(1)
    } else {
        name
    }
}