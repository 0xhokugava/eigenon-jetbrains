package io.eigenon.jetbrains.openqasm.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import io.eigenon.jetbrains.openqasm.OpenQasmLanguage
import io.eigenon.jetbrains.openqasm.lexer.OpenQasmLexer
import io.eigenon.jetbrains.openqasm.lexer.OpenQasmTokenTypes

class OpenQasmParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = OpenQasmLexer()

    override fun createParser(project: Project?): PsiParser = OpenQasmParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun getCommentTokens(): TokenSet = TokenSet.create(
        OpenQasmTokenTypes.LINE_COMMENT,
        OpenQasmTokenTypes.BLOCK_COMMENT,
    )

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(OpenQasmTokenTypes.STRING)

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = OpenQasmFile(viewProvider)

    private companion object {
        val FILE = IFileElementType("OPENQASM_FILE", OpenQasmLanguage)
    }
}
