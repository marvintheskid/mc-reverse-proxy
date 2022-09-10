package gbx.proxy.scripting

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

/**
 * Represents a proxy script.
 */
class ProxyScript(val name: String, val instance: Any) {
    companion object {
        val PARAM_CACHE = mutableMapOf<KClass<*>, KType>()
    }

    /**
     * Initializes this script.
     */
    fun initialize() {
        instance::class.functions
            .filter { it.hasAnnotation<Entrypoint>() }
            .forEach { it.call(instance) }
    }

    /**
     * Destroys this script.
     */
    fun destruct() {
        instance::class.functions
            .filter { it.hasAnnotation<Destructor>() }
            .forEach { it.call(instance) }
    }

    /**
     * Calls the given function with the given parameters.
     *
     * @param function the function's name
     */
    fun call(function: String, vararg params: Any) {
        val functions = instance::class.functions
            .filter { fn -> fn.name == function }

        if (functions.size == 1) {
            functions[0].call(instance, *params)
        } else {
            functions
                .filter { fn ->
                    val fnTypes = fn.parameters.map { param -> param.type }
                    val paramTypes = params.map { param -> PARAM_CACHE.computeIfAbsent(param::class) { it.createType() } }
                    return@filter fnTypes == paramTypes
                }
                .first()
                .call(instance, *params)
        }
    }
}