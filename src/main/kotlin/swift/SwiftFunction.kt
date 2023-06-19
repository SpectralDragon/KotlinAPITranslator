package swift

open class SwiftFunctionParameter(
    val name: String,
    val type: SwiftType,
    val defaultValue: SwiftElement?
): SwiftElement {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitFunctionParameter(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        TODO("Not yet implemented")
    }

}

open class SwiftFunction(
    val name: String,
    val parameters: List<SwiftFunctionParameter>,
    val body: SwiftBlock?,
    val returnType: SwiftType,
    var isSuspend: Boolean = false,
    var isPublicAPI: Boolean = false,
    var isStatic: Boolean = false,
    var isInline: Boolean = false,
): SwiftExpression() {

    val parametersCount: Int
        get() = parameters.count()

    override fun accept(visitor: SwiftVisitor) {
        visitor.visitFunction(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        body?.accept(visitor)
    }
}

open class SwiftConstructor(
    parameters: List<SwiftFunctionParameter>,
    body: SwiftBlock?,
    returnType: SwiftType,
    isPublicAPI: Boolean,
    val owner: SwiftClass
): SwiftFunction(SwiftKeywords.CONSTRUCTOR, parameters, body, returnType, isPublicAPI = isPublicAPI)