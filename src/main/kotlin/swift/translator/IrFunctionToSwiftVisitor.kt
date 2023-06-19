package swift.translator

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.parentAsClass
import swift.SwiftBlock
import swift.SwiftClass
import swift.SwiftConstructor
import swift.SwiftFunction
import swift.SwiftFunctionParameter
import swift.SwiftType

class IrFunctionToSwiftVisitor(): IrToSwiftVisitor<SwiftFunction>() {
    override fun visitFunction(declaration: IrFunction, data: SwiftVisitorContext): SwiftFunction {
        val parameters = declaration.valueParameters.map {
            SwiftFunctionParameter(it.name.toString(), SwiftType(it.type), null)
        }

        val block = declaration.body?.accept(IrElementToSwiftStatementVisitor(), data) as? SwiftBlock
        return SwiftFunction(
            name = declaration.name.toString(),
            isPublicAPI = declaration.visibility.isPublicAPI,
            parameters = parameters,
            body = block,
            returnType = SwiftType(declaration.returnType)
        )
    }

    override fun visitConstructor(declaration: IrConstructor, data: SwiftVisitorContext): SwiftFunction {
        val parameters = declaration.valueParameters.map {
            SwiftFunctionParameter(it.name.toString(), SwiftType(it.type), null)
        }

        // TODO: (Vlad) It's ok when we render fatal error
//        val block = declaration.body?.accept(IrElementToSwiftStatementVisitor(), data) as? SwiftBlock

        // TODO: Smells like hack...
        val swiftClass = if (data.currentClass?.irClass == declaration.symbol.owner.parentAsClass) {
            data.currentClass
        } else {
            declaration.symbol.owner.parentAsClass.accept(IrDeclarationToSwiftVisitor(), data) as? SwiftClass
        }

        return SwiftConstructor(
            isPublicAPI = declaration.visibility.isPublicAPI,
            parameters = parameters,
            body = null,
            returnType = SwiftType(declaration.returnType),
            owner = requireNotNull(swiftClass)
        )
    }
}
