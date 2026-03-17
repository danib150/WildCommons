plugins {
    java
    id("com.gradleup.shadow") version "9.4.0" apply false
}

group = "it.danielebruni.wildadventure"
version = "1.0.0"


subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")

        maven("https://repo.codemc.io/repository/nms/") {
            name = "codemc-nms"
        }

        maven("https://repo.codemc.io/repository/maven-public/") {
            name = "codemc"
        }

    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.44")
        annotationProcessor("org.projectlombok:lombok:1.18.44")
    }

    java {

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        withSourcesJar()
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}