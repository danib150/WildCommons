plugins {
    java
    id("com.gradleup.shadow") version "9.4.0" apply false
    id("maven-publish")
}

group = "it.danielebruni.wildadventure"
version = "1.0.1"
val publishableModules = setOf("WildCommons-Core")


subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    if (name in publishableModules) {
        apply(plugin = "maven-publish")

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("gpr") {
                    from(components["java"])
                    groupId = project.group.toString()
                    artifactId = project.name.lowercase()
                    version = project.version.toString()
                }
            }

            repositories {
                maven {
                    name = "WildCommons"
                    url = uri("https://maven.pkg.github.com/danib150/WildCommons")

                    credentials {
                        username = (findProperty("gpr.user") as String?)
                            ?: System.getenv("GITHUB_ACTOR")
                        password = (findProperty("gpr.key") as String?)
                            ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

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