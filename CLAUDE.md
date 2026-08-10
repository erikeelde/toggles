# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Toggles is a multi-module Android library for feature switching. It stores feature toggles in an external app via ContentProvider, persisting across app reinstalls. Published libraries (`toggles-core`, `toggles-flow`, `toggles-prefs`) are on Maven Central under `se.eelde.toggles`. Each has a `-noop` variant for release builds that returns defaults without connecting to the Toggles app.

## Build Commands

Requires **Java 21** and **Android SDK** (API 36). Gradle 9.4.0 with Kotlin DSL.

```bash
# PR validation (interactive — mirrors CI)
./scripts/pr_check.sh

# Build
./gradlew assembleDebug                          # All debug APKs (~5-8 min)
./gradlew assembleAndroidTest                    # Compile instrumentation tests

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
- **Static analysis**: Detekt (config: `config/detekt/detekt.yml`), slack-lint (custom lint checks — denies `java.util.Date` via `DenyListedApi`, denies unconditional `Log` calls in libraries via `LogConditional`)

### ContentProvider Architecture

The core mechanism uses Android ContentProvider for inter-process communication between the Toggles app (provider) and consuming apps (clients). The `modules/provider/` module implements the provider side; `toggles-core/` implements the client side.

**Old API** (`Toggle`, `ToggleValue`) — flat model where a single `Toggle` object bundles configuration metadata (key, type) with its current value. Uses `toggleUri()` endpoints (`/currentConfiguration/...`). Insert auto-creates both the configuration and a default-scope value in one call. `toggleValueUri()` manages predefined allowed values. Still supported but being superseded. Published client: `toggles-flow` (`TogglesImpl`).

**New API** (`TogglesConfiguration`, `TogglesConfigurationValue`, `ToggleScope`) — normalized model that separates concerns:
- `TogglesConfiguration` (key, type) — the toggle definition, managed via `configurationUri()` endpoints (`/configuration/...`)
- `TogglesConfigurationValue` (configurationId, value, scope) — per-scope values, managed via `configurationValueUri()` endpoints (`/configuration/{id|key}/values`)
- `ToggleScope` — scopes (default + development auto-created), read-only via `scopeUri()` (`/scope`)

New code should use the new API. The new API enables multi-scope support (e.g. different values per environment).

**Library internal architecture** — both `toggles-flow` and `toggles-prefs` use a 3-layer decomposition:
- `TogglesProvider` (internal) — data access layer owning all ContentProvider interaction (queries, mutations, observation via `ContentObserver`/`callbackFlow`)
- `TogglesResolver` (internal) — business logic for scope-aware value resolution, auto-creation of missing configurations/values, and default mismatch detection
- `TogglesImpl` / `TogglesPreferencesImpl` (public) — thin facades implementing the public `Toggles` / `TogglesPreferences` interfaces

`ToggleState` (in `toggles-core`) bundles `TogglesConfiguration?`, `List<TogglesConfigurationValue>`, and `List<ToggleScope>` — used as the interchange type between layers and exposed in violation handler callbacks (`onMissingToggle`, `onDefaultMismatch`).

**URI endpoints** (defined in `TogglesProviderContract` in `toggles-core`):
- `configurationUri()` / `configurationUri(id: Long)` / `configurationUri(key: String)` — CRUD for configurations
- `configurationValueUri(id: Long)` / `configurationValueUri(key: String)` — configuration values. Insert/update only by ID; key variants are read-only
- `toggleUri()` / `toggleUri(id: Long)` / `toggleUri(key: String)` — legacy current configuration endpoint
- `toggleValueUri()` — predefined configuration values (insert-only, no query/update/delete)
- `scopeUri()` — scopes (read-only; default + development scopes auto-created per application)

**Provider tests** use Robolectric with Hilt (`modules/provider/implementation/src/test/`), organized by endpoint:
- `configuration/` — tests for configuration CRUD by ID and key
- `configurationValue/` — tests for configuration value operations
- `scope/` — tests for scope queries
- Root package — tests for current configuration (legacy toggle API) and predefined values

## CI/CD

GitHub Actions in `.github/workflows/`:
- `pull-request.yml` — detekt, unit tests and instrumentation tests on PRs
- `post-merge.yml` — on every push to `main`: `./gradlew check`, packages debug APKs, and publishes library **snapshots** to Maven Central
- `app-release.yml` — `workflow_dispatch` only. Runs release-drafter; it *creates or refreshes the draft GitHub release*, it does not publish anything
- `libraries-snapshot.yml` — `workflow_dispatch` only, for an ad-hoc snapshot publish
- `release_workflow.yml` / `libraries-release.yml` — both fire on `release: published`; see below

CI uses `warningsAsErrors=true` (`.github/ci-gradle.properties`).

## Releasing

**Releases are made by publishing a GitHub release. The tag prefix decides what ships.** Both release workflows listen to the same `release: published` event and gate on the tag, so they are mutually exclusive:

| Tag | Workflow that runs | What it does |
|---|---|---|
| `vX.Y.Z` | `release_workflow.yml` (`if: !startsWith(tag, 'lib/')`) | `./gradlew check`, then `:toggles-app:publishReleaseBundle` to Google Play and `assembleRelease`; attaches APKs to the release. `libraries-release` is **skipped**. |
| `lib/X.Y.Z` | `libraries-release.yml` (`if: startsWith(tag, 'lib/')`) | `publishAndReleaseToMavenCentral`. `release_workflow` is **skipped**. |

The app and the libraries are therefore versioned and released **independently**, and their tags normally sit on different commits.

### Versions come from git tags

`scripts/generate_versions.sh` derives `versions.properties` (generated, **gitignored — never edit it by hand**) from the tags reachable at `HEAD`:

- newest `v*` tag → `V_VERSION` / `V_VERSION_CODE` (the app)
- newest `lib/*` tag → `V_LIBRARY_VERSION` (the published libraries); with no tag at `HEAD` this becomes a `-SNAPSHOT`

`libraries-release.yml` calls the version action with `release: true`, which **requires `HEAD` to be exactly on a `lib/*` tag** — it exits with `--release but HEAD is N commit(s) ahead of lib/x.y.z — tag HEAD first` otherwise. `release_workflow.yml` does not use release mode.

### Releasing the app

1. Make sure `main` is green.
2. Release-drafter maintains a draft release as PRs merge; `app-release.yml` can be dispatched to refresh it.
3. Edit the draft — set the tag to the next `vX.Y.Z`, check the notes — and publish.
4. `release_workflow.yml` runs the checks and uploads to Play. Note `triplet-play` is configured with `releaseStatus DRAFT`, so it lands as a **draft in the Play Console** and still needs promoting there.

### Releasing the libraries

1. Publish a GitHub release whose tag is `lib/X.Y.Z`, targeting the commit to release.
2. `libraries-release.yml` runs `publishAndReleaseToMavenCentral`.

**Maven Central releases are permanent** — artifacts cannot be deleted or overwritten. Check the version is what you intend before publishing.

If a change only touches `toggles-app` or `modules/`, no library release is needed; the published libraries are `toggles-core`, `toggles-flow`, `toggles-prefs` and their `-noop` variants.

## Kotlin Compatibility

Published libraries use [tapmoc](https://github.com/GradleUp/tapmoc) (`configureKotlinCompatibility`) in `AndroidLibraryConventionPlugin` to lock metadata version and `kotlin-stdlib` to the version specified by `kotlinCompatibility` in `libs.versions.toml`. This ensures consumers on older Kotlin versions can use the libraries. The `kotlinCompatibility` entry has a `@keep` annotation so `versionCatalogUpdate` doesn't remove it (it's referenced programmatically via `findVersion()`, not by a library/plugin declaration).

## Dependency Upgrade Notes

AGP (Android Gradle Plugin), Kotlin, Hilt/Dagger, KSP, and triplet-play are tightly coupled and must be upgraded together. When running `versionCatalogUpdate`, review the diff carefully and keep these versions in sync.

Dependencies outside the version catalog that need manual updates: `dev.premex.pulse` in `build.gradle.kts`, `org.gradle.toolchains.foojay-resolver-convention` in both `settings.gradle.kts` and `build-logic/settings.gradle.kts`, plus GitHub Actions versions in `.github/workflows/` and `.github/actions/`.

### AGP 9 Convention Plugin Notes

- **Built-in Kotlin**: AGP 9 bundles Kotlin — convention plugins do not apply `org.jetbrains.kotlin.android`. The `kotlin-gradle-plugin` is still a `compileOnly` dependency in `build-logic/conventions/build.gradle.kts` for access to `KotlinAndroidProjectExtension`.
- **`CommonExtension` has no type parameters**: Use direct property access (e.g. `commonExtension.compileSdk = 36`, `commonExtension.lint.apply { ... }`) instead of DSL lambda blocks.
- **`detektMain` unavailable**: AGP 9 built-in Kotlin changes source set registration, breaking `detektMain`. Tracked in [detekt#8320](https://github.com/detekt/detekt/issues/8320). Currently commented out in CI — restore when fixed.
- **BCV tasks unavailable**: `org.jetbrains.kotlinx.binary-compatibility-validator` (0.18.1) does not register `apiCheck`/`apiDump` tasks with AGP 9. The `.api` files must be maintained manually until BCV adds AGP 9 support. Verify changes with `javap -public` against compiled classes.