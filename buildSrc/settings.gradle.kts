import java.io.FileInputStream
import java.util.Properties

/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/
pluginManagement {
    val build_snapshot_train: String? by settings
    val kotlin_repo_url: String? by settings
    repositories {
        maven("https://plugins.gradle.org/m2")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        mavenCentral()
        if (build_snapshot_train?.toBoolean() == true) {
            mavenLocal()
        }
        if (kotlin_repo_url != null) {
            maven(kotlin_repo_url!!)
        }
    }
}

plugins {
    id ("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    versionCatalogs {
        val libs by creating {
            from(files("../gradle/libs.versions.toml"))

            fun kotlinVersionFromProjectRootProperties(): String? {
                val properties = Properties()
                FileInputStream(file("../gradle.properties")).use {
                    properties.load(it)
                }
                return properties["kotlin_version"]?.toString()
            }

            val kotlinVersion = if (extra.has("kotlin_version")) {
                extra.get("kotlin_version").toString()
            } else {
                kotlinVersionFromProjectRootProperties()
            }

            if (kotlinVersion != null) {
                version("kotlin-version", kotlinVersion)
            }
        }
    }
}
