import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    val nettyVersion = "4.1.77.Final"
    val annotationsVersion = "23.0.0"
    val gsonVersion = "2.9.1"
    val authlibVersion = "3.5.41"
    val log4jVersion = "2.19.0"

    api(group = "io.netty", name = "netty-all", version = nettyVersion)
    api(group = "org.jetbrains", name = "annotations", version = annotationsVersion)
    api(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    api(group = "com.mojang", name = "authlib", version = authlibVersion)
    api(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)
    api(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
    api(group = "org.apache.logging.log4j", name = "log4j-slf4j2-impl", version = log4jVersion)
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}