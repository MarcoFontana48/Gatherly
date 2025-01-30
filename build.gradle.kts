import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.task.tree)
}

buildscript {
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject.libs.kotlin.stdlib)
    implementation(project(":commons"))
    implementation(project(":utils"))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.web.client)
    implementation(libs.vertx.kafka.client)
    implementation(libs.jackson.core)
    implementation(libs.jackson.module.kotlin)
    testImplementation(rootProject.libs.bundles.kotlin.testing)
    testImplementation(libs.junit)
    kover(project(":friendship-service"))
    kover(project(":user-service"))
}

kotlin {
    // sets tools to build, run and test the software to version 21
    jvmToolchain(21)

    // sets all warnings as errors
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks.test {
    useJUnitPlatform()
}

gitSemVer {
    versionPrefix.set("v")
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
    assignGitSemanticVersion()
}

val kotlinProjects = listOf("friendship-service", "user-service")

kover {
    merge {
        // Include only kotlin projects
        kotlinProjects.forEach {
            projects(":$it")
        }

        // Create an aggregated variant for the main services
        createVariant("aggregated") {
            add("jvm")
        }
    }

    currentProject {
        // Copy the aggregated variant for relevant projects
        kotlinProjects.forEach {
            copyVariant(it, "aggregated")
        }
    }

    reports {
        kotlinProjects.forEach {
            variant(it) {
                filters.includes.projects.add(":$it")
            }
        }
    }

    tasks.dokkaHtmlMultiModule {
        outputDirectory.set(layout.projectDirectory.dir("resurces/dokka"))
    }
}
