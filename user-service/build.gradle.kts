plugins {
    kotlin("jvm")
}

group = "spe.app"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    testImplementation(libs.archunit)
    implementation(project(":commons"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors = true
    }
}