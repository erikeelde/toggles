# Lint Baseline Cleanup Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all lint and detekt warnings suppressed by baseline files and remove stale baselines.

**Architecture:** Fix warnings by category (one commit per category), ordered simplest-first. Some baselines are stale (code already fixed) and just need regeneration. After all fixes, regenerate baselines to confirm they're clean.

**Tech Stack:** Kotlin, Android (AGP 9), Jetpack Compose, Hilt, Room, Coroutines

**Spec:** `docs/superpowers/specs/2026-03-14-lint-baseline-cleanup-design.md`

---

## Chunk 1: Simple Fixes (Tasks 1-3)

### Task 1: ConvertToWebp

**Files:**
- Modify: `toggles-sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`

- [ ] **Step 1: Convert PNG to WebP**

```bash
cwebp toggles-sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png -o toggles-sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
rm toggles-sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png
```

If `cwebp` is not available, use any image tool or Android Studio's built-in conversion. The key requirement is removing the `.png` and replacing it with `.webp`.

- [ ] **Step 2: Update any XML references**

Check `toggles-sample/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` and any other XML that references `ic_launcher_round` to ensure it still resolves correctly. Usually the resource name stays the same (Android resolves by name, not extension), so no XML changes should be needed.

- [ ] **Step 3: Verify build**

```bash
./gradlew :toggles-sample:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add toggles-sample/src/main/res/mipmap-xxxhdpi/
git commit -m "Convert ic_launcher_round.png to WebP format"
```

---

### Task 2: FullyQualifiedResource

**Files:**
- Modify: `modules/applications/src/main/java/se/eelde/toggles/applications/ApplicationEntry.kt`

The file uses fully qualified resource references like `se.eelde.toggles.composetheme.R.color.toggles_blue` and `se.eelde.toggles.composetheme.R.drawable.ic_settings_white_24dp`. Fix by adding an import alias and using unqualified calls.

- [ ] **Step 1: Add import alias and replace qualified references**

Add this import:
```kotlin
import se.eelde.toggles.composetheme.R as ComposeThemeR
```

Then replace:
- Line 114: `colorResource(id = se.eelde.toggles.composetheme.R.color.toggles_blue)` → `colorResource(id = ComposeThemeR.color.toggles_blue)`
- Line 123: `painterResource(id = se.eelde.toggles.composetheme.R.drawable.ic_settings_white_24dp)` → `painterResource(id = ComposeThemeR.drawable.ic_settings_white_24dp)`
- Line 139: `painterResource(id = se.eelde.toggles.composetheme.R.drawable.ic_oss)` → `painterResource(id = ComposeThemeR.drawable.ic_oss)`

- [ ] **Step 2: Verify build**

```bash
./gradlew :modules:applications:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add modules/applications/
git commit -m "Replace fully qualified resource references with import alias"
```

---

### Task 3: ComposeModifierMissing

**Files:**
- Modify: `modules/oss/src/main/java/se/eelde/toggles/oss/ErrorView.kt`
- Modify: `modules/oss/src/main/java/se/eelde/toggles/oss/LoadingView.kt`

Add `modifier: Modifier = Modifier` parameter to each composable and apply it to the root layout.

- [ ] **Step 1: Fix ErrorView.kt**

Both overloads need a modifier parameter. The `ErrorView(content)` overload has a `Column` with `Modifier.fillMaxSize()` as root — change to use the passed modifier combined with fillMaxSize. The `ErrorView(errorString)` overload delegates to the content overload, so just pass the modifier through.

```kotlin
@Composable
fun ErrorView(errorString: String, modifier: Modifier = Modifier) =
    ErrorView(modifier = modifier) {
        androidx.compose.material3.Text(text = errorString)
    }

@Composable
fun ErrorView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... rest unchanged
    }
}
```

Note: The `ErrorView(content)` overload reorders parameters to put `modifier` before the trailing lambda `content`.

- [ ] **Step 2: Fix LoadingView.kt**

Same pattern:

```kotlin
@Composable
fun LoadingView(loadingTitle: String, modifier: Modifier = Modifier) =
    LoadingView(modifier = modifier) {
        Text(text = loadingTitle)
    }

@Composable
fun LoadingView(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... rest unchanged
    }
}
```

- [ ] **Step 3: Update callers if needed**

