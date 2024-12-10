import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit
import io.github.gciatto.kt.mpp.Plugins
import io.github.gciatto.kt.mpp.helpers.ProjectType

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ktMpp.multiProjectHelper)
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

multiProjectHelper {
    defaultProjectType = ProjectType.KOTLIN // default project type for all projects which are not explicitly marked

    ktProjects = subprojects.toSet()
    jvmProjects = emptySet()
    jsProjects = emptySet()
    otherProjects = emptySet()

    val baseProjectTemplate =
        buildSet {
            add(Plugins.documentation)
            add(Plugins.linter)
            add(Plugins.bugFinder)
            add(Plugins.versions)
        }

    ktProjectTemplate =
        buildSet {
            addAll(baseProjectTemplate)
            add(Plugins.multiplatform)
        }

    jvmProjectTemplate =
        buildSet {
            addAll(baseProjectTemplate)
            add(Plugins.jvmOnly)
        }

    jsProjectTemplate =
        buildSet {
            addAll(baseProjectTemplate)
            add(Plugins.jsOnly)
        }

    otherProjectTemplate =
        buildSet {
            add(Plugins.versions)
        }

    applyProjectTemplates()
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
