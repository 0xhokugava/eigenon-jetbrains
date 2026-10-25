package io.eigenon.jetbrains.openqasm

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object OpenQasmFileType : LanguageFileType(OpenQasmLanguage) {
    override fun getName(): String = "OpenQASM"

    override fun getDescription(): String = "OpenQASM quantum assembly language"

    override fun getDefaultExtension(): String = "qasm"

    override fun getIcon(): Icon = OpenQasmIcons.FILE
}
