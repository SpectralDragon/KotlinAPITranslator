/**
 * This object can render code and supports additional features like indentation and new lines.
 * It's also the great place to add extra features like minimization for code to keep output file less =)
 */
class SwiftRenderContext(
    private var stringBuilder: StringBuilder = StringBuilder(),
    private var indentLevel: Int = 0,
    private val indentGranularity: Int = 4,
    private var isNewLine: Boolean = false
) {

    fun put(char: Char) {
        putIndentIfNeeded()
        stringBuilder.append(char)
    }

    fun put(string: String) {
        putIndentIfNeeded()
        stringBuilder.append(string)
    }

    fun putWhitespace() {
        stringBuilder.append(' ')
    }

    fun pushIndent() {
        indentLevel += 1
    }

    fun newLine() {
        stringBuilder.append('\n')
        isNewLine = true
    }

    fun popIndent() {
        indentLevel -= 1
    }

    override fun toString(): String {
        return stringBuilder.toString()
    }

    // TODO: (Vlad) Cache for indent level
    private fun putIndentIfNeeded() {
        if (!isNewLine)
            return

        isNewLine = false
        val indent = CharArray(indentLevel * indentGranularity) { ' ' }
        stringBuilder.append(indent)
    }
}