Search for usages of `ErrorView(` and `LoadingView(` across the codebase. Since the new parameter has a default value, existing callers should compile without changes. Verify with:

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add modules/oss/
git commit -m "Add Modifier parameter to ErrorView and LoadingView composables"
```

---

## Chunk 2: Not-Null Operator Cleanup (Tasks 4-5)

### Task 4: Replace !! in TogglesProvider and related provider code

**Files:**
- Modify: `modules/provider/implementation/src/main/java/se/eelde/toggles/provider/TogglesProvider.kt`
- Modify: `modules/provider/implementation/src/main/java/se/eelde/toggles/provider/PackageManagerWrapper.kt`
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesPredefinedConfigurationValue.kt`

- [ ] **Step 1: Fix TogglesProvider.kt — context!! usage**

Replace all `context!!` with `requireNotNull(context)`. There are instances at lines 58, 202-203, 210, 305, 386, 407, 419.

For line 58 (lazy init):
```kotlin
entryPointBuilder.build(requireNotNull(context))
```

For lines 202-203 (notification URI):
```kotlin
if (context != null) {
    cursor.setNotificationUri(requireNotNull(context).contentResolver, uri)
}
```
Actually this is redundant — if context is checked for null on line 202, just use `context!!` is safe but the lint rule still flags it. Better: use `context?.let { cursor.setNotificationUri(it.contentResolver, uri) }` or just `context?.contentResolver?.let { cursor.setNotificationUri(it, uri) }`.

Simplest fix — replace the null check + `!!` pattern:
```kotlin
context?.let { ctx ->
    cursor.setNotificationUri(ctx.contentResolver, uri)
}
```

For line 210:
```kotlin
return callingApplication.packageName == requireNotNull(context).packageName
```

For lines 305, 386, 407, 419 (contentResolver notifications):
```kotlin
requireNotNull(context).contentResolver.notifyInsert(...)
requireNotNull(context).contentResolver.notifyUpdate(...)
requireNotNull(context).contentResolver.notifyChange(...)
```

- [ ] **Step 2: Fix TogglesProvider.kt — uri.lastPathSegment!! usage**

Lines 129, 138, 146, 153, 164, 171, 338, 348, 361, 404, 416. Replace with `requireNotNull(uri.lastPathSegment)`:

```kotlin
// Line 129, 138: java.lang.Long.valueOf(uri.lastPathSegment!!)
java.lang.Long.valueOf(requireNotNull(uri.lastPathSegment))

// Line 146, 153: configurationDao.getToggle(uri.lastPathSegment!!, ...)
configurationDao.getToggle(requireNotNull(uri.lastPathSegment), ...)

// Line 164, etc: uri.lastPathSegment!!
requireNotNull(uri.lastPathSegment)
```

- [ ] **Step 3: Fix TogglesProvider.kt — values!! and toggle.value!! usage**

Line 224: `Toggle.fromContentValues(values!!)` → `Toggle.fromContentValues(requireNotNull(values))`
Line 266: `TogglesPredefinedConfigurationValue.fromContentValues(values!!)` → same pattern
Line 272: `fullConfig.value!!` → `requireNotNull(fullConfig.value)`
Line 278, 290, 335, 358, 369: same `values!!` pattern → `requireNotNull(values)`
Line 340: `toggle.value!!` → `requireNotNull(toggle.value)`

- [ ] **Step 4: Fix TogglesProvider.kt — callingApplicationPackageName!!**

Lines 89, 94, 96 are in `getCallingApplication()`. Replace:
```kotlin
val packageName = requireNotNull(packageManagerWrapper.callingApplicationPackageName)
```
Extract to a local variable used in all three places:
```kotlin
private fun getCallingApplication(applicationDao: ProviderApplicationDao): TogglesApplication =
    synchronized(this) {
        val callingPackageName = requireNotNull(packageManagerWrapper.callingApplicationPackageName)
        var togglesApplication: TogglesApplication? =
            applicationDao.loadByPackageName(callingPackageName)

        if (togglesApplication == null) {
            togglesApplication = TogglesApplication(
                id = 0,
                packageName = callingPackageName,
                applicationLabel = packageManagerWrapper.applicationLabel,
                shortcutId = callingPackageName,
            )
            // ... rest unchanged
        }
        return togglesApplication
    }
```

- [ ] **Step 5: Fix TogglesProvider.kt — Date() usage**

