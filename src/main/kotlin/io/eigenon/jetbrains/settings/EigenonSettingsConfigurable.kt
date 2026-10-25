package io.eigenon.jetbrains.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class EigenonSettingsConfigurable : Configurable {
    private var component: EigenonSettingsComponent? = null

    override fun getDisplayName(): String = "Eigenon"

    override fun getPreferredFocusedComponent(): JComponent? =
        component?.preferredFocusedComponent

    override fun createComponent(): JComponent {
        val settingsComponent = EigenonSettingsComponent()
        component = settingsComponent
        return settingsComponent.panel
    }

    override fun isModified(): Boolean = component?.let {
        it.executablePath != EigenonSettings.getInstance().executablePath
    } ?: false

    override fun apply() {
        component?.let { EigenonSettings.getInstance().executablePath = it.executablePath }
    }

    override fun reset() {
        component?.executablePath = EigenonSettings.getInstance().executablePath
    }

    override fun disposeUIResources() {
        component?.dispose()
        component = null
    }
}
