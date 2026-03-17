plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.9.4-R0.1-SNAPSHOT")
    compileOnly(project(":WildCommons-Core"))
}

tasks.test {
    useJUnitPlatform()
}