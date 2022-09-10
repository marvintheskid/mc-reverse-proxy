package gbx.proxy.scripting

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = EXTENSION,
    compilationConfiguration = ProxyScriptConfiguration::class
)
abstract class ProxyScriptDefinition
