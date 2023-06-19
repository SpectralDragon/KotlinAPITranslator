import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(ExperimentalPathApi::class)
class SampleTest {

    @TempDir
    lateinit var temporaryDirectory: Path

    @DisplayName("Test Trivial Function")
    @Test
    fun trivialTest() {
        // given
        val mainSourceFile = SourceFile.kotlin(
            "trivial.kt",
            """
                fun foo(): Int = 42
            """.trimIndent()
        )

        val expectedSwiftContent = """
            public func foo() -> Int32 {
                return 42
            }
        """.trimIndent()

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile("trivial.swift", expectedSwiftContent)
    }

    @DisplayName("Test Top Level Functions")
    @Test
    fun testTopLevelFunction() {
        // given
        val kotlinFileName = "toplevelfunction.kt"
        val swiftFileName = "toplevelfunction.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName, expectedSwiftContent)
    }

    @DisplayName("Test Multiple Classes")
    @Test
    fun testMultipleClassesAndMethods() {
        // given
        val kotlinFileName = "dataclass.kt"
        val swiftFileName = "dataclass.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName, expectedSwiftContent)
    }

    @DisplayName("Test mutable list")
    @Test
    fun testMutableList() {
        // given
        val kotlinFileName = "mutablelist.kt"
        val swiftFileName = "mutablelist.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName, expectedSwiftContent)
    }

    // TODO: Currently not works

//    @DisplayName("Test class with companion object")
//    @Test
//    fun testClassWithCompanionObject() {
//        val kotlinFileName = "companionobject.kt"
//        val swiftFileName = "companionobject.swift"
//        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
//        val expectedSwiftContent = Utils.getFileContent(swiftFileName)
//
//        Utils.compile(listOf(mainSourceFile), temporaryDirectory)
//
//        val generatedFiles = temporaryDirectory.walk().toList()
//        assertContains(generatedFiles.map { it.name }, swiftFileName)
//
//        generatedFiles.forEach { file ->
//            file.printFileContent()
//            assertContains(expectedSwiftContent, file.readText(Charsets.UTF_8))
//        }
//    }

    // region Helpers
    private fun assertContainsSwiftFile(swiftFile: String, expectedSwiftContent: String) {
        val file = temporaryDirectory.walk().toList().first { it.name == swiftFile }
        assertEquals(file.name, swiftFile)
        assertEquals(file.readText(Charsets.UTF_8), expectedSwiftContent)
    }
    // endregion
}