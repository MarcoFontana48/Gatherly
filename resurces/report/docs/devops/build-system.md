# Build System & Project Structure
The build system is a set of tools and practices used to automate the process of building the project.

The project uses **Gradle** as build system.

## Project Structure
The team decided to use mono-git-repository, multi project structure. Subprojects are the following ones:
- `commons (multiplatform)`: contains the multiplatform code shared across all subprojects.
- `content-service (NodeJs)`: contains the content microservice.
- `friendship-service (JVM)`: contains the friendship microservice.
- `user-service (JVM)`: contains the user microservice.
- `utils (JVM)`: contains facilities for testing.

## Dependencies & Plugins Declaration
In order to collect all the dependencies and plugins in a single place, 
the project uses the `Version Catalog`. You can find the file in `gradle/libs.versions.toml`.

This method allows to declare dependencies and plugins in `build.gradle.kts` files using the following syntax:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependecies {
    implementation(rootProject.libs.kotlin.stdlib)
}
```

## Typescript Gradle Plugin
To include in the build process of the content-service project, 
it was necessary to use a plugin that provides tasks for compiling TypeScript code. 
Specifically, these tasks are:

- `compileTypescript`: Compiles TypeScript files.
- `npmDependencies`: Installs dependencies from the package.json.
- `checkNode`: Checks if Node.js is installed. If not specified, it will download it.

During the project's `build` process, these tasks are invoked in sequence as dependencies, 
starting with `checkNode`, followed by `npmDependencies`, and finally `compileTypescript`.

It should be noted that this plugin was developed for a prior university project.
This is a link to the repository containing it: [Typescript Plugin](https://github.com/zucchero-sintattico/typescript-gradle-plugin).

## Shadow Jar
For generating JAR files, since they include dependencies from other projects,
we used the `Shadow Jar` plugin, which allows the build process to correctly include all dependencies.

During each `build`, the `shadowJar` task is executed as a dependency.
This can be verified using the [gradle-task-tree](https://github.com/dorongold/gradle-task-tree) 
plugin by executing the taskTree task.
