import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "gbx.proxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://libraries.minecraft.net/")
    }
}

dependencies {
    val junitVersion = "5.8.1"
    val nettyVersion = "4.1.77.Final"
    val annotationsVersion = "23.0.0"
    val gsonVersion = "2.9.1"
    val authlibVersion = "3.5.41"
    val kotlinVersion = "1.7.10"
    val coroutinesVersion = "1.6.4"

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)

    implementation(group = "io.netty", name = "netty-all", version = nettyVersion)
    implementation(group = "org.jetbrains", name = "annotations", version = annotationsVersion)
    implementation(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    implementation(group = "com.mojang", name = "authlib", version = authlibVersion)

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-common", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-jvm", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-jvm-host", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-dependencies", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-dependencies-maven", version = kotlinVersion)

    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
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
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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