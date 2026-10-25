package io.eigenon.jetbrains.openqasm.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class OpenQasmLexer : LexerBase() {
    private var currentBuffer: CharSequence = ""
    private var currentBufferEnd: Int = 0
    private var currentTokenStart: Int = 0
    private var currentTokenEnd: Int = 0
    private var currentTokenType: IElementType? = null
    private var dialect: Dialect = Dialect.UNKNOWN

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int,
    ) {
        currentBuffer = buffer
        currentBufferEnd = endOffset
        currentTokenStart = startOffset
        currentTokenEnd = startOffset
        dialect = detectDialect(buffer)
        locateToken()
    }

    override fun getState(): Int = dialect.state

    override fun getTokenType(): IElementType? = currentTokenType

    override fun getTokenStart(): Int = currentTokenStart

    override fun getTokenEnd(): Int = currentTokenEnd

    override fun advance() {
        currentTokenStart = currentTokenEnd
        locateToken()
    }

    override fun getBufferSequence(): CharSequence = currentBuffer

    override fun getBufferEnd(): Int = currentBufferEnd

    private fun locateToken() {
        if (currentTokenStart >= currentBufferEnd) {
            currentTokenEnd = currentTokenStart
            currentTokenType = null
            return
        }

        val first = currentBuffer[currentTokenStart]

        when {
            isWhitespace(first) -> scanWhitespace()
            startsWith(currentTokenStart, "//") -> scanLineComment()
            startsWith(currentTokenStart, "/*") && dialect != Dialect.OPENQASM_2 -> scanBlockComment()
            dialect.isOpenQasm3 && isDirectivePayloadStart(currentTokenStart) -> scanDirectiveText()
            dialect.isOpenQasm3 && isPragmaStart(currentTokenStart) -> scanPragmaKeyword()
            dialect.isOpenQasm3 && first == '@' && charAtOrNull(currentTokenStart + 1)?.let(::isIdentifierStart) == true ->
                scanAnnotationKeyword()
            first == '"' -> scanString('"')
            first == '\'' && dialect.isOpenQasm3 && isArbitraryStringContext(currentTokenStart) ->
                scanString('\'')
            isNumberStart(first, currentTokenStart) -> scanNumber()
            first == '$' && charAtOrNull(currentTokenStart + 1)?.isDigit() == true -> scanPhysicalQubit()
            isIdentifierStart(first) -> scanIdentifier()
            first in PUNCTUATION -> emit(OpenQasmTokenTypes.PUNCTUATION, currentTokenStart + 1)
            first in OPERATOR_CHARACTERS -> scanOperator()
            else -> emit(TokenType.BAD_CHARACTER, currentTokenStart + 1)
        }
    }

    private fun scanWhitespace() {
        var offset = currentTokenStart + 1
        while (offset < currentBufferEnd && isWhitespace(currentBuffer[offset])) {
            offset++
        }
        emit(TokenType.WHITE_SPACE, offset)
    }

    private fun scanLineComment() {
        var offset = currentTokenStart + 2
        while (offset < currentBufferEnd && currentBuffer[offset] != '\n' && currentBuffer[offset] != '\r') {
            offset++
        }
        emit(OpenQasmTokenTypes.LINE_COMMENT, offset)
    }

    private fun scanBlockComment() {
        var offset = currentTokenStart + 2
        while (offset < currentBufferEnd - 1 && !startsWith(offset, "*/")) {
            offset++
        }
        if (offset < currentBufferEnd - 1) {
            offset += 2
        } else {
            offset = currentBufferEnd
        }
        emit(OpenQasmTokenTypes.BLOCK_COMMENT, offset)
    }

    private fun scanString(delimiter: Char) {
        var offset = currentTokenStart + 1
        var escaped = false
        while (offset < currentBufferEnd) {
            val character = currentBuffer[offset]
            if (!escaped && character == delimiter) {
                offset++
                break
            }
            if (!escaped && (character == '\n' || character == '\r')) {
                break
            }
            escaped = !escaped && character == '\\'
            if (character != '\\') {
                escaped = false
            }
            offset++
        }
        emit(OpenQasmTokenTypes.STRING, offset)
    }

    private fun scanDirectiveText() {
        var offset = currentTokenStart + 1
        while (offset < currentBufferEnd && currentBuffer[offset] != '\n' && currentBuffer[offset] != '\r') {
            offset++
        }
        emit(OpenQasmTokenTypes.DIRECTIVE_TEXT, offset)
    }

    private fun scanPragmaKeyword() {
        val keywordLength = if (currentBuffer[currentTokenStart] == '#') "#pragma".length else "pragma".length
        emit(OpenQasmTokenTypes.KEYWORD, currentTokenStart + keywordLength)
    }

    private fun scanAnnotationKeyword() {
        var offset = currentTokenStart + 2
        while (offset < currentBufferEnd && isIdentifierPart(currentBuffer[offset])) {
            offset++
        }
        emit(OpenQasmTokenTypes.KEYWORD, offset)
    }

    private fun scanNumber() {
        var offset = currentTokenStart

        if (startsWithIgnoreCase(offset, "0x")) {
            offset = scanDigits(offset + 2) { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
        } else if (startsWithIgnoreCase(offset, "0b")) {
            offset = scanDigits(offset + 2) { it == '0' || it == '1' }
        } else if (startsWithIgnoreCase(offset, "0o")) {
            offset = scanDigits(offset + 2) { it in '0'..'7' }
        } else {
            offset = scanDigits(offset) { it.isDigit() }
            if (offset < currentBufferEnd && currentBuffer[offset] == '.') {
                offset = scanDigits(offset + 1) { it.isDigit() }
            }
            if (offset < currentBufferEnd && currentBuffer[offset].lowercaseChar() == 'e') {
                var exponentEnd = offset + 1
                if (exponentEnd < currentBufferEnd && currentBuffer[exponentEnd] in setOf('+', '-')) {
                    exponentEnd++
                }
                offset = scanDigits(exponentEnd) { it.isDigit() }
            }
        }

        val tokenType = if (isVersionLiteral(currentTokenStart)) {
            OpenQasmTokenTypes.VERSION
        } else {
            OpenQasmTokenTypes.NUMBER
        }
        emit(tokenType, offset.coerceAtLeast(currentTokenStart + 1))
    }

    private fun scanDigits(startOffset: Int, isDigit: (Char) -> Boolean): Int {
        var offset = startOffset
        while (offset < currentBufferEnd) {
            val character = currentBuffer[offset]
            if (character != '_' && !isDigit(character)) {
                break
            }
            offset++
        }
        return offset
    }

    private fun scanPhysicalQubit() {
        var offset = currentTokenStart + 2
        while (offset < currentBufferEnd && currentBuffer[offset].isDigit()) {
            offset++
        }
        emit(OpenQasmTokenTypes.IDENTIFIER, offset)
    }

    private fun scanIdentifier() {
        var offset = currentTokenStart + 1
        while (offset < currentBufferEnd && isIdentifierPart(currentBuffer[offset])) {
            offset++
        }

        val identifier = currentBuffer.subSequence(currentTokenStart, offset).toString()
        val tokenType = when (identifier) {
            in declarationsForDialect() -> OpenQasmTokenTypes.DECLARATION
            in keywordsForDialect() -> OpenQasmTokenTypes.KEYWORD
            in typesForDialect() -> OpenQasmTokenTypes.TYPE
            in builtinGatesForDialect() -> OpenQasmTokenTypes.BUILTIN_GATE
            in builtinConstantsForDialect() -> OpenQasmTokenTypes.BUILTIN_CONSTANT
            else -> OpenQasmTokenTypes.IDENTIFIER
        }
        emit(tokenType, offset)
    }

    private fun keywordsForDialect(): Set<String> = when (dialect) {
        Dialect.OPENQASM_2 -> OPENQASM_2_KEYWORDS
        Dialect.OPENQASM_3, Dialect.VERSIONLESS -> OPENQASM_3_KEYWORDS
        Dialect.UNKNOWN -> KEYWORDS
    }

    private fun declarationsForDialect(): Set<String> = when (dialect) {
        Dialect.OPENQASM_2 -> OPENQASM_2_DECLARATIONS
        Dialect.OPENQASM_3, Dialect.VERSIONLESS -> OPENQASM_3_DECLARATIONS
        Dialect.UNKNOWN -> DECLARATIONS
    }

    private fun typesForDialect(): Set<String> = when (dialect) {
        Dialect.OPENQASM_2 -> OPENQASM_2_TYPES
        Dialect.OPENQASM_3, Dialect.VERSIONLESS -> OPENQASM_3_TYPES
        Dialect.UNKNOWN -> TYPES
    }

    private fun builtinConstantsForDialect(): Set<String> = when (dialect) {
        Dialect.OPENQASM_2 -> OPENQASM_2_BUILTIN_CONSTANTS
        Dialect.OPENQASM_3, Dialect.VERSIONLESS -> OPENQASM_3_BUILTIN_CONSTANTS
        Dialect.UNKNOWN -> BUILTIN_CONSTANTS
    }

    private fun builtinGatesForDialect(): Set<String> = when (dialect) {
        Dialect.OPENQASM_2 -> OPENQASM_2_BUILTIN_GATES
        Dialect.OPENQASM_3, Dialect.VERSIONLESS -> OPENQASM_3_BUILTIN_GATES
        Dialect.UNKNOWN -> BUILTIN_GATES
    }

    private fun scanOperator() {
        val operator = OPERATORS.firstOrNull { startsWith(currentTokenStart, it) }
        emit(OpenQasmTokenTypes.OPERATOR, currentTokenStart + (operator?.length ?: 1))
    }

    private fun emit(type: IElementType, endOffset: Int) {
        currentTokenType = type
        currentTokenEnd = endOffset
    }

    private fun isNumberStart(character: Char, offset: Int): Boolean =
        character.isDigit() ||
            (character == '.' && charAtOrNull(offset + 1)?.isDigit() == true)

    private fun isVersionLiteral(offset: Int): Boolean =
        previousIdentifier(offset) == "OPENQASM"

    private fun isArbitraryStringContext(offset: Int): Boolean =
        previousIdentifier(offset) in OPENQASM_3_ARBITRARY_STRING_KEYWORDS

    private fun isPragmaStart(offset: Int): Boolean {
        val keyword = if (startsWith(offset, "#pragma")) "#pragma" else "pragma"
        if (!startsWith(offset, keyword)) {
            return false
        }
        return charAtOrNull(offset + keyword.length)?.let(::isIdentifierPart) != true
    }

    private fun isDirectivePayloadStart(offset: Int): Boolean {
        var cursor = offset - 1
        while (cursor >= 0 && currentBuffer[cursor] in HORIZONTAL_WHITESPACE) {
            cursor--
        }
        if (cursor < 0 || currentBuffer[cursor] == '\n' || currentBuffer[cursor] == '\r') {
            return false
        }

        val identifierEnd = cursor + 1
        while (cursor >= 0 && isIdentifierPart(currentBuffer[cursor])) {
            cursor--
        }
        if (identifierEnd == cursor + 1) {
            return false
        }

        val identifier = currentBuffer.subSequence(cursor + 1, identifierEnd).toString()
        return (identifier == "pragma" && (cursor < 0 || currentBuffer[cursor] != '@')) ||
            (cursor >= 0 && currentBuffer[cursor] == '@')
    }

    private fun previousIdentifier(offset: Int): String? {
        var cursor = offset - 1
        while (cursor >= 0 && isWhitespace(currentBuffer[cursor])) {
            cursor--
        }
        val wordEnd = cursor + 1
        while (cursor >= 0 && isIdentifierPart(currentBuffer[cursor])) {
            cursor--
        }
        if (cursor + 1 == wordEnd) {
            return null
        }
        return currentBuffer.subSequence(cursor + 1, wordEnd).toString()
    }

    private fun detectDialect(buffer: CharSequence): Dialect {
        var offset = 0
        val endOffset = buffer.length

        while (offset < endOffset) {
            while (offset < endOffset && isWhitespace(buffer[offset])) {
                offset++
            }
            if (startsWith(buffer, endOffset, offset, "//")) {
                while (offset < endOffset && buffer[offset] != '\n' && buffer[offset] != '\r') {
                    offset++
                }
                continue
            }
            if (startsWith(buffer, endOffset, offset, "/*")) {
                offset += 2
                while (offset < endOffset - 1 && !startsWith(buffer, endOffset, offset, "*/")) {
                    offset++
                }
                offset = (offset + 2).coerceAtMost(endOffset)
                continue
            }
            break
        }

        if (!startsWith(buffer, endOffset, offset, "OPENQASM")) {
            return Dialect.VERSIONLESS
        }
        offset += "OPENQASM".length
        if (offset < endOffset && isIdentifierPart(buffer[offset])) {
            return Dialect.UNKNOWN
        }
        while (offset < endOffset && isWhitespace(buffer[offset])) {
            offset++
        }

        val versionStart = offset
        while (offset < endOffset && buffer[offset].isDigit()) {
            offset++
        }
        return when (buffer.subSequence(versionStart, offset).toString().toIntOrNull()) {
            2 -> Dialect.OPENQASM_2
            3 -> Dialect.OPENQASM_3
            else -> Dialect.UNKNOWN
        }
    }

    private fun startsWith(offset: Int, text: String): Boolean =
        startsWith(currentBuffer, currentBufferEnd, offset, text)

    private fun startsWithIgnoreCase(offset: Int, text: String): Boolean {
        if (offset + text.length > currentBufferEnd) {
            return false
        }
        return text.indices.all { index -> currentBuffer[offset + index].equals(text[index], ignoreCase = true) }
    }

    private fun charAtOrNull(offset: Int): Char? =
        if (offset in 0 until currentBufferEnd) currentBuffer[offset] else null

    private enum class Dialect(val state: Int, val isOpenQasm3: Boolean = false) {
        UNKNOWN(0),
        VERSIONLESS(1, isOpenQasm3 = true),
        OPENQASM_2(2),
        OPENQASM_3(3, isOpenQasm3 = true),
    }

    private companion object {
        val OPENQASM_2_KEYWORDS = setOf(
            "OPENQASM",
            "include",
            "measure",
            "reset",
            "barrier",
            "if",
        )

        val OPENQASM_3_KEYWORDS = setOf(
            "OPENQASM",
            "include",
            "measure",
            "reset",
            "barrier",
            "delay",
            "box",
            "durationof",
            "if",
            "else",
            "for",
            "while",
            "in",
            "break",
            "continue",
            "end",
            "return",
            "switch",
            "case",
            "default",
            "ctrl",
            "negctrl",
            "inv",
            "pow",
            "pragma",
        )

        val KEYWORDS = OPENQASM_2_KEYWORDS + OPENQASM_3_KEYWORDS

        val OPENQASM_2_DECLARATIONS = setOf(
            "gate",
            "opaque",
        )

        val OPENQASM_3_DECLARATIONS = setOf(
            "gate",
            "extern",
            "def",
            "defcal",
            "defcalgrammar",
            "cal",
            "let",
            "input",
            "output",
            "const",
            "readonly",
            "mutable",
        )

        val DECLARATIONS = OPENQASM_2_DECLARATIONS + OPENQASM_3_DECLARATIONS

        val OPENQASM_2_TYPES = setOf(
            "qreg",
            "creg",
        )

        val OPENQASM_3_TYPES = OPENQASM_2_TYPES + setOf(
            "qubit",
            "bit",
            "int",
            "uint",
            "float",
            "angle",
            "bool",
            "duration",
            "stretch",
            "complex",
            "array",
            "void",
        )

        val TYPES = OPENQASM_2_TYPES + OPENQASM_3_TYPES

        val OPENQASM_2_BUILTIN_GATES = setOf(
            "U",
            "CX",
            "u0",
            "u1",
            "u2",
            "u3",
            "id",
            "x",
            "y",
            "z",
            "h",
            "s",
            "sdg",
            "t",
            "tdg",
            "rx",
            "ry",
            "rz",
            "cx",
            "cy",
            "cz",
            "crz",
            "ch",
            "ccx",
            "swap",
            "cswap",
            "cu1",
            "cu3",
        )

        val OPENQASM_3_BUILTIN_GATES = setOf(
            "U",
            "gphase",
            "p",
            "x",
            "y",
            "z",
            "h",
            "s",
            "sdg",
            "t",
            "tdg",
            "sx",
            "rx",
            "ry",
            "rz",
            "cx",
            "cy",
            "cz",
            "cp",
            "crx",
            "cry",
            "crz",
            "ch",
            "cu",
            "swap",
            "ccx",
            "cswap",
            "CX",
            "phase",
            "cphase",
            "id",
            "u1",
            "u2",
            "u3",
        )

        val BUILTIN_GATES = OPENQASM_2_BUILTIN_GATES + OPENQASM_3_BUILTIN_GATES

        val OPENQASM_2_BUILTIN_CONSTANTS = setOf("pi")
        val OPENQASM_3_BUILTIN_CONSTANTS = setOf("pi", "π", "tau", "τ", "euler", "ℇ", "true", "false")
        val BUILTIN_CONSTANTS = OPENQASM_2_BUILTIN_CONSTANTS + OPENQASM_3_BUILTIN_CONSTANTS

        val OPENQASM_3_ARBITRARY_STRING_KEYWORDS = setOf("include", "defcalgrammar")

        val HORIZONTAL_WHITESPACE = setOf(' ', '\t')

        val OPERATORS = listOf(
            "<<=",
            ">>=",
            "**=",
            "->",
            "==",
            "!=",
            "<=",
            ">=",
            "&&",
            "||",
            "<<",
            ">>",
            "**",
            "++",
            "+=",
            "-=",
            "*=",
            "/=",
            "%=",
            "&=",
            "|=",
            "^=",
        )

        val PUNCTUATION = setOf(';', ',', '.', ':', '(', ')', '[', ']', '{', '}')
        val OPERATOR_CHARACTERS = setOf('+', '-', '*', '/', '%', '^', '&', '|', '~', '!', '=', '<', '>', '@', '#')

        fun isWhitespace(character: Char): Boolean =
            character == '\uFEFF' || Character.isWhitespace(character)

        fun isIdentifierStart(character: Char): Boolean =
            character == '_' || Character.isUnicodeIdentifierStart(character)

        fun isIdentifierPart(character: Char): Boolean =
            character == '_' || Character.isUnicodeIdentifierPart(character)

        fun startsWith(buffer: CharSequence, endOffset: Int, offset: Int, text: String): Boolean {
            if (offset < 0 || offset + text.length > endOffset) {
                return false
            }
            return text.indices.all { index -> buffer[offset + index] == text[index] }
        }
    }
}
