
// TODO: (Vlad) Minimized code?
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

    fun put(chars: CharArray) {
        putIndentIfNeeded()
        stringBuilder.append(chars)
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