package swift

open class SwiftLiteral(): SwiftExpression() {

    override fun accept(visitor: SwiftVisitor) {
        visitor.visitLiteral(this)
    }
}

open class SwiftNullLiteral(): SwiftLiteral()
open class SwiftIntegerLiteral(val value: Int): SwiftLiteral()
open class SwiftDoubleLiteral(val value: Double): SwiftLiteral()
open class SwiftFloatLiteral(val value: Float): SwiftLiteral()
open class SwiftBooleanLiteral(val value: Boolean): SwiftLiteral()

open class SwiftStringLiteral(val value: String): SwiftLiteral()