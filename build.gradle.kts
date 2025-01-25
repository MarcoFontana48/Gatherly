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
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.0")
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
    jvmToolchain(21)

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
//        projects(":friendship-service", ":user-service")

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
//        copyVariant("friendship-service", "aggregated")
//        copyVariant("user-service", "aggregated")
    }

    reports {
//        variant("friendship-service") {
//            filters.includes.projects.add(":friendship-service")
//        }
//        variant("user-service") {
//            filters.includes.projects.add(":user-service")
//        }
        kotlinProjects.forEach {
            variant(it) {
                filters.includes.projects.add(":$it")
            }
        }
    }
}
