package swift.translator

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import swift.SwiftClass
import swift.SwiftElement

open class IrToSwiftVisitor<E: SwiftElement>(): IrElementVisitor<E, SwiftVisitorContext> {
    override fun visitElement(element: IrElement, data: SwiftVisitorContext): E {
        TODO("Not implemented")
    }
}

data class SwiftVisitorContext(
    var currentFile: IrFile,
    var currentClass: SwiftClass? = null,
    var currentFunction: IrFunction? = null
)