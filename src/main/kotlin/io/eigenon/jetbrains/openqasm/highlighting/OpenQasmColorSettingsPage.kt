package io.eigenon.jetbrains.openqasm.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import io.eigenon.jetbrains.openqasm.OpenQasmIcons
import javax.swing.Icon

class OpenQasmColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = OpenQasmIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = OpenQasmSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = emptyMap()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = ATTRIBUTE_DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = emptyArray()

    override fun getDisplayName(): String = "OpenQASM"

    private companion object {
        val ATTRIBUTE_DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keywords", OpenQasmSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Declarations", OpenQasmSyntaxHighlighter.DECLARATION),
            AttributesDescriptor("Types", OpenQasmSyntaxHighlighter.TYPE),
            AttributesDescriptor("Built-in gates", OpenQasmSyntaxHighlighter.BUILTIN_GATE),
            AttributesDescriptor("Built-in constants", OpenQasmSyntaxHighlighter.BUILTIN_CONSTANT),
            AttributesDescriptor("Identifiers", OpenQasmSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Version declaration", OpenQasmSyntaxHighlighter.VERSION),
            AttributesDescriptor("Numbers", OpenQasmSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Strings", OpenQasmSyntaxHighlighter.STRING),
            AttributesDescriptor("Directive text", OpenQasmSyntaxHighlighter.DIRECTIVE_TEXT),
            AttributesDescriptor("Line comments", OpenQasmSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("Block comments", OpenQasmSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Operators", OpenQasmSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Punctuation", OpenQasmSyntaxHighlighter.PUNCTUATION),
            AttributesDescriptor("Invalid characters", OpenQasmSyntaxHighlighter.BAD_CHARACTER),
        )

        val DEMO_TEXT = """
            OPENQASM 3.1;
            include "stdgates.inc";

            // Prepare a Bell state.
            qubit[2] q;
            bit[2] result;
            const float[64] theta = pi / 2;

            h q[0];
            cx q[0], q[1];
            rz(theta) q[1];
            result = measure q;
        """.trimIndent()
    }
}
