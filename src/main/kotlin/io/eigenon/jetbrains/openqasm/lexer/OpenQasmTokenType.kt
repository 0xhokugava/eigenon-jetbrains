package io.eigenon.jetbrains.openqasm.lexer

import com.intellij.psi.tree.IElementType
import io.eigenon.jetbrains.openqasm.OpenQasmLanguage

class OpenQasmTokenType(debugName: String) : IElementType(debugName, OpenQasmLanguage)
