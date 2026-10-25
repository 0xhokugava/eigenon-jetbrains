package io.eigenon.jetbrains.settings

import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import junit.framework.TestCase

class EigenonSettingsTest : TestCase() {
    fun testExecutablePathIsLocalToTheIde() {
        val annotation = EigenonSettings::class.java.getAnnotation(State::class.java)

        assertNotNull(annotation)
        assertEquals("eigenon.xml", annotation.storages.single().value)
        assertEquals(RoamingType.DISABLED, annotation.storages.single().roamingType)
    }

    fun testDefaultConfigurationUsesPathDiscovery() {
        assertEquals("", EigenonSettings.SettingsState().executablePath)
    }
}
