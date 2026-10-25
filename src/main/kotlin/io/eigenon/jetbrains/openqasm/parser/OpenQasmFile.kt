package io.eigenon.jetbrains.openqasm.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import io.eigenon.jetbrains.openqasm.OpenQasmFileType
import io.eigenon.jetbrains.openqasm.OpenQasmLanguage

class OpenQasmFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, OpenQasmLanguage) {
    override fun getFileType(): OpenQasmFileType = OpenQasmFileType

    override fun toString(): String = "OpenQASM File"
}
