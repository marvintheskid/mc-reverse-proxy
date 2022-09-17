import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    val jlineVersion = "3.21.0"
    val tcaVersion = "1.3.0"

    implementation(project(":api"))

    api(group = "org.jline", name = "jline-reader", version = jlineVersion)
    api(group = "org.jline", name = "jline-terminal", version = jlineVersion)
    api(group = "org.jline", name = "jline-terminal-jansi", version = jlineVersion)
    api(group = "net.minecrell", name = "terminalconsoleappender", version = tcaVersion)
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)

    manifest {
        attributes["Main-Class"] = "me.marvin.proxy.ProxyBootstrap"
        attributes["Multi-Release"] = "true"
    }
}