/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
    // Serialization version should be aligned with the Kotlin version embedded in Gradle
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

val buildSnapshotTrain = properties["build_snapshot_train"]?.toString().toBoolean()

extra["kotlin_repo_url"] = rootProject.properties["kotlin_repo_url"]
val kotlin_repo_url: String? by extra
val kotlinVersion= rootProject.properties["kotlin_version"].toString()

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://plugins.gradle.org/m2")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
    mavenLocal()
    if (buildSnapshotTrain) {
        mavenLocal()
    }
    if (kotlin_repo_url != null) {
        maven(kotlin_repo_url!!)
    }
}

val ktor_version = "3.0.0-rc-2-eap-1091"

dependencies {
    implementation(kotlin("gradle-plugin", "2.1.0-dev-8424"))
    implementation(kotlin("compiler-embeddable", "2.1.0-dev-8424")) //gradle don't support LV 2.1
    implementation(kotlin("serialization", kotlinVersion))

    val ktlint_version = "3.15.0"
    implementation("org.jmailen.gradle:kotlinter-gradle:$ktlint_version")

    implementation("io.ktor:ktor-server-default-headers:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-caching-headers:$ktor_version")
    implementation("io.ktor:ktor-server-conditional-headers:$ktor_version")
    implementation("io.ktor:ktor-server-compression:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx:$ktor_version")
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
    implementation("io.ktor:ktor-utils:$ktor_version")

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)
    implementation(libs.tomlj)
    implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${libs.versions.atomicfu.get()}")

    // A hack to make version catalogs accessible from buildSrc sources
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

kotlin {
    jvmToolchain(8)
}


extra["kotlin_language_version"] = rootProject.properties["kotlin_language_version"]
val kotlin_language_version: String? by extra

extra["kotlin_api_version"] = rootProject.properties["kotlin_api_version"]
val kotlin_api_version: String? by extra

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xsuppress-version-warnings",
        "-Xskip-metadata-version-check",
    )

    if (kotlin_language_version != null) {
        println("Using Kotlin Language Version 1.9 for buildSrc folder, because Gradle does not support 2.1 yet.")
        kotlinOptions.languageVersion = "1.9"
    }
    if (kotlin_language_version != null) {
        kotlinOptions.apiVersion = "1.9"
    }
}
