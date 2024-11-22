import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.ktlint)
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
    testImplementation(rootProject.libs.bundles.kotlin.testing)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
    }
}

gitSemVer {
    versionPrefix.set("v")
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
    assignGitSemanticVersion()
}
