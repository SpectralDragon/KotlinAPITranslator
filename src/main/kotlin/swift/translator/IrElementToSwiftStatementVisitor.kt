package swift.translator

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import swift.SwiftBlock
import swift.SwiftReturn
import swift.SwiftStatement
import swift.SwiftType
import swift.SwiftWrappedExpressionStatement

class IrElementToSwiftStatementVisitor(): IrToSwiftVisitor<SwiftStatement>() {
    override fun visitReturn(expression: IrReturn, data: SwiftVisitorContext): SwiftStatement {
        return SwiftReturn(expression.value.accept(IrElementToSwiftExpressionVisitor(), data), SwiftType(expression.type))
    }

    override fun visitBlockBody(body: IrBlockBody, data: SwiftVisitorContext): SwiftStatement {
        val statements = body.statements.map {
            it.accept(this, data)
        }
        return SwiftBlock(statements)
    }

    override fun visitBlock(expression: IrBlock, data: SwiftVisitorContext): SwiftStatement {
        val statements = expression.statements.map {
            it.accept(this, data)
        }
        return SwiftBlock(statements)
    }

    override fun visitElement(element: IrElement, data: SwiftVisitorContext): SwiftStatement {
        // Currently we don't support any side effects, we should return something to avoid crash
        return SwiftStatement()
    }

    override fun visitExpression(
        expression: IrExpression,
        data: SwiftVisitorContext
    ): SwiftStatement {
        return SwiftWrappedExpressionStatement(expression.accept(IrElementToSwiftExpressionVisitor(), data))
    }
}