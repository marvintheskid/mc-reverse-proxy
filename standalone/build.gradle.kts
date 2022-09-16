import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":api"))
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "me.marvin.proxy.ProxyBootstrap"
    }
}