plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

group = "spe.app"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.vertx.core)
    implementation(project(":commons"))
    implementation("mysql:mysql-connector-java:8.0.33")
    testImplementation(kotlin("test"))
    testImplementation(libs.archunit)
    testImplementation(libs.mockito.core)
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
