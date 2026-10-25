package io.eigenon.jetbrains.executable

import junit.framework.TestCase
import java.nio.file.Path

class EigenonExecutableLocatorTest : TestCase() {
    fun testConfiguredExecutableTakesPrecedenceOverPath() {
        val configured = Path.of("/opt/eigenon/bin/eigenon")
        val fromPath = Path.of("/usr/local/bin/eigenon")
        val locator = locator(
            pathValue = "/usr/local/bin",
            states = mapOf(configured to ExecutablePathState.VALID, fromPath to ExecutablePathState.VALID),
        )

        assertEquals(
            EigenonExecutableResolution.Found(configured, EigenonExecutableSource.CONFIGURED),
            locator.resolve(configured.toString()),
        )
    }

    fun testInvalidConfiguredPathDoesNotSilentlyFallBackToPath() {
        val configured = Path.of("/missing/eigenon")
        val fromPath = Path.of("/usr/local/bin/eigenon")
        val locator = locator(
            pathValue = "/usr/local/bin",
            states = mapOf(fromPath to ExecutablePathState.VALID),
        )

        assertEquals(
            EigenonExecutableResolution.InvalidConfiguredPath(
                configured.toString(),
                InvalidExecutableReason.DOES_NOT_EXIST,
            ),
            locator.resolve(configured.toString()),
        )
    }

    fun testBlankConfigurationDiscoversExecutableOnPath() {
        val fromPath = Path.of("/usr/local/bin/eigenon")
        val locator = locator(
            pathValue = "/usr/bin:/usr/local/bin",
            states = mapOf(fromPath to ExecutablePathState.VALID),
        )

        assertEquals(
            EigenonExecutableResolution.Found(fromPath, EigenonExecutableSource.PATH),
            locator.resolve(""),
        )
    }

    fun testBlankConfigurationReportsNotFound() {
        val locator = locator(pathValue = "/usr/bin:/usr/local/bin")

        assertEquals(EigenonExecutableResolution.NotFound, locator.resolve(""))
    }

    fun testRelativeConfiguredPathIsRejected() {
        val locator = locator(pathValue = null)

        assertEquals(
            EigenonExecutableResolution.InvalidConfiguredPath(
                "bin/eigenon",
                InvalidExecutableReason.NOT_ABSOLUTE,
            ),
            locator.resolve("bin/eigenon"),
        )
    }

    fun testConfiguredDirectoryIsRejected() {
        val configured = Path.of("/opt/eigenon")
        val locator = locator(
            pathValue = null,
            states = mapOf(configured to ExecutablePathState.NOT_A_FILE),
        )

        assertEquals(
            EigenonExecutableResolution.InvalidConfiguredPath(
                configured.toString(),
                InvalidExecutableReason.NOT_A_FILE,
            ),
            locator.resolve(configured.toString()),
        )
    }

    fun testNonExecutableConfiguredFileIsRejected() {
        val configured = Path.of("/opt/eigenon/bin/eigenon")
        val locator = locator(
            pathValue = null,
            states = mapOf(configured to ExecutablePathState.NOT_EXECUTABLE),
        )

        assertEquals(
            EigenonExecutableResolution.InvalidConfiguredPath(
                configured.toString(),
                InvalidExecutableReason.NOT_EXECUTABLE,
            ),
            locator.resolve(configured.toString()),
        )
    }

    fun testWindowsPathExtensionsAreUsedDuringDiscovery() {
        val fromPath = Path.of("/tools/eigenon.EXE")
        val locator = locator(
            pathValue = "/other;/tools",
            pathExtensionsValue = ".COM;.EXE",
            isWindows = true,
            pathSeparator = ";",
            states = mapOf(fromPath to ExecutablePathState.VALID),
        )

        assertEquals(
            EigenonExecutableResolution.Found(fromPath, EigenonExecutableSource.PATH),
            locator.resolve(""),
        )
    }

    fun testEmptyPathEntryIsIgnored() {
        val currentDirectoryExecutable = Path.of("eigenon").toAbsolutePath().normalize()
        val locator = locator(
            pathValue = ":/usr/bin",
            states = mapOf(currentDirectoryExecutable to ExecutablePathState.VALID),
        )

        assertEquals(EigenonExecutableResolution.NotFound, locator.resolve(""))
    }

    private fun locator(
        pathValue: String?,
        pathExtensionsValue: String? = null,
        isWindows: Boolean = false,
        pathSeparator: String = ":",
        states: Map<Path, ExecutablePathState> = emptyMap(),
    ) = EigenonExecutableLocator(
        pathValue = pathValue,
        pathExtensionsValue = pathExtensionsValue,
        isWindows = isWindows,
        pathSeparator = pathSeparator,
        inspectPath = { states[it] ?: ExecutablePathState.DOES_NOT_EXIST },
    )
}
