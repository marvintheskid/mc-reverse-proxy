package gbx.proxy.scripting

/**
 * A method annotated with this annotation gets called when the script gets enabled.
 * Methods with this annotation must have 0 parameters.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Entrypoint

/**
 * A method annotated with this annotation gets called when the script gets disabled.
 * Methods with this annotation must have 0 parameters.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Destructor

/**
 * The extension of the scripts.
 *
 * @see ProxyScriptDefinition
 * @see ProxyScriptHandler
 */
const val EXTENSION = "proxy.kts"
