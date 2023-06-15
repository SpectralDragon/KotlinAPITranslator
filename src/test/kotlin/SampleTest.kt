import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.walk
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class, ExperimentalPathApi::class)
class SampleTest {

    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun trivialTest() {
        val mainSourceFile = SourceFile.kotlin(
            "main.kt",
            """
                fun foo(): Int = 42
            """.trimIndent()
        )

        val katComponentRegistrar = KotlinApiTranslatorComponentRegistrar(temporaryDirectory)
        val result = KotlinCompilation().apply {
            sources = listOf(mainSourceFile)
            compilerPluginRegistrars = listOf(katComponentRegistrar)

        }.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")
    }
}