import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import java.nio.file.Path
import javax.xml.transform.Source
import kotlin.test.assertEquals

object Utils {
    @OptIn(ExperimentalCompilerApi::class)
    fun compile(files: List<SourceFile>, temporaryDirectory: Path) {
        val katComponentRegistrar = KotlinApiTranslatorComponentRegistrar(temporaryDirectory)
        val result = KotlinCompilation().apply {
            sources = files
            compilerPluginRegistrars = listOf(katComponentRegistrar)
        }.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    fun getFileContent(name: String): String {
        return this::class.java.getResourceAsStream(name)!!
            .bufferedReader()
            .readText()
    }

    fun getSourceFile(name: String): SourceFile {
        return this::class.java.getResource(name)!!.let {
            val file = File(it.toURI())
            SourceFile.fromPath(file)
        }
    }
}