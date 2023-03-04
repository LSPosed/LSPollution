@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin)
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    annotationProcessor(libs.auto.value)
    compileOnly(libs.aapt2.proto)
    compileOnly(libs.androidx.annotation)
    compileOnly(libs.auto.value.annotations)
    compileOnly(libs.guava)
    implementation(libs.bundletool)
    implementation(libs.commons.codec)
    implementation(libs.protobuf.java)

    testCompileOnly(libs.guava)
    testImplementation(libs.junit)
}

publish {
    githubRepo = "LSPosed/LSPollution"
    publications {
        register<MavenPublication>(rootProject.name) {
            artifactId = project.name
            group = group
            version = version
            from(components.getByName("java"))
            pom {
                name.set(project.name)
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
    }
}