Lines 495, 506: `Date()` and `Date(Date().time - oneSecond)`. These will be handled in Task 7 (Date→Instant). Skip for now.

- [ ] **Step 6: Fix PackageManagerWrapper.kt**

Line 18: `callingApplicationPackageName!!` — replace with `requireNotNull(callingApplicationPackageName)`.

- [ ] **Step 7: Fix TogglesPredefinedConfigurationValue.kt**

Line 53: `values.getAsLong(...)!!` → `requireNotNull(values.getAsLong(...))`
Line 56: same pattern.

- [ ] **Step 8: Verify build and tests**

```bash
./gradlew :modules:provider:implementation:assembleDebug :modules:provider:implementation:test :modules:database:implementation:assembleDebug
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 9: Commit**

```bash
git add modules/provider/ modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesPredefinedConfigurationValue.kt
git commit -m "Replace !! with requireNotNull in provider and database code"
```

---

### Task 5: Replace !! in app modules, published libraries, and remaining files

**Files:**
- Modify: `modules/configurations/src/main/java/se/eelde/toggles/configurations/ConfigurationListView.kt`
- Modify: `modules/configurations/src/main/java/se/eelde/toggles/configurations/ConfigurationViewModel.kt`
- Modify: `modules/configurations/src/main/java/se/eelde/toggles/configurations/ConfigurationsEntry.kt`
- Modify: `toggles-app/src/main/java/se/eelde/toggles/dialogs/scope/ScopeView.kt`
- Modify: `toggles-app/src/main/java/se/eelde/toggles/dialogs/scope/ScopeViewModel.kt`
- Modify: `toggles-flow/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt`
- Modify: `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt`
- Modify: `toggles-sample/src/main/java/se/eelde/toggles/example/toggles2/Toggles2.kt`

- [ ] **Step 1: Fix ConfigurationListView.kt**

Line 40: `configuration.configurationValues!!` → `requireNotNull(configuration.configurationValues)`
Line 43: same
Line 65: `configuration.key!!` → `requireNotNull(configuration.key)`
Lines 134, 140, 146, 152: `selectedScope!!.id` → `requireNotNull(selectedScope).id`

- [ ] **Step 2: Fix ConfigurationViewModel.kt**

Line 78: `applicationDao.getApplication(applicationId)!!` → `requireNotNull(applicationDao.getApplication(applicationId))`

- [ ] **Step 3: Fix ConfigurationsEntry.kt**

Lines 116, 133, 148, 158: `uiState.value.application!!` → `requireNotNull(uiState.value.application)`

Extract to a local val in each usage context for readability if used multiple times in the same block.

- [ ] **Step 4: Fix ScopeView.kt**

Line 140: `viewState.selectedScope!!` → `requireNotNull(viewState.selectedScope)`

- [ ] **Step 5: Fix ScopeViewModel.kt**

Line 74: `partialViewState.scopes.maxByOrNull { it.timeStamp }!!` → `requireNotNull(partialViewState.scopes.maxByOrNull { it.timeStamp })`

- [ ] **Step 6: Fix TogglesImpl.kt (toggles-flow)**

Line 108: `type.enumConstants!!` → `requireNotNull(type.enumConstants)`

- [ ] **Step 7: Fix TogglesPreferencesImpl.kt (toggles-prefs)**

Line 101: `type.enumConstants!!` → `requireNotNull(type.enumConstants)`

- [ ] **Step 8: Fix Toggles2.kt (toggles-sample)**

Line 66: `type.enumConstants!!` → `requireNotNull(type.enumConstants)`
Line 109: `configurationUri.lastPathSegment!!` → `requireNotNull(configurationUri.lastPathSegment)`

- [ ] **Step 9: Verify build and tests**

```bash
./gradlew assembleDebug test
```

Expected: BUILD SUCCESSFUL, all tests pass. Set timeout to 600000ms.

- [ ] **Step 10: Commit**

```bash
git add modules/configurations/ toggles-app/ toggles-flow/ toggles-prefs/ toggles-sample/
git commit -m "Replace !! with requireNotNull across app and library modules"
```

---

## Chunk 3: Date to Instant Migration (Tasks 6-7)

### Task 6: Migrate database layer from Date to Instant

**Files:**
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/RoomDateConverter.kt` (rename to `RoomInstantConverter.kt`)
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesConfiguration.kt`
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesScope.kt`
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesDatabase.kt`
- Modify: `modules/database/implementation/src/main/java/se/eelde/toggles/database/dao/application/TogglesConfigurationDao.kt`

No Room schema migration is needed — the underlying column type remains `Long` (epoch millis).

- [ ] **Step 1: Create RoomInstantConverter.kt (replacing RoomDateConverter)**

Delete `RoomDateConverter.kt` and create `RoomInstantConverter.kt`:

```kotlin
package se.eelde.toggles.database

