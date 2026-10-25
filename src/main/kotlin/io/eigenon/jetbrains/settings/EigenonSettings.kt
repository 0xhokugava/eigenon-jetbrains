package io.eigenon.jetbrains.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "io.eigenon.jetbrains.settings.EigenonSettings",
    storages = [Storage(value = "eigenon.xml", roamingType = RoamingType.DISABLED)],
    category = SettingsCategory.TOOLS,
)
class EigenonSettings : SerializablePersistentStateComponent<EigenonSettings.SettingsState>(SettingsState()) {
    var executablePath: String
        get() = state.executablePath
        set(value) {
            updateState { it.copy(executablePath = value.trim()) }
        }

    data class SettingsState(
        @JvmField val executablePath: String = "",
    )

    companion object {
        fun getInstance(): EigenonSettings =
            ApplicationManager.getApplication().getService(EigenonSettings::class.java)
    }
}
