import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.nio.file.Path


@OptIn(ExperimentalCompilerApi::class)
class KotlinApiTranslatorComponentRegistrar(
    private val outputPath: Path,
) : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(KotlinApiTranslatorGenerationExtension(outputPath))
    }
}

