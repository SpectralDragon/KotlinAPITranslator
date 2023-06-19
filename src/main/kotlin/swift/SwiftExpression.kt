package swift

open class SwiftExpression(): SwiftElement {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitExpression(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {

    }
}

open class SwiftFunctionCall(
    val functionDeclaration: SwiftFunction,
    val values: List<SwiftExpression?>,
    val receiver: SwiftExpression?
): SwiftExpression() {

    override fun accept(visitor: SwiftVisitor) {
        visitor.visitFunctionCall(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        values.forEach { it?.accept(visitor) }
    }
}

open class SwiftGetValue(
    val valueName: String,
    val type: SwiftType
): SwiftExpression() {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitGetValue(this)
    }
}

open class SwiftConst(
    val value: SwiftExpression,
    val type: SwiftType
): SwiftExpression() {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitConst(this)
    }
}