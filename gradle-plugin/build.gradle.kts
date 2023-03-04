import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    idea
    alias(libs.plugins.kotlin)
    `java-gradle-plugin`
    `maven-publish`
    signing
}

dependencies {
    // TODO: Replace with `libs.agp`
    compileOnly(libs.agp.impl)
    implementation(projects.core)
}

val generatedDir = File(projectDir, "generated")
val generatedJavaSourcesDir = File(generatedDir, "main/java")

val genTask = tasks.register("generateBuildClass") {
    inputs.property("version", version)
    outputs.dir(generatedDir)
    doLast {
        val buildClassFile =
            File(generatedJavaSourcesDir, "org/lsposed/lspollution/plugin/Build.java")
        buildClassFile.parentFile.mkdirs()
        buildClassFile.writeText(
            """
            package org.lsposed.lspollution.plugin;
            /**
             * The type Build.
             */
            public class Build {
               /**
                * The constant VERSION.
                */
               public static final String VERSION = "$version";
            }""".trimIndent()
        )
    }
}

sourceSets {
    main {
        java {
            srcDir(generatedJavaSourcesDir)
        }
    }
}

tasks.withType(KotlinCompile::class.java) {
    dependsOn(genTask)
}

tasks.withType(Jar::class.java) {
    dependsOn(genTask)
}

idea {
    module {
        generatedSourceDirs.add(generatedJavaSourcesDir)
    }
}

publish {
    githubRepo = "LSPosed/LSPollution"
    publishPlugin("$group", rootProject.name, "org.lsposed.lspollution.plugin.LSPollutionPlugin") {
        name.set(rootProject.name)
        description.set("Resource obfuscator for Android applications")
        url.set("https://github.com/LSPosed/LSPollution")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://github.com/LSPosed/LSPollution/blob/master/LICENSE.txt")
            }
        }
        developers {
            developer {
                name.set("LSPosed")
                url.set("https://lsposed.org")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/LSPosed/LSPollution.git")
            url.set("https://github.com/LSPosed/LSPollution")
        }
    }
}
