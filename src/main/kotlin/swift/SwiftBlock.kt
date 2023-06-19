package swift

open class SwiftBlock(
    val statements: List<SwiftStatement>
): SwiftStatement() {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitBlock(this)
    }
    override fun acceptChildren(visitor: SwiftVisitor) {
        statements.forEach {
            it.accept(visitor)
        }
    }
}

open class SwiftReturn(
    val value: SwiftExpression,
    val returnType: SwiftType
): SwiftStatement() {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitReturn(this)
    }
}