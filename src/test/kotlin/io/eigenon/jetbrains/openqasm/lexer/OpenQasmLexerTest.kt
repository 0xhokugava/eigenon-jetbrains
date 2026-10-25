package io.eigenon.jetbrains.openqasm.lexer

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase

class OpenQasmLexerTest : TestCase() {
    fun testOpenQasm2Tokens() {
        val tokens = lex(
            """
            OPENQASM 2.0;
            include "qelib1.inc";
            qreg q[2];
            creg c[2];
            gate bell a, b { h a; CX a, b; }
            U(pi / 2, 0, pi) q[0];
            CX q[0], q[1];
            // Measure the result.
            measure q -> c;
            """.trimIndent(),
        )

        assertToken(tokens, OpenQasmTokenTypes.KEYWORD, "OPENQASM")
        assertToken(tokens, OpenQasmTokenTypes.VERSION, "2.0")
        assertToken(tokens, OpenQasmTokenTypes.TYPE, "qreg")
        assertToken(tokens, OpenQasmTokenTypes.DECLARATION, "gate")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_GATE, "U")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_GATE, "CX")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_CONSTANT, "pi")
        assertToken(tokens, OpenQasmTokenTypes.LINE_COMMENT, "// Measure the result.")
        assertToken(tokens, OpenQasmTokenTypes.OPERATOR, "->")
    }

    fun testOpenQasm3Tokens() {
        val tokens = lex(
            """
            /* OpenQASM 3 example. */
            OPENQASM 3.1;
            include "stdgates.inc";
            qubit[2] q;
            bit[2] result;
            const float[64] theta = π / 2;
            h q[0];
            cx q[0], q[1];
            result = measure q;
            """.trimIndent(),
        )

        assertToken(tokens, OpenQasmTokenTypes.BLOCK_COMMENT, "/* OpenQASM 3 example. */")
        assertToken(tokens, OpenQasmTokenTypes.VERSION, "3.1")
        assertToken(tokens, OpenQasmTokenTypes.TYPE, "qubit")
        assertToken(tokens, OpenQasmTokenTypes.TYPE, "float")
        assertToken(tokens, OpenQasmTokenTypes.DECLARATION, "const")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_CONSTANT, "π")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_GATE, "h")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_GATE, "cx")
    }

    fun testOpenQasm2DoesNotTreatBlockSyntaxAsAComment() {
        val tokens = lex("OPENQASM 2.0; /* not valid in OpenQASM 2 */")

        assertFalse(tokens.any { it.type == OpenQasmTokenTypes.BLOCK_COMMENT })
        assertEquals(4, tokens.count { it.type == OpenQasmTokenTypes.OPERATOR && it.text in setOf("/", "*") })
    }

    fun testOpenQasm2KeepsOpenQasm3WordsAsIdentifiers() {
        val tokens = lex("OPENQASM 2.0; qubit for const;")

        assertToken(tokens, OpenQasmTokenTypes.IDENTIFIER, "qubit")
        assertToken(tokens, OpenQasmTokenTypes.IDENTIFIER, "for")
        assertToken(tokens, OpenQasmTokenTypes.IDENTIFIER, "const")
    }

    fun testOpenQasm3SupportsSingleQuotedArbitraryStrings() {
        val tokens = lex(
            "OPENQASM 3.1; include 'stdgates.inc'; defcalgrammar 'openpulse';",
        )

        assertToken(tokens, OpenQasmTokenTypes.STRING, "'stdgates.inc'")
        assertToken(tokens, OpenQasmTokenTypes.STRING, "'openpulse'")
    }

    fun testVersionlessOpenQasm3SupportsSingleQuotedArbitraryStrings() {
        val tokens = lex("include 'stdgates.inc'; defcalgrammar 'openpulse'; qubit q;")

        assertToken(tokens, OpenQasmTokenTypes.STRING, "'stdgates.inc'")
        assertToken(tokens, OpenQasmTokenTypes.STRING, "'openpulse'")
        assertToken(tokens, OpenQasmTokenTypes.TYPE, "qubit")
    }

    fun testUnsupportedExplicitVersionIsNotAssumedToBeOpenQasm3() {
        val tokens = lex("OPENQASM 4.0; include 'future.inc';")

        assertFalse(tokens.any { it.type == OpenQasmTokenTypes.STRING && it.text.startsWith("'") })
        assertEquals(2, tokens.count { it.type == TokenType.BAD_CHARACTER && it.text == "'" })
    }

    fun testOpenQasm3SupportsLegacyRegisterTypesAndGlobalPhase() {
        val tokens = lex("OPENQASM 3.1; qreg q[2]; creg c[2]; gphase(pi / 2);")

        assertToken(tokens, OpenQasmTokenTypes.TYPE, "qreg")
        assertToken(tokens, OpenQasmTokenTypes.TYPE, "creg")
        assertToken(tokens, OpenQasmTokenTypes.BUILTIN_GATE, "gphase")
    }

    fun testOpenQasm2DoesNotTreatGlobalPhaseAsABuiltinGate() {
        val tokens = lex("OPENQASM 2.0; gphase;")

        assertToken(tokens, OpenQasmTokenTypes.IDENTIFIER, "gphase")
    }

    fun testBuiltinGateSetsFollowTheDialect() {
        val openQasm2Tokens = lex("OPENQASM 2.0; u0(1) q; cu1(1) a, b; cu3(1, 2, 3) a, b; phase q;")
        val openQasm3Tokens = lex(
            "OPENQASM 3.1; phase(1) q; cphase(1) a, b; id q; cu(1, 2, 3, 4) a, b; " +
                "u0(1) q; cu1(1) a, b; cu3(1, 2, 3) a, b;",
        )

        assertToken(openQasm2Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "u0")
        assertToken(openQasm2Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "cu1")
        assertToken(openQasm2Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "cu3")
        assertToken(openQasm2Tokens, OpenQasmTokenTypes.IDENTIFIER, "phase")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "phase")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "cphase")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "id")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.BUILTIN_GATE, "cu")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.IDENTIFIER, "u0")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.IDENTIFIER, "cu1")
        assertToken(openQasm3Tokens, OpenQasmTokenTypes.IDENTIFIER, "cu3")
    }

    fun testOpenQasm3DirectivePayloadsAreOpaque() {
        val tokens = lex(
            """
            OPENQASM 3.1;
            pragma simulator ? `custom`
            #pragma legacy ? `payload`
            @vendor ? `metadata`
            qubit q;
            """.trimIndent(),
        )

        assertToken(tokens, OpenQasmTokenTypes.KEYWORD, "pragma")
        assertToken(tokens, OpenQasmTokenTypes.KEYWORD, "#pragma")
        assertToken(tokens, OpenQasmTokenTypes.KEYWORD, "@vendor")
        assertToken(tokens, OpenQasmTokenTypes.DIRECTIVE_TEXT, "simulator ? `custom`")
        assertToken(tokens, OpenQasmTokenTypes.DIRECTIVE_TEXT, "legacy ? `payload`")
        assertToken(tokens, OpenQasmTokenTypes.DIRECTIVE_TEXT, "? `metadata`")
        assertFalse(tokens.any { it.type == TokenType.BAD_CHARACTER })
    }

    fun testSingleQuotedStringsAreRejectedOutsideOpenQasm3ArbitraryStringContexts() {
        val openQasm2Tokens = lex("OPENQASM 2.0; include 'qelib1.inc';")
        val openQasm3Tokens = lex("OPENQASM 3.1; bit[3] value = '101';")

        assertFalse(openQasm2Tokens.any { it.type == OpenQasmTokenTypes.STRING && it.text.startsWith("'") })
        assertFalse(openQasm3Tokens.any { it.type == OpenQasmTokenTypes.STRING && it.text.startsWith("'") })
        assertEquals(2, openQasm2Tokens.count { it.type == TokenType.BAD_CHARACTER && it.text == "'" })
        assertEquals(2, openQasm3Tokens.count { it.type == TokenType.BAD_CHARACTER && it.text == "'" })
    }

    fun testMalformedInputAlwaysMakesProgress() {
        val source = "OPENQASM 3; \"unterminated\n/* unterminated block \u0001"
        val tokens = lex(source)

        assertTrue(tokens.isNotEmpty())
        assertTrue(tokens.all { it.text.isNotEmpty() })
        assertTrue(tokens.size <= source.length)
    }

    fun testUnknownCharacterIsReported() {
        val tokens = lex("OPENQASM 3; `")

        assertToken(tokens, TokenType.BAD_CHARACTER, "`")
    }

    private fun lex(source: String): List<Token> {
        val lexer = OpenQasmLexer()
        val tokens = mutableListOf<Token>()
        lexer.start(source)

        while (lexer.tokenType != null) {
            assertTrue("The lexer must always advance", lexer.tokenEnd > lexer.tokenStart)
            tokens += Token(
                type = requireNotNull(lexer.tokenType),
                text = source.substring(lexer.tokenStart, lexer.tokenEnd),
            )
            assertTrue("The lexer emitted too many tokens", tokens.size <= source.length)
            lexer.advance()
        }

        return tokens
    }

    private fun assertToken(tokens: List<Token>, type: IElementType, text: String) {
        assertTrue(
            "Expected token $type with text '$text', but got $tokens",
            tokens.any { it.type == type && it.text == text },
        )
    }

    private data class Token(val type: IElementType, val text: String)
}
