import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import swift.translator.IrToSwiftFileVisitor
import swift.translator.SwiftVisitorContext
import java.io.File
import java.nio.file.Path

// FIXME: mutablelist test doesn't work

internal class KotlinApiTranslatorVisitor(
    private val outputDirectory: Path,
) : IrElementVisitorVoid {

    override fun visitModuleFragment(declaration: IrModuleFragment) {
        // Start here. Feel free to change the method's body any way you like.
        declaration.files.forEach { irFile ->
            val context = SwiftRenderContext()

            val visitorContext = SwiftVisitorContext(irFile)
            val element = irFile.accept(IrToSwiftFileVisitor(), visitorContext)
            val visitor = SwiftRenderVisitor(context)
            element.acceptChildren(visitor)

            // Hack to delete last new line character
            context.removeLastCharacter()

            val content = context.toString()
            val fileName = irFile.name.substringBefore('.')
            val swiftFile = File(outputDirectory.toFile(), "$fileName.swift")
            swiftFile.writeText(content)
            swiftFile.createNewFile()
        }
    }
}