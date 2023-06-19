import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.utils.getJsName
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUInt
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isUnit

class SwiftIrType(
    private val type: IrType
) {

    private fun isNullable(): Boolean = type.isNullable()

    // TODO: (Vlad) Looks like we should support 32bit arch?
    // TODO: (Vlad) Type for functions
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun getSwiftType(): String {
        val swiftType = if (type.isUnit()) {
            "Void"
        } else if (type.isInt()) {
            "Int32"
        } else if (type.isLong()) {
            "Int"
        } else if (type.isULong()) {
            "UInt"
        } else if (type.isUInt()) {
            "UInt32"
        } else if (type.isAny()) {
            "Any"
        } else if (type.isBoolean()) {
            "Bool"
        } else if (type.isByte()) {
            "Int8"
        } else if (type.isChar()) {
            "UInt8"
        } else if (type.isDouble()) {
            "Double"
        } else if (type.isFloat()) {
            "Float"
        } else if (type.isString()) {
            "String"
        } else {
            if (type is IrSimpleTypeImpl && type.isMutableCollection()) {
                "NS" + type.classifier.owner.symbol.descriptor.name.toString()
            } else {
                type.classFqName.toString()
            }
        }

        return if (isNullable()) {
            "$swiftType?"
        } else {
            swiftType
        }
    }

    fun isMutableCollection() = type.isMutableCollection()

}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrType.isMutableCollection(): Boolean {
    return (this as IrSimpleTypeImpl).classifier.descriptor.name.toString().startsWith("Mutable")
}