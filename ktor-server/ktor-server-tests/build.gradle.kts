description = ""

plugins {
    id("kotlinx-serialization")
}

kotlin.sourceSets {
    commonTest {
        dependencies {
            api(project(":ktor-server"))
            api(project(":ktor-server:ktor-server-plugins:ktor-server-rate-limit"))
            api(project(":ktor-server:ktor-server-test-host"))
        }
    }
    jvmTest {
        dependencies {
            implementation(libs.jansi)
            implementation(project(":ktor-client:ktor-client-plugins:ktor-client-encoding"))
            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
            api(libs.logback.classic)
            api(project(":ktor-server:ktor-server-plugins:ktor-server-sse"))
        }
    }
}
