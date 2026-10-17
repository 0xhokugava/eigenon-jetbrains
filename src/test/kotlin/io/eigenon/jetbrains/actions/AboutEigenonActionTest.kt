package io.eigenon.jetbrains.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AboutEigenonActionTest : BasePlatformTestCase() {

    fun testActionIsRegistered() {
        val action = ActionManager.getInstance().getAction(ACTION_ID)

        assertNotNull(action)
        assertEquals(AboutEigenonAction::class.java, action.javaClass)
        assertEquals("About Eigenon Quantum Toolkit", action.templatePresentation.text)
    }

    private companion object {
        const val ACTION_ID = "io.eigenon.jetbrains.actions.AboutEigenonAction"
    }
}
