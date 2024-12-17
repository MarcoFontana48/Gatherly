// defines a list of plugins to be applied to the project
plugins {
    id("kotlin-multiplatform")
    alias(libs.plugins.ktlint)
}

// what repositories to use for resolving dependencies
repositories {
    // use the maven central repository
    mavenCentral()
}

// top-level block for multiplatform project configuration
kotlin {
    jvmToolchain(21)

    // defines as target for the project jvm
    jvm {
        // defines that it will include Java sources into the JVM target's compilations
        withJava()
    }
    // defines a target for the project js
    js {
        // uses commonJs for js dependencies management
        useCommonJs()

        // enables tasks for Node packages generation
        binaries.executable()

        // target consists of a node project
        nodejs {
            // configuration of project running
            runTask {}
            // configuration of test execution
            testRuns {}
        }
    }

    // configures predefined and declares custom source sets of the project
    sourceSets {
        // defines a source set for common code
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val jsMain by getting {
            dependencies {
                api(kotlin("stdlib-js"))
            }
        }
    }

    sourceSets.all {
        languageSettings.apply {
            // provides source compatibility with the specified version of Kotlin.
            languageVersion = "1.8"

            // allows using declarations only from the specified version of Kotlin bundled libraries.
            apiVersion = "1.8"

            // enables the specified language feature
            enableLanguageFeature("InlineClasses")

            // allow using the specified opt-in
            optIn("kotlin.ExperimentalUnsignedTypes")

            // enables/disable progressive mode (default is false)
            progressiveMode = true
        }
    }
}
