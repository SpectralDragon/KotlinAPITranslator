package swift.translator

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import swift.SwiftClass
import swift.SwiftExpression
import swift.SwiftFunction

/**
 * Visit Kotlin Declaration IR and collect all for Swift Expression
 */
class IrDeclarationToSwiftVisitor(): IrToSwiftVisitor<SwiftExpression>() {
    override fun visitClass(declaration: IrClass, data: SwiftVisitorContext): SwiftExpression {
        return declaration.toSwiftClass(data)
    }

    override fun visitFunction(declaration: IrFunction, data: SwiftVisitorContext): SwiftExpression {
        return declaration.accept(IrFunctionToSwiftVisitor(), data.copy(currentFunction = declaration))
    }

    override fun visitConstructor(
        declaration: IrConstructor,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return declaration.accept(IrFunctionToSwiftVisitor(), data.copy(currentFunction = declaration))
    }

    override fun visitField(declaration: IrField, data: SwiftVisitorContext): SwiftExpression {
        return declaration.accept(IrElementToSwiftExpressionVisitor(), data)
    }

    override fun visitProperty(
        declaration: IrProperty,
        data: SwiftVisitorContext
    ): SwiftExpression {
        return declaration.accept(IrElementToSwiftExpressionVisitor(), data)
    }
}

fun IrClass.toSwiftClass(context: SwiftVisitorContext): SwiftClass {
    val declorations: MutableList<SwiftExpression> = mutableListOf()
    val methods: MutableList<SwiftFunction> = mutableListOf()

    val clazz = SwiftClass(this.name.toString(), methods = methods, declorations = declorations, modality = modality, irClass = this)
    val newContext = context.copy(currentClass = clazz)

    declarations.forEach {
        when (it) {
            is IrConstructor -> {
                methods.add(it.accept(IrFunctionToSwiftVisitor(), newContext))
            }
            is IrSimpleFunction -> {
                methods.add(it.accept(IrFunctionToSwiftVisitor(), newContext))
            }
            else -> {
                declorations.add(it.accept(IrDeclarationToSwiftVisitor(), newContext))
            }
        }
    }

    return clazz
}