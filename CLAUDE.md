# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Toggles is a multi-module Android library for feature switching. It stores feature toggles in an external app via ContentProvider, persisting across app reinstalls. Published libraries (`toggles-core`, `toggles-flow`, `toggles-prefs`) are on Maven Central under `se.eelde.toggles`. Each has a `-noop` variant for release builds that returns defaults without connecting to the Toggles app.

## Build Commands

Requires **Java 21** and **Android SDK** (API 35). Gradle 9.2.0 with Kotlin DSL.

```bash
# Build
./gradlew assembleDebug                          # All debug APKs (~5-8 min)

# Static analysis
./gradlew detekt                                 # Detekt (~2-5 min)
./gradlew versionCatalogFormat                   # Format version catalog

# Tests
./gradlew test                                   # All unit tests (~3-7 min)
./gradlew :toggles-core:test                     # Single module tests
./gradlew check                                  # All checks (~8-15 min)
./gradlew pixel6api35googleDebugAndroidTest       # Emulator tests (~10-20 min)

# Module-specific checks
./gradlew :toggles-core:check
./gradlew :toggles-flow:check
./gradlew :toggles-prefs:check

# Publishing
./gradlew publishToMavenLocal

# Dependency updates
./gradlew versionCatalogUpdate                   # Update versions in libs.versions.toml
./scripts/update_gradle_wrapper.sh               # Update Gradle wrapper
```

First build downloads dependencies and takes 15-25 minutes. Subsequent builds are 2-8 minutes. Set generous timeouts for Gradle commands (10+ minutes for simple tasks, 30+ for full checks).

## Architecture

### Module Types

- **Published libraries** (`toggles-core/`, `toggles-flow/`, `toggles-prefs/`, and their `-noop` variants): Client libraries for consuming apps. Core communicates with ContentProvider; Flow provides reactive Kotlin Flow API; Prefs provides SharedPreferences-like API.
- **Apps** (`toggles-app/`, `toggles-sample/`): Main toggle management app and sample demonstrating library usage.
- **Internal modules** (`modules/`): Feature modules for the app using API/Implementation/Wiring separation pattern (e.g., `database/api`, `database/implementation`, `database/wiring`). Configuration type modules: `booleanconfiguration/`, `stringconfiguration/`, `integerconfiguration/`, `enumconfiguration/`.

### Build Logic

Custom Gradle convention plugins in `build-logic/conventions/` provide consistent configuration:
- `toggles.android.application` / `toggles.android.library` / `toggles.android.module` — Android setup
- `toggles.android.compose.application` / `toggles.android.compose.module` — Compose configuration
- `toggles.hilt` — Hilt DI with KSP
- `toggles.detekt.common` / `toggles.detekt.library` — Static analysis

Version catalog: `gradle/libs.versions.toml`. Properties: `gradle.properties` (12GB heap configured).

### Key Frameworks

- **DI**: Hilt (Dagger)
- **Database**: Room ORM
- **UI**: Jetpack Compose with Material 3
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Navigation 3
- **Serialization**: Moshi (JSON), kotlinx-serialization
- **Static analysis**: Detekt (config: `config/detekt/detekt.yml`)

### ContentProvider Architecture

The core mechanism uses Android ContentProvider for inter-process communication between the Toggles app (provider) and consuming apps (clients). The `modules/provider/` module implements the provider side; `toggles-core/` implements the client side.

## CI/CD

GitHub Actions in `.github/workflows/`:
- `pull-request.yml`: Runs detekt, unit tests, instrumentation tests on PRs
- `libraries-release.yml` / `app-release.yml`: Manual-trigger releases to Maven Central / Play Store

CI uses `warningsAsErrors=true` (`.github/ci-gradle.properties`).

## Dependency Upgrade Notes

AGP (Android Gradle Plugin), Kotlin, Hilt/Dagger, KSP, and triplet-play are tightly coupled and must be upgraded together:
- **Hilt/Dagger 2.59+** requires AGP 9+
- **Kotlin 2.3+** produces metadata v2.3 which is incompatible with Hilt 2.57.x
- **triplet-play 4.0+** requires AGP 9+
- **AGP 9** removes type parameters from `CommonExtension` — convention plugins in `build-logic/` need updating

When running `versionCatalogUpdate`, review the diff carefully and keep these versions in sync. Update them as a group when doing the AGP 9 migration.