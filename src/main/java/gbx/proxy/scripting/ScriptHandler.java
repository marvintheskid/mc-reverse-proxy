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
    private final Map<String, Source> scripts;

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
                .collect(Collectors.toMap(
                    file -> {
                        String name = file.getFileName().toString();
                        return name.substring(0, name.lastIndexOf(".js"));
                    },
                    file -> {
                        try {
                            return Source.newBuilder("js", file.toFile())
                                .cached(true)
                                .build();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                ));
        }
    }

    /**
     * Executes all enabled scripts.
     *
     * @param valueConsumer the value consumer
     */
    public void forAllScripts(Consumer<Value> valueConsumer) {
        try (Context ctx = acquireContext()) {
            scripts.values().forEach(script -> {
                Value evaluated = ctx.eval(script);
                valueConsumer.accept(evaluated);
            });
        };
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
        engine.close();
    }
}
