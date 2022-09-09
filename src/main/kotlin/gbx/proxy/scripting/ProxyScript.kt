package gbx.proxy.scripting

import kotlin.script.experimental.annotations.KotlinScript

const val EXTENSION = "proxy.kts"

@KotlinScript(
    fileExtension = EXTENSION,
    compilationConfiguration = ProxyScriptConfiguration::class
)
abstract class ProxyScript
