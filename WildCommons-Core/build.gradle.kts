plugins {
    id("java")
}


repositories {
    mavenCentral()
    maven {
        name = "essentialsx"
        url = uri("https://repo.essentialsx.net/releases/")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

}

dependencies {
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    implementation("org.apache.logging.log4j:log4j-core:3.0.0-beta3")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.4")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    compileOnly("net.essentialsx:EssentialsX:2.19.0")
}