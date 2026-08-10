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
./gradlew :modules:agent:implementation:testDebugUnitTest --tests "se.eelde.toggles.agent.AgentDaoTest"
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

**Filtering tests requires the variant task.** `--tests` works on `testDebugUnitTest`, not on the aggregate `test` lifecycle task, which will reject it.

**Capture Gradle's exit code explicitly:**

```bash
./gradlew check > /tmp/check.log 2>&1; echo "EXIT=$?"; tail -20 /tmp/check.log
```

Piping Gradle straight into `tail` (or any other command) reports the *pipeline's* exit status — `tail` succeeding — not Gradle's. A `BUILD FAILED` then looks like a pass. This has masked real failures more than once, including a `NewApi` lint error that would have crashed at runtime on `minSdk` devices.

## Architecture

### Module Types

- **Published libraries** (`toggles-core/`, `toggles-flow/`, `toggles-prefs/`, and their `-noop` variants): Client libraries for consuming apps. Core communicates with ContentProvider; Flow provides reactive Kotlin Flow API; Prefs provides SharedPreferences-like API.
- **Apps** (`toggles-app/`, `toggles-sample/`): Main toggle management app and sample demonstrating library usage.
- **Internal modules** (`modules/`): Feature modules for the app using API/Implementation/Wiring separation pattern (e.g., `database/api`, `database/implementation`, `database/wiring`). Configuration type modules: `booleanconfiguration/`, `stringconfiguration/`, `integerconfiguration/`, `enumconfiguration/`.

`modules/provider/api` holds code shared between the two ContentProviders — `ScopeChain`/`ScopeResolution` and the `ContentResolver.notifyUpdate`/`notifyInsert` extensions. `modules/agent/` depends on it rather than on `provider/implementation`: the agent provider is a *peer* of `TogglesProvider`, not a consumer of it.

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

**Agent API** (`modules/agent/`) — a *second* ContentProvider on its own authority, `se.eelde.toggles.agentprovider`, for driving toggles from an AI agent over adb. Independent of the client-facing provider above.

- Reads go through `openFile`/`openPipeHelper` so `adb shell content read` receives raw JSON on stdout. `content query` would mangle values containing `,` or `=`; `content call` wraps output in `Result: Bundle[{…}]`.
- Mutations go through `call()`. **Both `openFile` and `call()` must check `CallerAuthorization` first** — the provider is necessarily `exported="true"` (adb cannot reach an unexported provider), so the uid 2000/0 check is the only thing restricting it, not the manifest.
- `/describe` is self-describing and version-tied; host tooling hardcodes only that one command. A test asserts documented argument names match what `AgentCallHandler` actually reads, so the two cannot drift.
- Gated globally on the `beta_agent_api` toggle, which the Toggles app registers through its own `toggles-flow` instance (see `MainViewModel`) and which defaults to **off**. `/describe` stays reachable when disabled so a caller can tell "installed but off" from "not installed".
- Per-application `agentControlEnabled` (default on) can be switched off from the app's per-application overflow menu.
- **Mutations must call `AgentChangeNotifier`.** `toggles-flow` observes `configurationUri()`, `toggleUri()` and `scopeUri()` with `notifyForDescendants = true`; writing to the database without notifying leaves a running app on a stale value until it restarts, which presents as flakiness rather than a missing call.
- `.claude/skills/toggles-agent/SKILL.md` documents the API for agents consuming it.

### Database Migrations

Room schemas are exported to `modules/database/implementation/schemas/` and **are tracked** — a new version's JSON must be committed alongside the migration.

- Adding a column needs `@ColumnInfo(defaultValue = "…")` on the entity, or the exported schema will not match the migration SQL and `runMigrationsAndValidate` fails.
- SQLite cannot add `NOT NULL` or a foreign key to an existing column, so those need the create-temp / `INSERT … SELECT` / drop / rename rebuild. `MIGRATION_9_10` and `MIGRATION_11_12` rebuild `configurationValue`; copy the *current* schema JSON's definition rather than an older migration, since the table has accumulated two foreign keys and two indices. Losing either silently undoes earlier work — assert the cascades still fire, not just that the columns exist.
- The first test run after adding a schema version can fail with `FileNotFoundException` for the new JSON: test assets are snapshotted before KSP writes it. Re-run; it resolves.
- Room enables `PRAGMA foreign_keys` in `onOpen`, which runs *after* `onUpgrade`, so cascades do not fire during a migration. A table rebuild will not silently delete child rows — but verify with a test seeding real data rather than assuming.

