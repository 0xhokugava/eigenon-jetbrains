package io.eigenon.jetbrains.openqasm.lexer

object OpenQasmTokenTypes {
    @JvmField
    val KEYWORD = OpenQasmTokenType("OPENQASM_KEYWORD")

    @JvmField
    val DECLARATION = OpenQasmTokenType("OPENQASM_DECLARATION")

    @JvmField
    val TYPE = OpenQasmTokenType("OPENQASM_TYPE")

    @JvmField
    val BUILTIN_GATE = OpenQasmTokenType("OPENQASM_BUILTIN_GATE")

    @JvmField
    val BUILTIN_CONSTANT = OpenQasmTokenType("OPENQASM_BUILTIN_CONSTANT")

    @JvmField
    val IDENTIFIER = OpenQasmTokenType("OPENQASM_IDENTIFIER")

    @JvmField
    val VERSION = OpenQasmTokenType("OPENQASM_VERSION")

    @JvmField
    val NUMBER = OpenQasmTokenType("OPENQASM_NUMBER")

    @JvmField
    val STRING = OpenQasmTokenType("OPENQASM_STRING")

    @JvmField
    val DIRECTIVE_TEXT = OpenQasmTokenType("OPENQASM_DIRECTIVE_TEXT")

    @JvmField
    val LINE_COMMENT = OpenQasmTokenType("OPENQASM_LINE_COMMENT")

    @JvmField
    val BLOCK_COMMENT = OpenQasmTokenType("OPENQASM_BLOCK_COMMENT")

    @JvmField
    val OPERATOR = OpenQasmTokenType("OPENQASM_OPERATOR")

    @JvmField
    val PUNCTUATION = OpenQasmTokenType("OPENQASM_PUNCTUATION")
}
