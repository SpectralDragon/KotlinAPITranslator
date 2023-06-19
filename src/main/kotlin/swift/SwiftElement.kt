package swift

interface SwiftElement {
    fun accept(visitor: SwiftVisitor)

    fun acceptChildren(visitor: SwiftVisitor)
}