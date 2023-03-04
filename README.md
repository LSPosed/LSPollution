[![Build](https://github.com/LSPosed/LSPollution/actions/workflows/build.yml/badge.svg)](https://github.com/LSPosed/LSPollution/actions/workflows/build.yml)

LSPollution
========

Resource obfuscator for Android applications. LSPollution supports [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html).

Features
--------
> The tool of obfuscated aab resources.

- **Merge duplicated resources:** Consolidate duplicate resource files to reduce package size.
- **Filter bundle files:** Support for filtering files in the `bundle` package. Currently only supports filtering in the `MATE-INFO/` and `lib/` paths.
- **White list:** The resources in the whitelist are not to be obfuscated.
- **Incremental obfuscation:** Input the `mapping` file to support incremental obfuscation.
- **Remove string:** Input the unused file splits by lines to support remove strings.
- **???:** Looking ahead, there will be more feature support, welcome to submit PR & issue.

Usage
-----
In order to make LSPollution work with your project you have to apply the LSPollution Gradle plugin
to the project.

The following is an example `settings.gradle.kts` to apply LSPollution.
```kotlin
pluginManagement {
  repositories {
    mavenCentral()
  }
  plugins {
    id("org.lsposed.lspollution") version "0.2.0"
  }
}
```

**Note that you should use at least Java 17 to launch the gradle daemon for this plugin (this is also required by AGP 8+).**
The project that uses this plugin on the other hand does not necessarily to target Java 17.

Configuration
-------------
LSPollution plugin can be configured using `lspollution` extension object.

The following is an example `build.gradle.kts` that configures `lspollution` extension object with default values.
```kotlin
plugins {
  id("org.lsposed.lspollution")
  // other plugins...
}

lspollution {
  mappingFile = file("mapping.txt").toPath() // Mapping file used for incremental obfuscation
  whiteList = [ // White list rules
    "*.R.raw.*",
    "*.R.drawable.icon"
  ]
  obfuscatedBundleFileName = "duplicated-app.aab" // Obfuscated file name, must end with '.aab'
  mergeDuplicatedRes = true // Whether to allow the merge of duplicate resources
  enableFilterFiles = true // Whether to allow filter files
  filterList = [ // file filter rules
    "*/arm64-v8a/*",
    "META-INF/*"
  ]

  enableFilterStrings = false // switch of filter strings
  unusedStringPath = file("unused.txt").toPath() // strings will be filtered in this file
  languageWhiteList = ["en", "zh"] // keep en,en-xx,zh,zh-xx etc. remove others.
}
```

The `lspollution plugin` intrudes the `bundle` packaging process and can be obfuscated by executing the original packaging commands.
```cmd
./gradlew clean :app:bundleDebug --stacktrace
```

Get the obfuscated `bundle` file path by `gradle` .
```kotlin
val lspollutionPlugin = project.tasks.getByName("lspollution${VARIANT_NAME}")
val bundlePath = lspollutionPlugin.getObfuscatedBundlePath()
```

### [Whitelist](wiki/en/WHITELIST.md)
The resources that can not be confused. Welcome PR your configs which is not included in [WHITELIST](wiki/en/WHITELIST.md)

### [Command line](wiki/en/COMMAND.md)
**LSPollution** provides a `jar` file that can be executed directly from the command line. More details, please go to **[Command Line](wiki/en/COMMAND.md)**.

### [Output](wiki/en/OUTPUT.md)
After the packaging is completed, the obfuscated file and the log files will be output. More details, please go to **[Output File](wiki/en/OUTPUT.md)**.
- **resources-mapping.txt:** Resource obfuscation mapping, which can be used as the next obfuscation input to achieve incremental obfuscate.
- **aab:** Optimized aab file.
- **-duplicated.txt:** duplicated file logging.

## [Change log](wiki/en/CHANGELOG.md)
Version change log. More details, please go to **[Change Log](wiki/en/CHANGELOG.md)** .

Credit
------
LSPollution was forked from https://github.com/bytedance/AabResGuard. Credits to its original authors.

License
=======
    Copyright 2019-2021 AabResGuard Authors
    Copyright 2023 LSPosed

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
