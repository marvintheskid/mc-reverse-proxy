package gbx.proxy.scripting

import gbx.proxy.ProxyBootstrap
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.reflect.full.functions
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

/**
 * Basic script handler.
 */
class ProxyScriptHandler {
    companion object {
        private val SCRIPTS_FOLDER = ProxyBootstrap.PARENT_FOLDER.resolve("scripts")
        private val EXTENSION_PREDICATE: Predicate<Path> = Predicate { file -> file.fileName.toString().endsWith(EXTENSION) }
        private val ENABLED_PREDICATE: Predicate<Path> = Predicate { file -> !file.fileName.toString().startsWith("-") }
        private val COMPILER_CONFIG = createJvmCompilationConfigurationFromTemplate<ProxyScript>()
    }

    val scripts: MutableMap<String, Any>

    /**
     * Loads all scripts on startup.
     */
    init {
        scripts = Files.walk(SCRIPTS_FOLDER).use { scriptsFolder ->
            scriptsFolder
                .filter(EXTENSION_PREDICATE)
                .filter(ENABLED_PREDICATE)
                .peek { file -> println("[Scripts] Loading ${file.fileName}") }
                .map(::loadScript)
                .collect(Collectors.toMap(
                    { it!!.first },
                    { it!!.second }
                ))
        }
    }

    /**
     * Disables the given script if possible.
     *
     * @param name the name of the script
     */
    fun disableScript(name: String) {
        val enabledPath = SCRIPTS_FOLDER.resolve("$name.$EXTENSION")
        val disabledPath = SCRIPTS_FOLDER.resolve("-$name.$EXTENSION")
        val loaded = scripts.containsKey(name)

        if (!loaded) {
            println("[Scripts] This script '$name' is already disabled!")
            return
        }

        if (enabledPath.toFile().exists())
            Files.move(enabledPath, disabledPath, StandardCopyOption.ATOMIC_MOVE)
        scripts.remove(name)
    }

    /**
     * Enables the given script if possible.
     *
     * @param name the name of the script
     */
    fun enableScript(name: String) {
        val enabledPath = SCRIPTS_FOLDER.resolve("$name.$EXTENSION")
        val disabledPath = SCRIPTS_FOLDER.resolve("-$name.$EXTENSION")
        val loaded = scripts.containsKey(name)

        if (loaded) {
            println("[Scripts] This script '$name' is already enabled!")
            return
        }

        if (disabledPath.toFile().exists())
            Files.move(disabledPath, enabledPath, StandardCopyOption.ATOMIC_MOVE)
        loadScript(enabledPath).apply {
            scripts[first] = second
        }
    }

    /**
     * Restarts the given script if possible.
     *
     * @param name the name of the script
     */
    fun restartScript(name: String) {
        disableScript(name)
        enableScript(name)
    }

    private fun loadScript(file: Path): Pair<String, Any> {
        val name = file.fileName.toString()
        val script = BasicJvmScriptingHost().eval(
            file.toFile().toScriptSource(),
            COMPILER_CONFIG,
            null
        ).valueOr { result ->
            throw RuntimeException(
                result.reports
                    .filter { it.severity == ScriptDiagnostic.Severity.ERROR }
                    .joinToString("\n") { it.exception?.toString() ?: it.message },
                result.reports.find { it.exception != null }?.exception
            )
        }

        return Pair(
            name.substring(0, name.lastIndexOf(EXTENSION) - 1),
            script.returnValue.scriptInstance!!
        )
    }
}