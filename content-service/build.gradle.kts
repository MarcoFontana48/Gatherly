import com.lordcodes.turtle.shellRun
import io.github.zuccherosintattico.gradle.BuildCommandExecutable
import io.github.zuccherosintattico.utils.NodeCommandsExtension.npmCommand

plugins {
    alias(libs.plugins.typescript.gradle.plugin)
}

typescript {
    entrypoint = "src/main/typescript/main.js"
    outputDir = "build/dist"
    tsConfig = "tsconfig.json"
    buildCommandExecutable = BuildCommandExecutable.NPM
    buildCommand = "run build"
}

node {
    shouldInstall = true
    version = "22.1.0"
}

tasks.named("check") {
    dependsOn("compileTypescript")
    doLast {
        runCatching {
            shellRun(project.projectDir) {
                npmCommand(project, "run", "type-checking")
            }
        }
            .onFailure { logger.error(it.stackTraceToString()) }
    }
}