import androidx.room.TypeConverter
import java.time.Instant

class RoomInstantConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return if (value == null) null else Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
```

- [ ] **Step 2: Update TogglesDatabase.kt**

Line 27: `@TypeConverters(RoomDateConverter::class)` → `@TypeConverters(RoomInstantConverter::class)`

- [ ] **Step 3: Update TogglesConfiguration.kt entity**

Line 41: `var lastUse: Date = Date()` → `var lastUse: Instant = Instant.now()`
Line 44: `this.lastUse = Date()` → `this.lastUse = Instant.now()`
Update import: `java.util.Date` → `java.time.Instant`

- [ ] **Step 4: Update TogglesScope.kt entity**

Line 39: `var timeStamp: Date` → `var timeStamp: Instant`
Line 47: `Date()` → `Instant.now()`
Update import: `java.util.Date` → `java.time.Instant`

- [ ] **Step 5: Update TogglesConfigurationDao.kt**

Line 124: `suspend fun touch(configurationId: Long, date: Date)` → `suspend fun touch(configurationId: Long, date: Instant)`
Update import: `java.util.Date` → `java.time.Instant`

- [ ] **Step 6: Verify database module builds**

```bash
./gradlew :modules:database:implementation:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add modules/database/implementation/
git commit -m "Migrate database layer from java.util.Date to java.time.Instant"
```

---

### Task 7: Migrate published API and remaining code from Date to Instant

**Files:**
- Modify: `toggles-core/src/main/java/se/eelde/toggles/core/ToggleScope.kt`
- Modify: `modules/booleanconfiguration/src/main/java/se/eelde/toggles/booleanconfiguration/BooleanValueViewModel.kt`
- Modify: `modules/enumconfiguration/src/main/java/se/eelde/toggles/enumconfiguration/EnumValueViewModel.kt`
- Modify: `modules/integerconfiguration/src/main/java/se/eelde/toggles/integerconfiguration/IntegerValueViewModel.kt`
- Modify: `modules/stringconfiguration/src/main/java/se/eelde/toggles/stringconfiguration/StringValueViewModel.kt`
- Modify: `modules/provider/implementation/src/main/java/se/eelde/toggles/provider/TogglesProvider.kt`
- Modify: `toggles-app/src/main/java/se/eelde/toggles/dialogs/scope/ScopeViewModel.kt`

- [ ] **Step 1: Update ToggleScope.kt (toggles-core)**

This is the published API type. Change all Date references to Instant:

```kotlin
import java.time.Instant
// remove: import java.util.Date

