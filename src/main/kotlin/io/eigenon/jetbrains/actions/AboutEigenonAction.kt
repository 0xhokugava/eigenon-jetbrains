package io.eigenon.jetbrains.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

class AboutEigenonAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        Messages.showInfoMessage(
            event.project,
            "Eigenon Quantum Toolkit is ready for its first quantum development features.",
            "Eigenon Quantum Toolkit",
        )
    }
}
