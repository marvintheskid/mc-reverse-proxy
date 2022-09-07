package gbx.proxy.scripting;

import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a script provider.
 */
public interface ScriptProvider {
    /**
     * Provides values to the given context.
     *
     * @param global the global context
     */
    void provide(@NotNull Value global);
}
