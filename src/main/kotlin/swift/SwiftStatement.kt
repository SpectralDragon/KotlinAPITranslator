package swift

open class SwiftStatement: SwiftElement {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitElement(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        TODO("Not yet implemented")
    }
}