package swift.translator

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.util.isPropertyField
import swift.SwiftBooleanLiteral
import swift.SwiftConcatinating
import swift.SwiftConst
import swift.SwiftDoubleLiteral
import swift.SwiftExpression
import swift.SwiftFloatLiteral
import swift.SwiftFunctionCall
import swift.SwiftGetValue
import swift.SwiftIntegerLiteral
import swift.SwiftNullLiteral
import swift.SwiftProperty
import swift.SwiftStringLiteral
import swift.SwiftType

class IrElementToSwiftExpressionVisitor(): IrToSwiftVisitor<SwiftExpression>() {
    override fun visitElement(element: IrElement, data: SwiftVisitorContext): SwiftExpression {
        // We don't support a huge part of Kotlin IR and to avoid crash we just return base expression without any data.
        return SwiftExpression()
    }

    override fun visitConst(expression: IrConst<*>, data: SwiftVisitorContext): SwiftExpression {
        val value = when (val kind = expression.kind) {
            IrConstKind.Null -> SwiftNullLiteral()
            IrConstKind.String -> SwiftStringLiteral(kind.valueOf(expression).toString())
            IrConstKind.Boolean -> SwiftBooleanLiteral(kind.valueOf(expression) as Boolean)
            IrConstKind.Double -> SwiftDoubleLiteral(kind.valueOf(expression) as Double)
            IrConstKind.Float -> SwiftFloatLiteral(kind.valueOf(expression) as Float)
            IrConstKind.Int, IrConstKind.Long, IrConstKind.Short, IrConstKind.Byte, IrConstKind.Char -> {
                SwiftIntegerLiteral((kind.valueOf(expression) as Number).toInt())
            }
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
        val newContext = data.copy(currentFunction = expression.symbol.owner)
        val function = expression.symbol.owner
        val swiftFunction = function.accept(IrFunctionToSwiftVisitor(), newContext)
        val values = expression.valueArguments.map {
            it?.accept(this, data)
        }

        val extensionReceiver = expression.extensionReceiver?.accept(this, newContext)
        return SwiftFunctionCall(swiftFunction, values, extensionReceiver)
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

        val receiver = expression.extensionReceiver?.accept(this, data)
        return SwiftFunctionCall(swiftFunction, values, receiver)
    }

    override fun visitStringConcatenation(
        expression: IrStringConcatenation,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return SwiftConcatinating(expression.arguments.map { it.accept(IrElementToSwiftExpressionVisitor(), data) })
    }

    override fun visitGetField(expression: IrGetField, data: SwiftVisitorContext): SwiftExpression {
        return SwiftGetValue(expression.symbol.owner.name.toString(), SwiftType(expression.type))
    }

    override fun visitProperty(declaration: IrProperty, data: SwiftVisitorContext): SwiftExpression {
        val getter = declaration.getter?.accept(IrFunctionToSwiftVisitor(), data)
        val setter = declaration.setter?.accept(IrFunctionToSwiftVisitor(), data)

        // FIXME: Feel like we have other solution to detect where val or var properties
        return SwiftProperty(
            name = declaration.name.toString(),
            type = declaration.backingField?.let { SwiftType(it.type) } ?: getter!!.returnType,
            isStatic = declaration.backingField?.isStatic ?: false,
            isPublicAPI = declaration.visibility.isPublicAPI,
            isConst = declaration.setter == null,
            isComputedProperty = declaration.backingField == null,
            getter = getter,
            setter = setter
        )
    }
}
