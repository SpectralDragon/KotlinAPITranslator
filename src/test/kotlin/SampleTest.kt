import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.test.Test
import kotlin.test.assertContains

@OptIn(ExperimentalPathApi::class)
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

        val expectedSwiftContent = """
            func foo() -> Int32 {
                return 42
            }
        """.trimIndent()

        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testMethodCall() {
        val kotlinFileName = "toplevelfunction.kt"
        val swiftFileName = "toplevelfunction.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)

        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, swiftFileName)

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testClass() {
        val kotlinFileName = "dataclass.kt"
        val swiftFileName = "dataclass.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, swiftFileName)

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testMutableList() {
        val kotlinFileName = "mutablelist.kt"
        val swiftFileName = "mutablelist.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, swiftFileName)

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }
}