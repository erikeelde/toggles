# Replace Develocity with Premex Pulse

## Summary

Replace the `com.gradle.develocity` Gradle plugin with `dev.premex.pulse` (Premex's build intelligence service). Three files are touched; no version catalog changes needed.

## Changes

### `settings.gradle.kts`

- Add Pulse Maven repo to `pluginManagement`, scoped to `dev.premex.pulse.*`:
  ```
  maven {
      url = uri("https://artifacts.premex.se/api/maven/premex/pulse/")
      content { includeGroupByRegex("dev\\.premex\\.pulse.*") }
  }
  ```
- Remove `id("com.gradle.develocity") version "4.4.0"` from `plugins` block.
- Remove `develocity { buildScan { ... } }` block.
- `buildCache` block is unchanged (local only, remote disabled).

### `build-logic/settings.gradle.kts`

- Remove `id("com.gradle.develocity") version "4.4.0"` from `plugins` block.
- Remove `develocity { buildScan { ... } }` block.
- No Pulse plugin added here — build-logic is a composite build; its tasks appear in the main build's Pulse scan automatically.

### `build.gradle.kts`

- Add `id("dev.premex.pulse") version "0.3.4"` to `plugins` block.
- Remove `develocity { buildScan { ... } }` block.
- Add Pulse config block:
  ```kotlin
  pulse {
      serverUrl.set("https://pulse.premex.se")
      organization.set("eelde")
  }
  ```

## Out of scope

- Remote build cache via Pulse (currently disabled; can be a follow-up once the plugin is live).
- API token configuration for CI.

## Verification

After applying changes, run `./gradlew assembleDebug` and confirm a Pulse build scan link is printed and no Develocity references remain.
