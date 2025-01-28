# Version Control & Repository
The Version Control System (VCS) is a system that records changes to a file or set of files over time so that you can recall specific versions later.
It allows you to revert files back to a previous state, revert the entire project back to a previous state, 
compare changes over time, see who last modified something that might be causing a problem, 
who introduced an issue and when, and more.


The project uses **Git** as VCS.

## Semantic Versioning
The project uses Semantic Versioning for software release versioning.
This practice has been automated using the `git-sensitive-semantic-versioning` plugin, 
which can calculate the current version based on Git tags and the number of commits since those tags.

The plugin has been configured to use a versioning strategy based on conventional commit messages, 
incrementing the major, minor, or patch version depending on the commit type (e.g., feat, fix, build).

Below is our configuration.

```kotlin
plugins {
    alias(libs.plugins.gitSemVer)
}

buildscript {
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.0")
    }
}

gitSemVer {
    versionPrefix.set("v")
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
    assignGitSemanticVersion()
}
```

## Conventional Commits
The project uses Conventional Commits for commit messages.

For this purpose, a Git hook was implemented using Gradle to ensure the correctness of commits. 
Additionally, the same hook verifies that file formatting adheres to Kotlin language conventions via ktlint plugin.

To install automatically the Git hook, it's been used the `gradle-pre-commit-git-hooks` gradle plugin, 
as follows:

```kotlin
plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.8"
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks()
}
```

##  Repository Management

### Branching Strategy
The project uses the branch `main` as default branch, where semantic versioning and release are applied.
Additionally, a `dev` branch was used to develop new features. 

When parallelizing work was necessary, 
feature branches were created from the `dev` branch, following the naming convention `feat/<feature-name>`.
Other types of branches used are:
- `fix/<issue-name>`: branch for a bug fix.
- `refactor/<section-to-be-refactored>`: branch for refactors.


During development, the following branch map was created to better illustrate the branching logic.
![Branch Map](./img/Progetto%20SPE%20-%20Presentazione.jpg)

### Merging Strategy
The team mostly uses a rebase strategy for merging branches, allowing to keep the commit history clean and linear.

### Pull Requests
Given the small size of the development team, a pull request-based approach was deemed unnecessary. 
Instead, the coordination achieved during development was sufficient to stay updated on the repository's status 
and organize code reviews effectively.


[« Back to Index](../docs.md) | [« Previous](./build-system.md) | [Next »](./ci.md)