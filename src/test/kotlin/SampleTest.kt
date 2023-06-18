import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.io.TempDir
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

        TestUtils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testMethodCall() {
        val mainSourceFile = SourceFile.kotlin(
            "main.kt",
            """
                fun foo() {
                    println("foo")
                }
            """.trimIndent()
        )

        val expectedSwiftContent = """
            func foo() {
                println("foo")
            }
        """.trimIndent()

        TestUtils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testConcatinating() {
        val mainSourceFile = SourceFile.kotlin(
            "main.kt",
            """
                fun bar(arg1: Int, arg2: Long): String {
                    return "{$}arg1 {$}arg2"
                }
            """.trimIndent()
        )

        val expectedSwiftContent = """
            func bar(arg1: Int32, arg2: Int) -> String {
                return "\(arg1) \(arg2)"
            }
        """.trimIndent()

        TestUtils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testCall() {
        val mainSourceFile = SourceFile.kotlin(
            "main.kt",
            """
                fun baz(arg: String) =
                    arg.lowercase()
            """.trimIndent()
        )

        val expectedSwiftContent = """
            func baz(arg: String) {
                arg.lowercase()
            }
        """.trimIndent()

        TestUtils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun testClass() {
        val mainSourceFile = SourceFile.kotlin(
            "main.kt",
            """
                class Payload(val data: Long)
            """.trimIndent()
        )

        val expectedSwiftContent = """
            class Payload {
                let data: Int
            }
        """.trimIndent()

        TestUtils.compile(listOf(mainSourceFile), temporaryDirectory)

        val generatedFiles = temporaryDirectory.walk().toList()
        assertContains(generatedFiles.map { it.name }, "main.swift")

        generatedFiles.forEach { file ->
            file.printFileContent()
            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
        }
    }
}