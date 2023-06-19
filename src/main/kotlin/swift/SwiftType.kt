package swift

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isSequence
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUByte
import org.jetbrains.kotlin.ir.types.isUInt
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isUShort
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType


enum class SwiftPrimitiveKind(val rawString: String) {
    CLASS("Any"), // any class
    COLLECTION("Collection"), // any collection
    INTERFACE("Protocol"), // TODO: Think about it
    STRING("String"),
    DOUBLE("Double"), // double
    FLOAT("Float"), // float
    INT8("Int8"), // byte
    INT16("Int16"), // short, char
    INT32("Int32"), // int
    INT64("Int"), // long
    BOOLEAN("Bool"),
    UINT8("UInt8"), // ubyte
    UINT16("UInt16"), // ushort, uchar
    UINT32("UInt32"), // uint
    UINT64("UInt"), // ulong
    VOID("Void"), // unit
    ANY("Any")
}

class SwiftType(
    private val type: IrType
) {
    val kind: SwiftPrimitiveKind
    private var isNullable: Boolean

    init {
        kind = type.getSwiftTypeKind()
        isNullable = type.isNullable()
    }

    override fun toString(): String {
        val name = when (kind) {
            SwiftPrimitiveKind.COLLECTION -> {
                toArray()
            }
            SwiftPrimitiveKind.CLASS -> {
                getKotlinName()
            }
            else -> {
                kind.rawString
            }
        }

        return if (isNullable) "$name?" else name
    }

    // FIXME: Refactor it...

    fun getNameForClass(clazz: SwiftClass): String {
        return toString()
    }

    fun getNameForFunctionParameter(functionParameter: SwiftFunctionParameter): String {
        return when (kind) {
            SwiftPrimitiveKind.COLLECTION -> {
                if (isMutableCollection()) {
                    "inout ${toString()}"
                } else {
                    toString()
                }
            }
            else -> {
                toString()
            }
        }
    }


    private fun toArray(): String {
        return if (type is IrSimpleTypeImpl) {
            getKotlinName()
        } else {
            return "Any"
        }
    }

    /**
    Helper method to detect mutable collections
     */
    fun isMutableCollection(): Boolean {
        (type as IrSimpleTypeImpl)
        return getKotlinName().startsWith("Mutable")
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun getKotlinName(): String {
        return (type as? IrSimpleTypeImpl)?.classifier?.descriptor?.name?.toString() ?: kind.rawString
    }
}

// I don't like that, but I also don't want to use signatures solution...
fun IrType.getSwiftTypeKind(): SwiftPrimitiveKind {
    return if (isBoolean()) {
        SwiftPrimitiveKind.BOOLEAN
    } else if (isFloat()) {
        SwiftPrimitiveKind.FLOAT
    } else if (isDouble()) {
        SwiftPrimitiveKind.DOUBLE
    } else if (isULong()) {
        SwiftPrimitiveKind.UINT64
    } else if (isUInt()) {
        SwiftPrimitiveKind.UINT32
    } else if (isUShort()) {
        SwiftPrimitiveKind.UINT16
    } else if (isUByte()) {
        SwiftPrimitiveKind.UINT8
    } else if (isLong()) {
        SwiftPrimitiveKind.INT64
    } else if (isInt()) {
        SwiftPrimitiveKind.INT32
    } else if (isShort()) {
        SwiftPrimitiveKind.INT16
    } else if (isByte()) {
        SwiftPrimitiveKind.INT8
    } else if (isAny()) {
        SwiftPrimitiveKind.ANY
    } else if (isSequence()) {
        SwiftPrimitiveKind.COLLECTION
    } else if (isString()) {
        SwiftPrimitiveKind.STRING
    } else if (isKClass()) {
        SwiftPrimitiveKind.CLASS
    } else if (isUnit()) {
        SwiftPrimitiveKind.VOID
    } else {
        SwiftPrimitiveKind.CLASS // TODO: (Vlad) I currently not sure about it, we should discuss..
    }
}