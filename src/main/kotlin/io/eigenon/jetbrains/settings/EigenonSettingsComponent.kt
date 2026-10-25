package io.eigenon.jetbrains.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import io.eigenon.jetbrains.executable.EigenonExecutableResolution
import io.eigenon.jetbrains.executable.EigenonExecutableResolver
import io.eigenon.jetbrains.executable.EigenonExecutableSource
import io.eigenon.jetbrains.executable.InvalidExecutableReason
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

internal class EigenonSettingsComponent(
    private val resolver: EigenonExecutableResolver = EigenonExecutableResolver.getInstance(),
) {
    private val executablePathField = TextFieldWithBrowseButton()
    private val statusLabel = JBLabel()
    private val autoDetectButton = JButton("Auto-detect from PATH")
    private var validationGeneration = 0
    private var disposed = false

    val panel: JPanel

    var executablePath: String
        get() = executablePathField.text.trim()
        set(value) {
            executablePathField.text = value
            refreshStatus()
        }

    val preferredFocusedComponent: JComponent
        get() = executablePathField

    init {
        val descriptor = FileChooserDescriptorFactory
            .createSingleFileOrExecutableAppDescriptor()
            .withTitle("Select Eigenon Executable")
            .withDescription("Select the Eigenon command-line executable.")
        executablePathField.addBrowseFolderListener(null, descriptor)
        executablePathField.addDocumentListener(
            object : DocumentAdapter() {
                override fun textChanged(event: DocumentEvent) {
                    refreshStatus()
                }
            },
        )

        autoDetectButton.addActionListener {
            executablePathField.text = ""
        }

        val actionsPanel = JPanel(FlowLayout(FlowLayout.LEADING, 0, 0)).apply {
            add(autoDetectButton)
        }

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Eigenon executable:"), executablePathField, 1, false)
            .addComponent(actionsPanel, 1)
            .addComponent(
                JBLabel("Leave the path empty to discover the eigenon executable from PATH."),
                1,
            )
            .addComponent(statusLabel, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun dispose() {
        disposed = true
        validationGeneration++
    }

    private fun refreshStatus() {
        val configuredPath = executablePath
        val generation = ++validationGeneration
        statusLabel.text = "Checking Eigenon executable…"

        resolver.resolveAsync(configuredPath).whenComplete { resolution, error ->
            ApplicationManager.getApplication().invokeLater {
                if (disposed || generation != validationGeneration) {
                    return@invokeLater
                }

                statusLabel.text = if (error == null) {
                    describe(resolution)
                } else {
                    "Unable to check the Eigenon executable."
                }
            }
        }
    }

    private fun describe(resolution: EigenonExecutableResolution): String = when (resolution) {
        is EigenonExecutableResolution.Found -> when (resolution.source) {
            EigenonExecutableSource.CONFIGURED ->
                "Valid executable: ${resolution.path}"

            EigenonExecutableSource.PATH ->
                "Detected on PATH: ${resolution.path}"
        }

        is EigenonExecutableResolution.InvalidConfiguredPath -> when (resolution.reason) {
            InvalidExecutableReason.INVALID_PATH -> "The configured path is not valid."
            InvalidExecutableReason.NOT_ABSOLUTE -> "Choose an absolute path to the Eigenon executable."
            InvalidExecutableReason.DOES_NOT_EXIST -> "The configured path does not exist."
            InvalidExecutableReason.NOT_A_FILE -> "The configured path is not a file."
            InvalidExecutableReason.NOT_EXECUTABLE -> "The configured file is not executable."
        }

        EigenonExecutableResolution.NotFound ->
            "Eigenon was not found on PATH. Choose the executable manually or add it to PATH."
    }
}