**Provider tests** use Robolectric with Hilt (`modules/provider/implementation/src/test/`), organized by endpoint:
- `configuration/` — tests for configuration CRUD by ID and key
- `configurationValue/` — tests for configuration value operations
- `scope/` — tests for scope queries
- Root package — tests for current configuration (legacy toggle API) and predefined values

## CI/CD

GitHub Actions in `.github/workflows/`:
- `pull-request.yml`: Runs detekt, unit tests, instrumentation tests on PRs
- `libraries-release.yml` / `app-release.yml`: Manual-trigger releases to Maven Central / Play Store

CI uses `warningsAsErrors=true` (`.github/ci-gradle.properties`).

## Kotlin Compatibility

Published libraries use [tapmoc](https://github.com/GradleUp/tapmoc) (`configureKotlinCompatibility`) in `AndroidLibraryConventionPlugin` to lock metadata version and `kotlin-stdlib` to the version specified by `kotlinCompatibility` in `libs.versions.toml`. This ensures consumers on older Kotlin versions can use the libraries. The `kotlinCompatibility` entry has a `@keep` annotation so `versionCatalogUpdate` doesn't remove it (it's referenced programmatically via `findVersion()`, not by a library/plugin declaration).

## Dependency Upgrade Notes

AGP (Android Gradle Plugin), Kotlin, Hilt/Dagger, KSP, and triplet-play are tightly coupled and must be upgraded together. When running `versionCatalogUpdate`, review the diff carefully and keep these versions in sync.

Dependencies outside the version catalog that need manual updates: `dev.premex.pulse` in `build.gradle.kts`, `org.gradle.toolchains.foojay-resolver-convention` in both `settings.gradle.kts` and `build-logic/settings.gradle.kts`, plus GitHub Actions versions in `.github/workflows/` and `.github/actions/`.

### AGP 9 Convention Plugin Notes

- **Built-in Kotlin**: AGP 9 bundles Kotlin — convention plugins do not apply `org.jetbrains.kotlin.android`. The `kotlin-gradle-plugin` is still a `compileOnly` dependency in `build-logic/conventions/build.gradle.kts` for access to `KotlinAndroidProjectExtension`.
- **`CommonExtension` has no type parameters**: Use direct property access (e.g. `commonExtension.compileSdk = 36`, `commonExtension.lint.apply { ... }`) instead of DSL lambda blocks.
- **`detektMain` unavailable**: AGP 9 built-in Kotlin changes source set registration, breaking `detektMain`. Tracked in [detekt#8320](https://github.com/detekt/detekt/issues/8320). Currently commented out in CI — restore when fixed.
- **BCV tasks unavailable**: `org.jetbrains.kotlinx.binary-compatibility-validator` (0.18.1) does not register `apiCheck`/`apiDump` tasks with AGP 9. The `.api` files must be maintained manually until BCV adds AGP 9 support. Verify changes with `javap -public` against compiled classes. Note `.api` files are also blind to Kotlin nullability — JVM erasure makes `String` and `String?` produce an identical `getValue ()Ljava/lang/String;` — so tightening a published type is a source break that no tooling will flag.
- **Stale lint state**: a crash reading `Unexpected failure during lint analysis … this is a bug in lint` and naming a missing KSP-generated Hilt file (e.g. `DaggerDefault_HiltComponents_SingletonC.java`) is stale incremental state, not a code defect. Delete that module's `build/` directory and re-run before investigating; the message points at the wrong culprit.
- **Lint baselines are per-module and committed**. A new module generates one on first lint run and *aborts the build* with "Aborting build since new baseline file was created" — re-run after committing it. Inspect the contents first: they should only ever contain pre-existing findings from dependencies, never anything from the new module's own code.
- **detekt `TooManyFunctions`** fires at `count >= 11`, not `> 11`. Prefer extracting a helper class or top-level functions over a blanket suppression; class-level `@Suppress("TooManyFunctions")` has precedent where decomposition would hurt readability.