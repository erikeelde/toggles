# Replace Develocity with Premex Pulse — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Swap `com.gradle.develocity` for `dev.premex.pulse` across all three Gradle files that reference it.

**Architecture:** Pulse is applied as a project-level plugin in `build.gradle.kts`. Its Maven repo is declared in `settings.gradle.kts` `pluginManagement`. `build-logic` has Develocity removed and gets no Pulse plugin — its tasks roll up into the main build's scan automatically.

**Tech Stack:** Gradle Kotlin DSL, `dev.premex.pulse` 0.3.4, Maven repo at `https://artifacts.premex.se/api/maven/premex/pulse/`

---

### Task 1: Update `settings.gradle.kts`

**Files:**
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Add Pulse Maven repo to `pluginManagement` and remove Develocity plugin**

Replace the entire file with:

```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        maven {
            url = uri("https://artifacts.premex.se/api/maven/premex/pulse/")
            content {
                includeGroupByRegex("dev\\.premex\\.pulse.*")
            }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
    remote<HttpBuildCache> {
        isEnabled = false
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "Toggles"
includeBuild("build-logic")
include(
    ":toggles-app",
    ":toggles-sample",
    ":modules:compose-theme",
    ":modules:coroutines:api",
    ":modules:coroutines:wiring",
    ":modules:database:api",
    ":modules:database:implementation",
    ":modules:database:wiring",
    ":modules:provider:api",
    ":modules:provider:implementation",
    ":modules:provider:wiring",
    ":modules:applications",
    ":modules:configurations",
    ":modules:oss",
    ":modules:help",
    ":modules:routes:api",
    ":modules:stringconfiguration",
    ":modules:booleanconfiguration",
    ":modules:integerconfiguration",
    ":modules:enumconfiguration",
)
include(
    ":toggles-core",
    ":toggles-flow",
    ":toggles-flow-noop",
    ":toggles-prefs",
    ":toggles-prefs-noop"
)
```

- [ ] **Step 2: Confirm no Develocity references remain in this file**

Run:
```bash
grep -n "develocity\|gradle.enterprise" settings.gradle.kts
```
Expected: no output.

- [ ] **Step 3: Commit**

```bash
git add settings.gradle.kts
git commit -m "Replace Develocity with Pulse repo in settings.gradle.kts"
```

---

### Task 2: Update `build-logic/settings.gradle.kts`

**Files:**
- Modify: `build-logic/settings.gradle.kts`

- [ ] **Step 1: Remove Develocity plugin and block**

Replace the entire file with:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "build-logic"
include(":conventions")
```

- [ ] **Step 2: Confirm no Develocity references remain**

Run:
```bash
grep -n "develocity\|gradle.enterprise" build-logic/settings.gradle.kts
```
Expected: no output.

- [ ] **Step 3: Commit**

```bash
git add build-logic/settings.gradle.kts
git commit -m "Remove Develocity from build-logic settings"
```

---

### Task 3: Update `build.gradle.kts`

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Add Pulse plugin, remove Develocity block, add Pulse config**

Replace the entire file with:

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.com.android.tools.build.gradle)
        classpath(libs.com.google.dagger.hilt.android.gradle.plugin)
        classpath(libs.com.google.gms.google.services)
        classpath(libs.org.jetbrains.dokka.dokka.gradle.plugin)
        classpath(libs.app.cash.licensee.licensee.gradle.plugin)
    }
}

plugins {
    id("dev.premex.pulse") version "0.3.4"
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.github.triplet.play) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.com.autonomousapps.dependency.analysis)

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.io.gitlab.arturbosch.detekt) apply false
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator) apply false
    alias(libs.plugins.com.vanniktech.maven.publish) apply false
    alias(libs.plugins.org.jetbrains.dokka) apply false
    alias(libs.plugins.com.google.dagger.hilt.android) apply false
}

pulse {
    serverUrl.set("https://pulse.premex.se")
    organization.set("eelde")
}

dependencyAnalysis {
    issues {
        // Robolectric is referenced from test source via @Config and Shadows.shadowOf
        // (compile-time use), so DAGP's testImplementation -> testRuntimeOnly
        // demotion would break compilation.
        project(":modules:database:implementation") {
            onIncorrectConfiguration {
                exclude("org.robolectric:robolectric")
            }
            onRuntimeOnly {
                exclude("org.robolectric:robolectric")
            }
        }
        project(":toggles-flow") {
            onIncorrectConfiguration {
                exclude("org.robolectric:robolectric")
            }
            onRuntimeOnly {
                exclude("org.robolectric:robolectric")
            }
        }

        // hilt-android is applied by the toggles.hilt convention plugin and is
        // required by the dagger.hilt.android Gradle plugin even when DAGP can't
        // see direct usage in the wiring modules (they only use @Module / @InstallIn
        // from hilt-core but the Hilt Android plugin still needs hilt-android present).
        project(":modules:coroutines:wiring") {
            onUnusedDependencies {
                exclude("com.google.dagger:hilt-android")
            }
        }
        // The wiring modules expose Hilt @Provides on their public API surface, so
        // DAGP suggests api. The convention plugin uses implementation since most
        // consumers (including the apps) only need it for runtime DI wiring.
        project(":modules:database:wiring") {
            onIncorrectConfiguration {
                exclude("com.google.dagger:hilt-android")
            }
        }
        project(":modules:provider:wiring") {
            onIncorrectConfiguration {
                exclude("com.google.dagger:hilt-android")
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
```

- [ ] **Step 2: Confirm no Develocity references remain anywhere in the project**

Run:
```bash
grep -rn "develocity\|gradle.enterprise" --include="*.kts" --include="*.gradle" .
```
Expected: no output.

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "Add Pulse plugin and remove Develocity from build.gradle.kts"
```

---

### Task 4: Verify the build resolves and Pulse is active

- [ ] **Step 1: Run a lightweight build to confirm plugin resolution**

Run (generous timeout — first run downloads the Pulse plugin):
```bash
./gradlew help
```
Expected: Build succeeds. Look for a Pulse build scan URL printed at the end of the output (e.g. `https://pulse.premex.se/...`). No `develocity` errors.

- [ ] **Step 2: If `./gradlew help` prints a Pulse scan link, the migration is complete. Open a PR.**

```bash
git push -u origin claude/intelligent-merkle-e669cd
gh pr create \
  --title "Replace Develocity with Premex Pulse" \
  --body "$(cat <<'EOF'
## Summary

- Replaces `com.gradle.develocity` with `dev.premex.pulse` (0.3.4) across all Gradle files.
- Adds the Pulse Maven repo to `pluginManagement` in `settings.gradle.kts`.
- Removes all `develocity {}` blocks from `settings.gradle.kts`, `build-logic/settings.gradle.kts`, and `build.gradle.kts`.
- Pulse config: server `https://pulse.premex.se`, org `eelde`.
- Remote build cache and API token left for a follow-up PR.

## Test plan

- [ ] `./gradlew help` succeeds and prints a Pulse scan URL
- [ ] No `develocity` string remains in any `.kts` or `.gradle` file
- [ ] CI passes (detekt + unit tests)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```
