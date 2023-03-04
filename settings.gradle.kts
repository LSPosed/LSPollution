enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("aapt2-proto", "com.android.tools.build:aapt2-proto:7.4.2-8841542")
            library("agp", "com.android.tools.build:gradle-api:7.4.2")
            library("agp-impl", "com.android.tools.build:gradle:7.4.2")
            library("androidx-annotation", "androidx.annotation:annotation:1.6.0")
            library("auto-value", "com.google.auto.value:auto-value:1.8.2")
            library("auto-value-annotations", "com.google.auto.value:auto-value-annotations:1.10.1")
            library("bundletool", "com.android.tools.build:bundletool:1.14.0")
            library("commons-codec", "commons-codec:commons-codec:1.5")
            library("guava", "com.google.guava:guava:31.1-jre")
            library("junit", "junit:junit:4.13.2")
            library("protobuf-java", "com.google.protobuf:protobuf-java:3.21.12")
            plugin("lsplugin-publish", "org.lsposed.lsplugin.publish").version("1.1")
            plugin("kotlin", "org.jetbrains.kotlin.jvm").version("1.8.10")
        }
    }
}
rootProject.name = "lspollution"

include(":core", ":gradle-plugin")

//include ':core', ':plugin', ':app', ':df_module1', ':df_module2'
//
//settings.project(":app").projectDir = file("$rootDir/samples/app")
//settings.project(":df_module1").projectDir = file("$rootDir/samples/dynamic-features/df_module1")
//settings.project(":df_module2").projectDir = file("$rootDir/samples/dynamic-features/df_module2")
//
