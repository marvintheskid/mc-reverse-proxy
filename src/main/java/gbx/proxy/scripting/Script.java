package gbx.proxy.scripting;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple helper class for holding {@link Source GraalVM Polyglot source files}.
 */
public class Script implements AutoCloseable {
    private final String name;
    private final Source source;
    private final Supplier<Context> contextFactory;

    private Context persistentContext;

    public Script(@NotNull String name, @NotNull Source source, @NotNull Supplier<Context> contextFactory) {
        this.name = name;
        this.source = source;
        this.contextFactory = contextFactory;
    }

    /**
     * Initializes this script.
     */
    public void initialize() {
        if (persistentContext != null) {
            persistentContext.close();
            persistentContext = null;
        }

        persistentContext = contextFactory.get();
    }

    /**
     * Closes this script's persistent context.
     */
    @Override
    public void close() {
        if (persistentContext != null) {
            persistentContext.close();
            persistentContext = null;
        }
    }

    /**
     * Executes this script with a freshly created context.
     *
     * @param action the action
     * @param <T>    the type of the mapped value
     * @return a mapped value
     */
    @Nullable
    public <T> T execute(@NotNull Function<Value, T> action) {
        try (Context ctx = contextFactory.get()) {
            return execute(ctx, action);
        }
    }

    /**
     * Executes this script with a freshly created context.
     *
     * @param action the action
     */
    public void execute(@NotNull Consumer<Value> action) {
        try (Context ctx = contextFactory.get()) {
            execute(ctx, action);
        }
    }

    /**
     * Executes this script with the persistent context.
     *
     * @param action the action
     * @param <T>    the type of the mapped value
     * @return a mapped value
     */
    @Nullable
    public <T> T executePersistent(@NotNull Function<Value, T> action) {
        return execute(persistentContext, action);
    }

    /**
     * Executes this script with the persistent context.
     *
     * @param action the action
     */
    public void executePersistent(@NotNull Consumer<Value> action) {
        execute(persistentContext, action);
    }

    /**
     * Executes this script with a freshly created context.
     *
     * @param action the action
     * @param <T>    the type of the mapped value
     * @return a mapped value
     */
    @Nullable
    public <T> T execute(@NotNull Context ctx, @NotNull Function<Value, T> action) {
        Objects.requireNonNull(ctx, "context was null");
        Value value = ctx.eval(source);
        return action.apply(value);
    }

    /**
     * Executes this script with a freshly created context.
     *
     * @param action the action
     */
    public void execute(@NotNull Context ctx, @NotNull Consumer<Value> action) {
        Objects.requireNonNull(ctx, "context was null");
        Value value = ctx.eval(source);
        action.accept(value);
    }

    /**
     * Returns the name of the script (without extension).
     *
     * @return the name
     */
    @NotNull
    public String name() {
        return name;
    }

    /**
     * Returns the source {@link Source GraalVM Polyglot source file} associated with this object..
     *
     * @return the source
     */
    @NotNull
    public Source source() {
        return source;
    }
}
