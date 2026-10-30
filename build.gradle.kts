plugins {
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.jvm)
}

group = "it.unibo.spe.compose"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.mariaDb.client)
    testImplementation(libs.kotlin.test.junit)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors = true
        optIn.add("kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnit()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
