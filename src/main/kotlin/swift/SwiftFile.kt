package swift

open class SwiftFile(
    private val declarations: List<SwiftElement>
): SwiftElement {
    override fun acceptChildren(visitor: SwiftVisitor) {
        declarations.forEach { it.accept(visitor) }
    }

    override fun accept(visitor: SwiftVisitor) {
        TODO("Not yet implemented")
    }
}