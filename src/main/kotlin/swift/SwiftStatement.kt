package swift

open class SwiftStatement: SwiftElement {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitStatement(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        TODO("Not yet implemented")
    }
}

open class SwiftWrappedExpressionStatement(
    val expression: SwiftExpression
): SwiftStatement() {
    override fun accept(visitor: SwiftVisitor) {
        expression.accept(visitor)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        expression.accept(visitor)
    }
}