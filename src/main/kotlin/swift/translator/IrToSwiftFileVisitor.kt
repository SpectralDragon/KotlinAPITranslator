package swift.translator

import org.jetbrains.kotlin.ir.declarations.IrFile
import swift.SwiftFile

/**
 * Visit Kotlin File IR and collect all declaration for Swift
 */
class IrToSwiftFileVisitor(): IrToSwiftVisitor<SwiftFile>() {
    override fun visitFile(declaration: IrFile, data: SwiftVisitorContext): SwiftFile {
        val declarations = declaration.declarations.map {
            it.accept(IrDeclarationToSwiftVisitor(), data)
        }

        return SwiftFile(declarations)
    }
}
