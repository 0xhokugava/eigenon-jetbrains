package io.eigenon.jetbrains.openqasm.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import io.eigenon.jetbrains.openqasm.lexer.OpenQasmLexer
import io.eigenon.jetbrains.openqasm.lexer.OpenQasmTokenTypes

class OpenQasmSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = OpenQasmLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        OpenQasmTokenTypes.KEYWORD -> pack(KEYWORD)
        OpenQasmTokenTypes.DECLARATION -> pack(DECLARATION)
        OpenQasmTokenTypes.TYPE -> pack(TYPE)
        OpenQasmTokenTypes.BUILTIN_GATE -> pack(BUILTIN_GATE)
        OpenQasmTokenTypes.BUILTIN_CONSTANT -> pack(BUILTIN_CONSTANT)
        OpenQasmTokenTypes.IDENTIFIER -> pack(IDENTIFIER)
        OpenQasmTokenTypes.VERSION -> pack(VERSION)
        OpenQasmTokenTypes.NUMBER -> pack(NUMBER)
        OpenQasmTokenTypes.STRING -> pack(STRING)
        OpenQasmTokenTypes.DIRECTIVE_TEXT -> pack(DIRECTIVE_TEXT)
        OpenQasmTokenTypes.LINE_COMMENT -> pack(LINE_COMMENT)
        OpenQasmTokenTypes.BLOCK_COMMENT -> pack(BLOCK_COMMENT)
        OpenQasmTokenTypes.OPERATOR -> pack(OPERATOR)
        OpenQasmTokenTypes.PUNCTUATION -> pack(PUNCTUATION)
        TokenType.BAD_CHARACTER -> pack(BAD_CHARACTER)
        else -> emptyArray()
    }

    companion object {
        @JvmField
        val KEYWORD: TextAttributesKey = key("OPENQASM_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

        @JvmField
        val DECLARATION: TextAttributesKey = key("OPENQASM_DECLARATION", DefaultLanguageHighlighterColors.KEYWORD)

        @JvmField
        val TYPE: TextAttributesKey = key("OPENQASM_TYPE", DefaultLanguageHighlighterColors.KEYWORD)

        @JvmField
        val BUILTIN_GATE: TextAttributesKey = key("OPENQASM_BUILTIN_GATE", DefaultLanguageHighlighterColors.FUNCTION_CALL)

        @JvmField
        val BUILTIN_CONSTANT: TextAttributesKey = key("OPENQASM_BUILTIN_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)

        @JvmField
        val IDENTIFIER: TextAttributesKey = key("OPENQASM_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)

        @JvmField
        val VERSION: TextAttributesKey = key("OPENQASM_VERSION", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val NUMBER: TextAttributesKey = key("OPENQASM_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

        @JvmField
        val STRING: TextAttributesKey = key("OPENQASM_STRING", DefaultLanguageHighlighterColors.STRING)

        @JvmField
        val DIRECTIVE_TEXT: TextAttributesKey = key("OPENQASM_DIRECTIVE_TEXT", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val LINE_COMMENT: TextAttributesKey = key("OPENQASM_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        @JvmField
        val BLOCK_COMMENT: TextAttributesKey = key("OPENQASM_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)

        @JvmField
        val OPERATOR: TextAttributesKey = key("OPENQASM_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)

        @JvmField
        val PUNCTUATION: TextAttributesKey = key("OPENQASM_PUNCTUATION", DefaultLanguageHighlighterColors.PARENTHESES)

        @JvmField
        val BAD_CHARACTER: TextAttributesKey = key("OPENQASM_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private fun key(externalName: String, fallback: TextAttributesKey): TextAttributesKey =
            TextAttributesKey.createTextAttributesKey(externalName, fallback)
    }
}
