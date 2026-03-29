plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":WildCommons-Core"))
}

tasks.test {
    useJUnitPlatform()
}