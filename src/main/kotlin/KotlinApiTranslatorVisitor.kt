import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import java.io.File
import java.nio.file.Path

internal class KotlinApiTranslatorVisitor(
    private val outputDirectory: Path,
) : IrElementVisitorVoid {

    override fun visitModuleFragment(declaration: IrModuleFragment) {
        // Start here. Feel free to change the method's body any way you like.
        declaration.files.forEach { irFile ->
            val fileName = irFile.name.substringBefore('.')
            val swiftFile = File(outputDirectory.toFile(), "$fileName.swift")
            swiftFile.createNewFile()
        }
    }
}