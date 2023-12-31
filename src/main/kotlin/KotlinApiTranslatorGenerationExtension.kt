import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.nio.file.Path

internal class KotlinApiTranslatorGenerationExtension(
    private val outputDirectory: Path,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = KotlinApiTranslatorVisitor(outputDirectory)
        moduleFragment.acceptVoid(visitor)
    }
}