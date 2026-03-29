import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":WildCommons-Core"))
    implementation(project(":v1_21_11"))
    implementation(project(":v1_8_R3"))
    implementation(project(":v1_9_R2"))
    implementation(project(":v1_12_R1"))
}

tasks.withType<ShadowJar>() {
    archiveClassifier.set("")
}