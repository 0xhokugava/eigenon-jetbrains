package io.eigenon.jetbrains.executable

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.concurrency.AppExecutorUtil
import io.eigenon.jetbrains.settings.EigenonSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

enum class EigenonExecutableSource {
    CONFIGURED,
    PATH,
}

enum class InvalidExecutableReason {
    INVALID_PATH,
    NOT_ABSOLUTE,
    DOES_NOT_EXIST,
    NOT_A_FILE,
    NOT_EXECUTABLE,
}

sealed interface EigenonExecutableResolution {
    data class Found(
        val path: Path,
        val source: EigenonExecutableSource,
    ) : EigenonExecutableResolution

    data class InvalidConfiguredPath(
        val configuredPath: String,
        val reason: InvalidExecutableReason,
    ) : EigenonExecutableResolution

    data object NotFound : EigenonExecutableResolution
}

@Service(Service.Level.APP)
class EigenonExecutableResolver {
    private val locator = EigenonExecutableLocator()

    fun resolve(): EigenonExecutableResolution =
        resolve(EigenonSettings.getInstance().executablePath)

    fun resolve(configuredPath: String): EigenonExecutableResolution =
        locator.resolve(configuredPath)

    fun resolveAsync(configuredPath: String = EigenonSettings.getInstance().executablePath):
        CompletableFuture<EigenonExecutableResolution> =
        CompletableFuture.supplyAsync(
            { resolve(configuredPath) },
            AppExecutorUtil.getAppExecutorService(),
        )

    companion object {
        fun getInstance(): EigenonExecutableResolver =
            ApplicationManager.getApplication().getService(EigenonExecutableResolver::class.java)
    }
}

internal class EigenonExecutableLocator(
    private val pathValue: String? = System.getenv("PATH"),
    private val pathExtensionsValue: String? = System.getenv("PATHEXT"),
    private val isWindows: Boolean = SystemInfo.isWindows,
    private val pathSeparator: String = File.pathSeparator,
    private val inspectPath: (Path) -> ExecutablePathState = ::inspectExecutablePath,
) {
    fun resolve(configuredPath: String): EigenonExecutableResolution {
        val normalizedConfiguredPath = configuredPath.trim()
        if (normalizedConfiguredPath.isNotEmpty()) {
            return resolveConfiguredPath(normalizedConfiguredPath)
        }

        return findOnPath()
            ?.let { EigenonExecutableResolution.Found(it, EigenonExecutableSource.PATH) }
            ?: EigenonExecutableResolution.NotFound
    }

    private fun resolveConfiguredPath(configuredPath: String): EigenonExecutableResolution {
        val path = try {
            Path.of(configuredPath)
        } catch (_: InvalidPathException) {
            return invalid(configuredPath, InvalidExecutableReason.INVALID_PATH)
        }

        if (!path.isAbsolute) {
            return invalid(configuredPath, InvalidExecutableReason.NOT_ABSOLUTE)
        }

        val normalizedPath = path.normalize()
        val reason = when (inspectPath(normalizedPath)) {
            ExecutablePathState.VALID -> null
            ExecutablePathState.DOES_NOT_EXIST -> InvalidExecutableReason.DOES_NOT_EXIST
            ExecutablePathState.NOT_A_FILE -> InvalidExecutableReason.NOT_A_FILE
            ExecutablePathState.NOT_EXECUTABLE -> InvalidExecutableReason.NOT_EXECUTABLE
        }

        return if (reason == null) {
            EigenonExecutableResolution.Found(normalizedPath, EigenonExecutableSource.CONFIGURED)
        } else {
            invalid(configuredPath, reason)
        }
    }

    private fun findOnPath(): Path? {
        val path = pathValue?.takeIf { it.isNotBlank() } ?: return null
        val candidateNames = candidateNames()

        return path
            .split(pathSeparator)
            .asSequence()
            .map(String::trim)
            .map { it.removeSurrounding("\"") }
            .filter(String::isNotEmpty)
            .mapNotNull(::safePath)
            .flatMap { directory -> candidateNames.asSequence().map(directory::resolve) }
            .map { it.toAbsolutePath().normalize() }
            .distinct()
            .firstOrNull { inspectPath(it) == ExecutablePathState.VALID }
    }

    private fun candidateNames(): List<String> {
        if (!isWindows) {
            return listOf(EXECUTABLE_NAME)
        }

        val extensions = pathExtensionsValue
            ?.split(';')
            .orEmpty()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { if (it.startsWith('.')) it else ".$it" }

        return buildList {
            add(EXECUTABLE_NAME)
            extensions.forEach { add(EXECUTABLE_NAME + it) }
        }.distinct()
    }

    private fun safePath(value: String): Path? = try {
        Path.of(value)
    } catch (_: InvalidPathException) {
        null
    }

    private fun invalid(
        configuredPath: String,
        reason: InvalidExecutableReason,
    ) = EigenonExecutableResolution.InvalidConfiguredPath(configuredPath, reason)

    private companion object {
        const val EXECUTABLE_NAME = "eigenon"
    }
}

internal enum class ExecutablePathState {
    VALID,
    DOES_NOT_EXIST,
    NOT_A_FILE,
    NOT_EXECUTABLE,
}

private fun inspectExecutablePath(path: Path): ExecutablePathState = when {
    !Files.exists(path) -> ExecutablePathState.DOES_NOT_EXIST
    !Files.isRegularFile(path) -> ExecutablePathState.NOT_A_FILE
    !Files.isExecutable(path) -> ExecutablePathState.NOT_EXECUTABLE
    else -> ExecutablePathState.VALID
}
