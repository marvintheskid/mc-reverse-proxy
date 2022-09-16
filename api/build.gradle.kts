dependencies {
    val nettyVersion = "4.1.77.Final"
    val annotationsVersion = "23.0.0"
    val gsonVersion = "2.9.1"
    val authlibVersion = "3.5.41"

    api(group = "io.netty", name = "netty-all", version = nettyVersion)
    api(group = "org.jetbrains", name = "annotations", version = annotationsVersion)
    api(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    api(group = "com.mojang", name = "authlib", version = authlibVersion)
}