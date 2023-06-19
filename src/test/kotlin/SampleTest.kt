import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.test.Test
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

    @DisplayName("Test multiple packages")
    @Test()
    fun testMultiplePackages() {
        // given
        val kotlinFileFirst = SourceFile.kotlin("mutliplepackagefile1.kt", """
            package a.b.c

            class Foo()
        """.trimIndent())

        val kotlinFileSecond = SourceFile.kotlin("mutliplepackagefile2.kt", """
            package b.c.d

            class Bar()
        """.trimIndent())

        val swiftFileName1 = "mutliplepackagefile1.swift"
        val expectedSwiftContentFile1 = Utils.getFileContent(swiftFileName1)

        val swiftFileName2 = "mutliplepackagefile2.swift"
        val expectedSwiftContentFile2 = Utils.getFileContent(swiftFileName2)

        // when
        Utils.compile(listOf(kotlinFileFirst, kotlinFileSecond), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName1, expectedSwiftContentFile1)
        assertContainsSwiftFile(swiftFileName2, expectedSwiftContentFile2)
    }

    @DisplayName("Test different types")
    @Test
    fun testDifferentTypes() {
        // given
        val kotlinFileName = "differenttypes.kt"
        val swiftFileName = "differenttypes.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName, expectedSwiftContent)
    }

    @DisplayName("Test Getter and Setter in Class Field")
    @Test
    fun testGetterAndSetter() {
        // given
        val kotlinFileName = "gettersetter.kt"
        val swiftFileName = "gettersetter.swift"
        val mainSourceFile = Utils.getSourceFile(kotlinFileName)
        val expectedSwiftContent = Utils.getFileContent(swiftFileName)

        // when
        Utils.compile(listOf(mainSourceFile), temporaryDirectory)

        // then
        assertContainsSwiftFile(swiftFileName, expectedSwiftContent)
    }

    // region Helpers
    private fun assertContainsSwiftFile(swiftFile: String, expectedSwiftContent: String) {
        val file = temporaryDirectory.walk().toList().first { it.name == swiftFile }
        assertEquals(file.name, swiftFile)
        assertEquals(file.readText(Charsets.UTF_8), expectedSwiftContent)
    }
    // endregion
}