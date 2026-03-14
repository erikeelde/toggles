# Lint Baseline Cleanup

Fix all lint and detekt warnings currently suppressed by baseline files, then remove the baselines.

## Approach

Fix warnings by category (one commit per category), ordered from simplest to most impactful. After each category is fixed, regenerate or remove the corresponding baseline entries.

## Categories

### 1. ConvertToWebp

**File:** `toggles-sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`

Convert PNG to WebP format.

### 2. FullyQualifiedResource

**File:** `modules/applications/src/main/java/se/eelde/toggles/applications/ApplicationEntry.kt`

Replace fully qualified `androidx.compose.ui.res.colorResource(...)` and `painterResource(...)` calls with imports and unqualified calls.

### 3. UseOrEmpty

Replace `?: ""` with `.orEmpty()` in configuration view files:

- `modules/booleanconfiguration/.../BooleanValueView.kt` — `viewState.title ?: ""`
- `modules/enumconfiguration/.../EnumValueView.kt` — `state.title ?: ""`
- `modules/integerconfiguration/.../IntegerValueView.kt` — `uiState.title ?: ""`
- `modules/stringconfiguration/.../StringValueView.kt` — `stringValue ?: ""`, `title ?: ""`

### 4. ComposeModifierMissing

Add `modifier: Modifier = Modifier` parameter to composable functions and apply it to the root layout:

- `modules/oss/.../ErrorView.kt` — both `ErrorView` overloads
- `modules/oss/.../LoadingView.kt` — both `LoadingView` overloads
- `toggles-sample` — if it has its own copies of `ErrorView`/`LoadingView`, fix those too

### 5. InjectDispatcher

Replace direct `Dispatchers.IO` usage with constructor-injected `@IoDispatcher dispatcher: CoroutineDispatcher` using the existing Hilt qualifier from `modules/coroutines/api`.

**Files:**
- `modules/booleanconfiguration/.../BooleanValueViewModel.kt`
- `modules/enumconfiguration/.../EnumValueViewModel.kt`
- `modules/integerconfiguration/.../IntegerValueViewModel.kt`
- `modules/stringconfiguration/.../StringValueViewModel.kt`
- `modules/provider/implementation/.../TogglesProvider.kt`
- `toggles-app/.../ScopeViewModel.kt`
- `toggles-app/.../ApplicationModule.kt`
- `toggles-sample/.../ApplicationModule.kt`

The `CoroutinesDispatchersModule` in `modules/coroutines/wiring` already provides `@IoDispatcher`. The `@Suppress("InjectDispatcher")` there is correct — it's the one place that legitimately references `Dispatchers.IO`.

### 6. AvoidUsingNotNullOperator / UnsafeCallOnNullableType

Replace `!!` with safe alternatives across production and test code.

**Strategy by context:**

- **`context!!`** in `TogglesProvider.kt` → `requireNotNull(context)` with descriptive message. ContentProvider context is guaranteed non-null after `onCreate()`, so `requireNotNull` is appropriate.
- **`uri.lastPathSegment!!`** → `requireNotNull(uri.lastPathSegment)`. The URI matcher guarantees a segment exists for matched patterns.
- **`packageManagerWrapper.callingApplicationPackageName!!`** → `requireNotNull(...)` with message.
- **`scope!!`, `values!!`, `defaultScope!!`, `scopeDao!!`** in provider → `requireNotNull(...)`.
- **`it.value!!`** in ViewModels → `requireNotNull(it.value)` or `checkNotNull(it.value)`.
- **`applicationDao.getApplication(id)!!`** → `requireNotNull(applicationDao.getApplication(id))`.
- **`configuration.configurationValues!!`**, **`configuration.key!!`** in UI → `requireNotNull(...)`.
- **`selectedScope!!`, `uiState.value.application!!`** in UI → `requireNotNull(...)`.
- **`partialViewState.scopes.maxByOrNull { it.timeStamp }!!`** in `ScopeViewModel.kt` → `requireNotNull(...)` or safe alternative.
- **`TogglesPredefinedConfigurationValue` cursor column access** (`COL_ID!!`, `COL_CONFIG_ID!!`) → `requireNotNull(...)`.

