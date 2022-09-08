package gbx.proxy.scripting;

import gbx.proxy.ProxyBootstrap;
import gbx.proxy.scripting.context.ProxyContext;
import gbx.proxy.scripting.context.UtilityContext;
import org.graalvm.polyglot.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic script handler.
 */
public class ScriptHandler implements Closeable {
    private static final List<ScriptProvider> PROVIDERS = List.of(
        ProxyContext.instance(),
        UtilityContext.instance()
    );
    private final Engine engine;
    private final Map<String, Script> scripts;

    /**
     * Creates a new script handler, and loads all enabled scripts.
     *
     * @throws IOException if {@link Files#walk(Path, FileVisitOption...)} fails
     */
    public ScriptHandler() throws IOException {
        engine = Engine.create();
        try (Stream<Path> scriptsFolder = Files.walk(ProxyBootstrap.PARENT_FOLDER.resolve("scripts"))) {
            scripts = scriptsFolder
                .filter(file -> file.getFileName().toString().endsWith(".js"))
                .filter(file -> !file.getFileName().toString().startsWith("-"))
                .peek(file -> System.out.println("[Scripts] Loading " + file.getFileName() + "..."))
                .map(file -> {
                    try {
                        String name = file.getFileName().toString();
                        Source source = Source.newBuilder("js", file.toFile())
                            .cached(true)
                            .build();

                        return new Script(
                            name.substring(0, name.lastIndexOf(".js")),
                            source,
                            this::acquireContext
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .peek(Script::initialize)
                .collect(Collectors.toMap(Script::name, Function.identity()));
        }
    }

    /**
     * Iterates through all enabled scripts.
     *
     * @param scriptConsumer the script consumer
     */
    public void forAllScripts(Consumer<Script> scriptConsumer) {
        if (scripts.isEmpty()) return;
        scripts.values().forEach(scriptConsumer);
    }

    /**
     * Executes all enabled scripts.
     *
     * @param valueConsumer the value consumer
     */
    public void executeScripts(Consumer<Value> valueConsumer) {
        if (scripts.isEmpty()) return;

        try (Context ctx = acquireContext()) {
            scripts.values().forEach(script -> script.execute(ctx, valueConsumer));
        }
    }

    /**
     * Acquires the correct JS context and applies all {@link ScriptProvider script providers}.
     *
     * @return a new context
     */
    private Context acquireContext() {
        Context ctx = Context.newBuilder("js")
            .engine(engine)
            .allowHostClassLookup(s -> true)
            .allowHostAccess(HostAccess.ALL)
            .build();

        PROVIDERS.forEach(provider -> provider.provide(ctx.getBindings("js")));
        return ctx;
    }

    @Override
    public void close() {
        scripts.values().forEach(Script::close);
        engine.close();
    }
}
