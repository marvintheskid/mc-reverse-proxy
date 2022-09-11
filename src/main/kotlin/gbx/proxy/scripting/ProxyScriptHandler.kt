package gbx.proxy.scripting

import gbx.proxy.ProxyBootstrap
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmScriptingHostConfigurationBuilder
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
        private val COMPILER_CONFIG = createJvmCompilationConfigurationFromTemplate<ProxyScriptDefinition>()
    }

    private val scripts: MutableMap<String, ProxyScript>

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
                .peek(ProxyScript::initialize)
                .collect(Collectors.toMap(
                    { it!!.name },
                    { it!! }
                ))
        }
    }

    /**
     * Loops through all the scripts, and executes the given action.
     *
     * @param action the action
     */
    fun forEachScript(action: BiConsumer<Class<out Any>, ProxyScript>) {
        scripts.values.forEach { action.accept(it.instance::class.java, it) }
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

        if (enabledPath.toFile().exists()) {
            Files.move(enabledPath, disabledPath, StandardCopyOption.ATOMIC_MOVE)
        }

        scripts.remove(name)
            ?.apply { destruct() }
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
            initialize()
            scripts[this.name] = this
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

    private fun loadScript(file: Path): ProxyScript {
        val name = file.fileName.toString()

        val script = BasicJvmScriptingHost().eval(
            file.toFile().toScriptSource(),
            COMPILER_CONFIG,
            null
        ).valueOr { result ->
            throw RuntimeException(
                result.reports
                    .filter {
                        it.severity == ScriptDiagnostic.Severity.ERROR
                            || it.severity == ScriptDiagnostic.Severity.FATAL
                            || (it.severity == ScriptDiagnostic.Severity.WARNING
                                && it.code == ScriptDiagnostic.unspecifiedError
                                && it.location != null)
                    }
                    .joinToString("\n") { it.render(withSeverity = false) },
                result.reports.find { it.exception != null }?.exception
            )
        }

        return ProxyScript(
            name.substring(0, name.lastIndexOf(EXTENSION) - 1),
            script.returnValue.scriptInstance!!
        )
    }
}