**Published library code:**
- **`TogglesImpl.kt`** (toggles-flow): `toggleUri().authority!!`, `uri!!`, `uri!!.lastPathSegment!!` → `requireNotNull(...)`.
- **`TogglesPreferencesImpl.kt`** (toggles-prefs): `uri!!`, `uri!!.lastPathSegment!!`, `type.enumConstants!!` → `requireNotNull(...)`.

**Test files:**
- `TogglesPreferencesReturnsProviderValues.kt` — `authority!!`, `values!!` (6+ instances)
- `CheckForProviders.kt` (androidTest) — `providerInfo!!.authority`
- All other test `!!` usage → `requireNotNull()` for clear error messages vs. bare NPE.

### 7. DenyListedApi (Date to Instant)

Replace `java.util.Date` with `java.time.Instant` throughout. This is a **breaking API change** for published libraries.

**Published API types (toggles-core):**

- `ToggleScope.kt`: Change `timeStamp: Date` → `timeStamp: Instant`
  - Property: `val timeStamp: Instant`
  - Builder: `lateinit var timeStamp: Instant`, `fun setTimeStamp(timeStamp: Instant)`
  - `copy()`: parameter type changes to `Instant`
  - DSL builder function: transitively affected through Builder
  - `toContentValues()`: `timeStamp.time` → `timeStamp.toEpochMilli()`
  - `fromContentValues()`: `Date(long)` → `Instant.ofEpochMilli(long)`
  - `fromCursor()`: same conversion
- `TogglesConfiguration.kt` (core): no Date fields, no changes needed

**Database entities (modules/database/implementation):**

- `TogglesConfiguration.kt` entity: `lastUse: Date = Date()` → `lastUse: Instant = Instant.now()`
- `TogglesScope.kt` entity: constructor `Date` parameter → `Instant`, `newScope()` factory: `Date()` → `Instant.now()`
- `RoomDateConverter.kt` → rename to `RoomInstantConverter.kt`:
  - `fromTimestamp(Long?) → Instant?`: `Instant.ofEpochMilli(value)`
  - `instantToTimestamp(Instant?) → Long?`: `instant?.toEpochMilli()`
  - Update `@TypeConverters` registration in the Room database class
- No Room schema migration needed — the underlying column type remains `Long` (epoch millis). Only the TypeConverter's Java type changes.

**DAO interfaces:**

- `configurationDao.touch(id, Date)` → change parameter type to `Instant`
- Any other DAO methods accepting `Date` parameters

**ViewModels:**

- `BooleanValueViewModel.kt`: `configurationDao.touch(id, Date())` → `Instant.now()`
- Same pattern in Enum/Integer/String ViewModels
- `TogglesProvider.kt`: any `Date()` construction → `Instant.now()`

**Published library variants:**

- `toggles-flow`, `toggles-flow-noop`, `toggles-prefs`, `toggles-prefs-noop` — reference `ToggleScope` from `toggles-core`, so they pick up the type change transitively. Check for any direct `Date` usage in these modules.

**toggles-sample:**

- `Toggles2.kt`: may construct Date objects → `Instant.now()`

**Breaking API surface for consumers:**

- `ToggleScope.timeStamp` property: `Date` → `Instant`
- `ToggleScope.Builder.timeStamp` property: `Date` → `Instant`
- `ToggleScope.Builder.setTimeStamp()` parameter: `Date` → `Instant`
- `ToggleScope.copy()` parameter: `Date` → `Instant`
- Any consumer code reading `scope.timeStamp` will get `Instant` instead of `Date`

## Baseline File Handling

After all fixes are applied, run lint and detekt to regenerate baselines. Ideally all baseline files become empty and can be removed or left as empty placeholders. If any warnings remain that are false positives or intentionally suppressed, document why in the baseline or via `@Suppress`.

Verify no baselines contain active issues:
```bash
find . -name "*baseline*" -exec grep -l "CurrentIssues\|<issue\|<ID>" {} \;
```

## Verification

After each category:
1. Build succeeds: `./gradlew assembleDebug`
2. Tests pass: `./gradlew test`
3. Lint/detekt pass or show reduced baseline: `./gradlew detekt` and check lint results

Final verification:
- `./gradlew check` passes
- All baseline files are empty or removed
- `./gradlew publishToMavenLocal` succeeds (validates published library API changes)
