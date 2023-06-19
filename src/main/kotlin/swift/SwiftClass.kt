package swift

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass

open class SwiftClass(
    val name: String,
    val modality: Modality,
    val declorations: List<SwiftExpression>,
    val methods: List<SwiftFunction>,
    val irClass: IrClass
): SwiftExpression() {

    override fun accept(visitor: SwiftVisitor) {
        visitor.visitClass(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {
        declorations.forEach { it.accept(visitor) }
        methods.forEach { it.accept(visitor) }
    }
}

open class SwiftProperty(
    val name: String,
    val type: SwiftType,
    val isStatic: Boolean,
    val isPublicAPI: Boolean,
    val isConst: Boolean,
    val getter: SwiftFunction?,
    val setter: SwiftFunction?
): SwiftExpression() {
    override fun accept(visitor: SwiftVisitor) {
        visitor.visitProperty(this)
    }

    override fun acceptChildren(visitor: SwiftVisitor) {

    }
}