public class ToggleScope private constructor(
    public val id: Long = 0,
    public val name: String,
    public val timeStamp: Instant,
) {
    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        public lateinit var name: String

        @set:JvmSynthetic
        public lateinit var timeStamp: Instant

        public fun setId(id: Long): Builder = apply { this.id = id }
        public fun setName(name: String): Builder = apply { this.name = name }
        public fun setTimeStamp(timeStamp: Instant): Builder = apply { this.timeStamp = timeStamp }
        public fun build(): ToggleScope = ToggleScope(id = id, name = name, timeStamp = timeStamp)
    }

    public fun copy(
        id: Long = this.id,
        name: String = this.name,
        timeStamp: Instant = this.timeStamp,
    ): ToggleScope = ToggleScope(id = id, name = name, timeStamp = timeStamp)

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.ToggleScope.COL_ID, id)
        put(ColumnNames.ToggleScope.COL_NAME, name)
        put(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP, timeStamp.toEpochMilli())
    }

    // equals, hashCode, toString unchanged (they use structural equality)

    public companion object {
        @JvmStatic
        public fun fromContentValues(contentValues: ContentValues): ToggleScope {
            return ToggleScope(
                id = contentValues.getAsLong(ColumnNames.ToggleScope.COL_ID),
                name = contentValues.getAsString(ColumnNames.ToggleScope.COL_NAME),
                timeStamp = Instant.ofEpochMilli(contentValues.getAsLong(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP))
            )
        }

        @JvmStatic
        public fun fromCursor(cursor: Cursor): ToggleScope {
            return ToggleScope(
                id = cursor.getLongOrThrow(ColumnNames.ToggleScope.COL_ID),
                name = cursor.getStringOrThrow(ColumnNames.ToggleScope.COL_NAME),
                timeStamp = Instant.ofEpochMilli(cursor.getLongOrThrow(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP))
            )
        }
    }
}
```

- [ ] **Step 2: Update ViewModels — replace Date() with Instant.now()**

In each of these files, change `import java.util.Date` → `import java.time.Instant` and replace `Date()` with `Instant.now()`:

- `BooleanValueViewModel.kt` line 138: `configurationDao.touch(configurationId, Date())` → `configurationDao.touch(configurationId, Instant.now())`
- `EnumValueViewModel.kt` line 158: same pattern
- `IntegerValueViewModel.kt` line 140: same pattern
- `StringValueViewModel.kt` line 138: same pattern

- [ ] **Step 3: Update TogglesProvider.kt — replace Date() with Instant.now()**

Line 495: `scope.timeStamp = Date(Date().time - oneSecond)` → `scope.timeStamp = Instant.now().minusMillis(oneSecond.toLong())`
Line 506: `developmentScope.timeStamp = Date()` → `developmentScope.timeStamp = Instant.now()`
Update import: `java.util.Date` → `java.time.Instant`

- [ ] **Step 4: Update ScopeViewModel.kt**

Line 83: `togglesScope.timeStamp = Date()` → `togglesScope.timeStamp = Instant.now()`
Update import: `java.util.Date` → `java.time.Instant`

- [ ] **Step 5: Verify full build and tests**

```bash
./gradlew assembleDebug test
```

Expected: BUILD SUCCESSFUL, all tests pass. Set timeout to 600000ms.

- [ ] **Step 6: Commit**

```bash
git add toggles-core/ modules/booleanconfiguration/ modules/enumconfiguration/ modules/integerconfiguration/ modules/stringconfiguration/ modules/provider/ toggles-app/
git commit -m "Migrate from java.util.Date to java.time.Instant (breaking API change)"
```

---

## Chunk 4: Baseline Cleanup and Final Verification (Task 8)

### Task 8: Regenerate baselines and verify clean state

- [ ] **Step 1: Run detekt to regenerate baselines**

```bash
./gradlew detekt
```

Note: detektMain may be unavailable (AGP 9 issue tracked in detekt#8320). If detekt passes, the baselines should be clean. If it fails on unrelated issues, investigate.

- [ ] **Step 2: Check for remaining baseline issues**

```bash
find . -name "*baseline*" \( -name "*.xml" \) -exec grep -l "<issue\|<ID\|CurrentIssues" {} \;
```

If any baselines still have entries, investigate whether the issues are:
- Fixed but baseline not regenerated → clear the baseline
- Genuine remaining issues → fix them
- False positives → add `@Suppress` with comment explaining why

- [ ] **Step 3: Clear empty/stale lint baselines**

For lint baselines that are now empty (no `<issue>` elements), replace content with empty baseline:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<issues format="6" by="lint" type="baseline" client="gradle" dependencies="false" name="AGP (9.1.0)" variant="all" version="9.1.0">

</issues>
```

Or remove the baseline file and the `baseline` reference in the module's `build.gradle.kts` if applicable.

- [ ] **Step 4: Clear stale detekt baselines**

For detekt baselines with no remaining `<ID>` entries, replace with empty baseline:

```xml
<?xml version="1.0" ?>
<SmellBaseline>
  <ManuallySuppressedIssues></ManuallySuppressedIssues>
  <CurrentIssues></CurrentIssues>
</SmellBaseline>
```

- [ ] **Step 5: Run full check**

```bash
./gradlew check
```

Expected: BUILD SUCCESSFUL. Set timeout to 600000ms (10 min).

- [ ] **Step 6: Verify published libraries build**

```bash
./gradlew publishToMavenLocal
```

Expected: BUILD SUCCESSFUL. Set timeout to 600000ms.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "Clean up stale lint and detekt baseline files"
```
