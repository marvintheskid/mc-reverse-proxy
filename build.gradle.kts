import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "gbx.proxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    val junitVersion = "5.8.1"
    val nettyVersion = "4.1.77.Final"
    val annotationsVersion = "23.0.0"

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)

    implementation(group = "io.netty", name = "netty-all", version = nettyVersion)
    implementation(group = "org.jetbrains", name = "annotations", version = annotationsVersion)
}

tasks.withType<ShadowJar> {
    exclude("META-INF/**.txt")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")

    manifest {
        attributes["Main-Class"] = "gbx.proxy.Bootstrap"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT)
    }
}

tasks.named<DefaultTask>("build") {
    dependsOn("shadowJar")
}