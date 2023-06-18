import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.nio.file.Path
import kotlin.test.assertEquals

object TestUtils {
    @OptIn(ExperimentalCompilerApi::class)
    fun compile(files: List<SourceFile>, temporaryDirectory: Path) {
        val katComponentRegistrar = KotlinApiTranslatorComponentRegistrar(temporaryDirectory)
        val result = KotlinCompilation().apply {
            sources = files
            compilerPluginRegistrars = listOf(katComponentRegistrar)
        }.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}