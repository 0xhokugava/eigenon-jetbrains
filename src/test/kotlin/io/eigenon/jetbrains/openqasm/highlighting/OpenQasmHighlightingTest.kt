package io.eigenon.jetbrains.openqasm.highlighting

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.eigenon.jetbrains.openqasm.OpenQasmFileType
import io.eigenon.jetbrains.openqasm.OpenQasmLanguage
import io.eigenon.jetbrains.openqasm.lexer.OpenQasmTokenTypes
import java.nio.file.Path

class OpenQasmHighlightingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String =
        Path.of("src", "test", "testData", "highlighting").toAbsolutePath().toString()

    fun testQasmExtensionUsesOpenQasmFileType() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("qasm")

        assertSame(OpenQasmFileType, fileType)
    }

    fun testOpenQasm2FixtureIsHighlighted() {
        val tokenTypes = highlightFixture("openqasm2.qasm")

        assertContainsElements(
            tokenTypes,
            OpenQasmTokenTypes.KEYWORD,
            OpenQasmTokenTypes.DECLARATION,
            OpenQasmTokenTypes.VERSION,
            OpenQasmTokenTypes.TYPE,
            OpenQasmTokenTypes.BUILTIN_GATE,
            OpenQasmTokenTypes.BUILTIN_CONSTANT,
            OpenQasmTokenTypes.LINE_COMMENT,
            OpenQasmTokenTypes.OPERATOR,
        )
    }

    fun testOpenQasm3FixtureIsHighlighted() {
        val tokenTypes = highlightFixture("openqasm3.qasm")

        assertContainsElements(
            tokenTypes,
            OpenQasmTokenTypes.KEYWORD,
            OpenQasmTokenTypes.DECLARATION,
            OpenQasmTokenTypes.VERSION,
            OpenQasmTokenTypes.TYPE,
            OpenQasmTokenTypes.BUILTIN_GATE,
            OpenQasmTokenTypes.BUILTIN_CONSTANT,
            OpenQasmTokenTypes.DIRECTIVE_TEXT,
            OpenQasmTokenTypes.LINE_COMMENT,
            OpenQasmTokenTypes.BLOCK_COMMENT,
            OpenQasmTokenTypes.OPERATOR,
        )
    }

    private fun highlightFixture(fileName: String): Set<IElementType> {
        val psiFile = myFixture.configureByFile(fileName)
        assertSame(OpenQasmLanguage, psiFile.language)

        val highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(
            psiFile.language,
            project,
            psiFile.virtualFile,
        )
        assertNotNull(highlighter)

        val lexer = requireNotNull(highlighter).highlightingLexer
        val tokenTypes = mutableSetOf<IElementType>()
        lexer.start(psiFile.text)
        while (lexer.tokenType != null) {
            val tokenType = requireNotNull(lexer.tokenType)
            tokenTypes += tokenType
            assertTrue(
                "Token $tokenType must have configured text attributes",
                highlighter.getTokenHighlights(tokenType).isNotEmpty() || tokenType.toString() == "WHITE_SPACE",
            )
            lexer.advance()
        }
        return tokenTypes
    }
}
