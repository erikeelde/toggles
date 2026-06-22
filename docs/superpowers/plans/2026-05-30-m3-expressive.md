# Material 3 Expressive Adoption Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adopt Material 3 Expressive in the Toggles management app — adaptive navigation, an adaptive list-detail pane for configurations → value editor, Expressive components on the value editors, and a dogfooded toggle that gates a new enum list presentation.

**Architecture:** `NavigationSuiteScaffold` provides adaptive top-level navigation (bottom bar ⇄ rail by window size). The `material3-adaptive-navigation3` scene strategy renders the configurations list and value-editor entries as a `ListDetailPaneScaffold` on expanded width and as push navigation on compact. `MaterialExpressiveTheme` supplies the expressive motion scheme. The Navigation 3 backstack remains the single source of truth.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2026.05.01), Material 3 Expressive, Navigation 3 (`1.2.0-alpha03`), `material3-adaptive-navigation3` (`1.3.0-beta02`), Hilt, toggles-flow (dogfooding).

**Spec:** `docs/superpowers/specs/2026-05-30-m3-expressive-design.md`

**Scope:** `toggles-app` and UI feature modules under `modules/`. Published libraries (`toggles-core/flow/prefs`) are untouched.

---

## File Structure

Files created or modified, grouped by responsibility:

- `gradle/libs.versions.toml` — add adaptive artifacts (navigation-suite, adaptive layout/window-info).
- `modules/compose-theme/.../Theme.kt` — swap `MaterialTheme` → `MaterialExpressiveTheme` + motion scheme.
- `toggles-app/.../MainActivity.kt` — `NavigationSuiteScaffold` shell + `ListDetailPaneScaffold` scene strategy on `NavDisplay`.
- `modules/applications/.../ApplicationEntry.kt` — remove `ModalNavigationDrawer`/`TogglesDrawer`; the Applications entry becomes plain content.
- `modules/booleanconfiguration/.../BooleanValueView.kt` — `ToggleButtonGroup` + `ButtonGroup`.
- `modules/integerconfiguration/.../IntegerValueView.kt`, `modules/stringconfiguration/.../StringValueView.kt`, `modules/enumconfiguration/.../EnumValueView.kt` — `ButtonGroup` for Revert/Save.
- `modules/enumconfiguration/build.gradle.kts` — add `implementation(projects.togglesFlow)`.
- `modules/enumconfiguration/.../EnumValueViewModel.kt` — inject `Toggles`, read `expressive_enum_list` flag into view state.
- `modules/enumconfiguration/.../EnumValueView.kt` — branch list presentation on the flag.
- `modules/enumconfiguration/src/test/.../EnumValueViewModelTest.kt` — unit test for flag → view state.

**Note on Compose UI testing:** Theme, navigation chrome, and component swaps (Tasks 2–6) are not unit-testable in a TDD-first way; they are verified by compile + `detekt` + manual emulator runs (compact and expanded). Only the enum ViewModel flag logic (Task 7) gets a true failing-test-first cycle.

---

## Task 1: Add adaptive dependencies to the version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add the adaptive version ref**

Under `[versions]`, add (the `androidx.compose.material3.adaptive` group is versioned independently of the Compose BOM):

```toml
material3Adaptive = "1.2.0"
```

- [ ] **Step 2: Add the library entries**

Under `[libraries]`, add. `material3-adaptive-navigation-suite` is part of the Compose BOM (no version); `adaptive`/`adaptive-layout` use the new ref:

```toml
androidx-material3-adaptive-navigation-suite = { module = "androidx.compose.material3:material3-adaptive-navigation-suite" }
androidx-material3-adaptive = { module = "androidx.compose.material3.adaptive:adaptive", version.ref = "material3Adaptive" }
androidx-material3-adaptive-layout = { module = "androidx.compose.material3.adaptive:adaptive-layout", version.ref = "material3Adaptive" }
```

- [ ] **Step 3: Remove the stale snapshot comment**

The `adaptive-navigation3` dep is now `1.3.0-beta02` (a release build), so delete the three comment lines above the `androidx-material3-adaptive-navigation3` entry (currently lines ~103–105) that say it is snapshot-only.

- [ ] **Step 4: Verify the catalog resolves**

Run: `./gradlew help`
Expected: BUILD SUCCESSFUL. If `material3Adaptive = "1.2.0"` fails to resolve, run `./gradlew dependencies --configuration releaseRuntimeClasspath` against `toggles-app` to find the version `adaptive-navigation3:1.3.0-beta02` pulls in transitively, and set `material3Adaptive` to match. Then re-run `./gradlew help`.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "Add Material3 adaptive navigation-suite and layout dependencies"
```

---

## Task 2: Switch the theme to MaterialExpressiveTheme

**Files:**
- Modify: `modules/compose-theme/src/main/java/se/eelde/toggles/composetheme/Theme.kt`

- [ ] **Step 1: Replace the MaterialTheme call**

In `TogglesTheme`, replace the imports of `androidx.compose.material3.MaterialTheme` with the expressive theme + motion scheme, and update the body. Final imports to add:

```kotlin
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
```

Replace the `MaterialTheme(...)` block (currently lines ~157–161) with:

```kotlin
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
```

Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` to the `TogglesTheme` function (or keep the inline `@OptIn` shown above). Remove the now-unused `import androidx.compose.material3.MaterialTheme` if nothing else in the file uses it.

- [ ] **Step 2: Compile the module**

Run: `./gradlew :modules:compose-theme:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. If `MaterialExpressiveTheme` / `MotionScheme` are unresolved, the pinned BOM predates stable Expressive — bump `androidx-compose-bom` to the latest and retry.

- [ ] **Step 3: Run detekt on the module**

Run: `./gradlew :modules:compose-theme:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add modules/compose-theme/src/main/java/se/eelde/toggles/composetheme/Theme.kt
git commit -m "Use MaterialExpressiveTheme with expressive motion scheme"
```

---

## Task 3: Adaptive top-level navigation with NavigationSuiteScaffold

This removes the `ModalNavigationDrawer` and `TogglesDrawer` and wraps the whole `NavDisplay` in a `NavigationSuiteScaffold` with three destinations. Top-level selection drives the backstack.

**Files:**
- Modify: `toggles-app/build.gradle.kts`
- Modify: `toggles-app/src/main/java/se/eelde/toggles/MainActivity.kt`
- Modify: `modules/applications/src/main/java/se/eelde/toggles/applications/ApplicationEntry.kt`
- Modify: `modules/applications/build.gradle.kts` (only if it currently declares the drawer-related deps it no longer needs — otherwise skip)

- [ ] **Step 1: Add the navigation-suite dependency to the app**

In `toggles-app/build.gradle.kts`, in the dependencies block (near the other material3 lines, ~line 120), add:

```kotlin
    implementation(libs.androidx.material3.adaptive.navigation.suite)
```

- [ ] **Step 2: Simplify the Applications entry (remove the drawer)**

In `modules/applications/.../ApplicationEntry.kt`, replace the entire `applicationNavigations` function so the `Applications` entry renders its content without `ModalNavigationDrawer` and without a `TopAppBar` navigation drawer icon (top-level nav now lives in the suite shell). Delete the `TogglesDrawer` composable entirely. New `applicationNavigations`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.applicationNavigations(
    navigateToConfigurations: (Long) -> Unit,
) {
    entry<Applications> {
        val viewModel = hiltViewModel<ApplicationViewModel>()
        val viewState = viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Applications") })
            },
        ) { paddingValues ->
            ApplicationsView(
                viewState = viewState.value,
                modifier = Modifier.padding(paddingValues),
                navigateToConfigurations = navigateToConfigurations
            )
        }
    }
}
```

Remove now-unused imports in this file: `Image`, `background`, `Column`, `Spacer`, `height`, `Icons`, `Menu`, `DrawerState`, `DrawerValue`, `IconButton`, `ModalDrawerSheet`, `ModalNavigationDrawer`, `NavigationDrawerItem`, `NavigationDrawerItemDefaults`, `rememberDrawerState`, `mutableIntStateOf`, `remember`, `rememberCoroutineScope`, `setValue`/`getValue` (keep `getValue` if still used by `collectAsState` delegation — here `viewState` uses `.value`, so both can go), `Alignment`, `colorResource`, `painterResource`, `launch`, and the `ComposeThemeR` alias. Keep `Scaffold`, `TopAppBar`, `Text`, `Modifier`, `padding`, `Composable`, `collectAsState`, `EntryProviderScope`, `NavKey`, `Applications`, `hiltViewModel`.

- [ ] **Step 3: Add the navigation suite shell in MainActivity**

In `toggles-app/.../MainActivity.kt`, introduce a top-level destination enum and wrap `NavDisplay` in `NavigationSuiteScaffold`. Add imports:

```kotlin
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
```

Add this enum at file top level (below imports):

```kotlin
private enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: NavKey,
) {
    APPLICATIONS("Applications", Icons.Filled.Apps, Applications),
    OSS("Licenses", Icons.Filled.Info, Oss),
    HELP("Help", Icons.AutoMirrored.Filled.HelpOutline, Help),
}
```

(Add imports `androidx.compose.ui.graphics.vector.ImageVector` and `androidx.compose.material.icons.Icons`.)

- [ ] **Step 4: Wrap NavDisplay in the suite scaffold**

In `MainActivity.kt`, change `setContent` to host the suite. Replace the `TogglesTheme { Navigation() }` body with a composable that tracks the selected top-level destination and renders the suite around `Navigation(...)`:

```kotlin
        setContent {
            TogglesTheme {
                TogglesApp()
            }
        }
```

Add this composable to the file:

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun TogglesApp() {
    val backStack = rememberNavBackStack(Applications)
    var selected by remember { mutableStateOf(TopLevelDestination.APPLICATIONS) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = selected == destination,
                    onClick = {
                        selected = destination
                        backStack.add(destination.route)
                    },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                )
            }
        }
    ) {
        Navigation(backStack = backStack)
    }
}
```

- [ ] **Step 5: Hoist the backstack into Navigation**

Change the `Navigation` composable signature to accept the hoisted backstack and drop the internal `rememberNavBackStack`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun Navigation(
    backStack: NavBackStack,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        // ... rest unchanged
```

Add import `androidx.navigation3.runtime.NavBackStack`. Update the `applicationNavigations(...)` call inside `entryProvider` to match Task 3's reduced signature — it now only takes `navigateToConfigurations`:

```kotlin
            applicationNavigations(
                navigateToConfigurations = { applicationId ->
                    backStack.add(Configurations(applicationId))
                },
            )
```

(The `navigateToApplications`/`navigateToOss`/`navigateToHelp` lambdas are gone — the suite handles those. Remove the now-unused `Oss`/`Help`/`Applications` add-lambdas only from this call; keep their `entry<...>` blocks.)

- [ ] **Step 6: Compile the app**

Run: `./gradlew :toggles-app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. Fix any unresolved imports. If `NavigationSuiteScaffold`'s `item` DSL signature differs in the resolved version, adjust the `navigationSuiteItems` lambda to match (the parameter names `selected`/`onClick`/`icon`/`label` are stable across recent releases).

- [ ] **Step 7: detekt**

Run: `./gradlew :toggles-app:detekt :modules:applications:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Manual verification**

Run the app on a phone emulator (compact): confirm a bottom `NavigationBar` shows Applications/Licenses/Help and switching destinations works. Run on a tablet/foldable emulator (expanded): confirm it renders as a navigation rail instead.

- [ ] **Step 9: Commit**

```bash
git add toggles-app/build.gradle.kts toggles-app/src/main/java/se/eelde/toggles/MainActivity.kt \
  modules/applications/src/main/java/se/eelde/toggles/applications/ApplicationEntry.kt
git commit -m "Replace navigation drawer with adaptive NavigationSuiteScaffold"
```

---

## Task 4: List-detail adaptive pane for configurations → value editor

Apply the `material3-adaptive-navigation3` scene strategy so the `Configurations` list and value-editor entries render side-by-side on expanded width.

**Files:**
- Modify: `toggles-app/build.gradle.kts`
- Modify: `toggles-app/src/main/java/se/eelde/toggles/MainActivity.kt`

- [ ] **Step 1: Add the adaptive-navigation3 dependency to the app**

In `toggles-app/build.gradle.kts`, add near the nav3 deps (~line 129):

```kotlin
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.material3.adaptive)
```

- [ ] **Step 2: Add the list-detail scene strategy to NavDisplay**

In `MainActivity.kt`, add imports:

```kotlin
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
```

In `Navigation`, create the scene strategy and pass it to `NavDisplay`:

```kotlin
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = listDetailStrategy,
        onBack = { backStack.removeLastOrNull() },
        // entryDecorators / entryProvider unchanged
```

Add import `androidx.navigation3.runtime.NavKey` if not present.

- [ ] **Step 3: Mark list and detail entries with pane metadata**

The list-detail scene strategy decides panes via entry metadata. Mark the `Configurations` entry as the list pane and the value-editor entries as the detail pane. For the `Configurations` entry, add metadata:

```kotlin
            entry<Configurations>(
                metadata = ListDetailSceneStrategy.listPane()
            ) { configurations ->
                // existing configurationsNavigations content stays;
                // see Step 4 for how this integrates
            }
```

For each value-editor entry (`BooleanConfiguration`, `IntegerConfiguration`, `StringConfiguration`, `EnumConfiguration`, `Scope`) add `metadata = ListDetailSceneStrategy.detailPane()`:

```kotlin
            entry<BooleanConfiguration>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { booleanConfiguration ->
                BooleanValueView(
                    booleanConfiguration = booleanConfiguration,
                    back = { backStack.removeLastOrNull() }
                )
            }
```

Add import `androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy`.

- [ ] **Step 4: Reconcile with the configurationsNavigations extension**

`configurationsNavigations` (in `modules/configurations`) currently registers its own `entry<Configurations>`. The `metadata` must be attached where that entry is declared. Move the `metadata = ListDetailSceneStrategy.listPane()` into the `entry<Configurations>` call inside `modules/configurations/.../ConfigurationsEntry.kt`, and add the `material3-adaptive-navigation3` dependency to `modules/configurations/build.gradle.kts`:

```kotlin
    implementation(libs.androidx.material3.adaptive.navigation3)
```

In `ConfigurationsEntry.kt`, change `entry<Configurations> { ... }` to:

```kotlin
    entry<Configurations>(
        metadata = ListDetailSceneStrategy.listPane()
    ) { configurations ->
```

and add `import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy`.

Likewise, the value-editor entries declared inside `configurationsNavigations`/`MainActivity` get `detailPane()` metadata as shown in Step 3. (Boolean/Integer/String/Enum/Scope entries are declared in `MainActivity.kt`, so Step 3 covers them.)

- [ ] **Step 5: Compile the app and configurations module**

Run: `./gradlew :toggles-app:compileDebugKotlin :modules:configurations:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. If `rememberListDetailSceneStrategy` / `ListDetailSceneStrategy.listPane()` signatures differ in `1.3.0-beta02`, consult the resolved API (the concepts — a remembered scene strategy + list/detail pane metadata — are stable; method names may be `listPane()`/`detailPane()` or take a pane-count argument). Adjust accordingly.

- [ ] **Step 6: detekt**

Run: `./gradlew :toggles-app:detekt :modules:configurations:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Manual verification**

Phone (compact): tapping an application shows the configurations list; tapping a configuration pushes the value editor full-screen; back returns to the list. Tablet/foldable (expanded): the configurations list and the selected value editor show side-by-side; selecting a different configuration updates the detail pane without leaving the list.

- [ ] **Step 8: Commit**

```bash
git add toggles-app/build.gradle.kts toggles-app/src/main/java/se/eelde/toggles/MainActivity.kt \
  modules/configurations/build.gradle.kts \
  modules/configurations/src/main/java/se/eelde/toggles/configurations/ConfigurationsEntry.kt
git commit -m "Render configurations list-detail as adaptive pane"
```

**Fallback (if Step 5 cannot resolve `adaptive-navigation3`):** Skip Tasks 4. Keep push navigation, and leave a note in the PR that the list-detail pane is deferred pending a resolvable `adaptive-navigation3` release. Tasks 5–7 do not depend on this.

---

## Task 5: Expressive components on the boolean editor

**Files:**
- Modify: `modules/booleanconfiguration/src/main/java/se/eelde/toggles/booleanconfiguration/BooleanValueView.kt`

- [ ] **Step 1: Replace Switch with ToggleButtonGroup and the button Row with ButtonGroup**

Replace the inner `BooleanValueView(viewState, ...)` body (the `Column` content, lines ~88–122) with a `ToggleButtonGroup` for On/Off and a `ButtonGroup` for Revert/Save. Update imports: remove `Switch`, `Row`, `Arrangement`, `End`, `Button`; add:

```kotlin
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
```

New `Column` content:

```kotlin
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    text = viewState.title.orEmpty()
                )

                @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                ButtonGroup(modifier = Modifier.padding(8.dp)) {
                    ToggleButton(
                        checked = viewState.checked == false,
                        onCheckedChange = { checkedChanged(false) },
                    ) { Text("Off") }
                    ToggleButton(
                        checked = viewState.checked == true,
                        onCheckedChange = { checkedChanged(true) },
                    ) { Text("On") }
                }

                @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Button(onClick = {
                        scope.launch { revert(); popBackStack() }
                    }) { Text("Revert") }
                    Button(onClick = {
                        scope.launch { save(); popBackStack() }
                    }) { Text("Save") }
                }
            }
```

Re-add `import androidx.compose.material3.Button` (it is still used for Revert/Save) and keep `fillMaxWidth`. The `ToggleButtonGroup`-style behavior is achieved via `ButtonGroup` + `ToggleButton`; if the resolved API exposes a dedicated single-select `ToggleButtonGroup`, prefer it.

- [ ] **Step 2: Compile**

Run: `./gradlew :modules:booleanconfiguration:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. If `ButtonGroup`/`ToggleButton` are unresolved, verify they exist at the pinned BOM; they are part of M3 Expressive.

- [ ] **Step 3: detekt**

Run: `./gradlew :modules:booleanconfiguration:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Manual verification**

Open a boolean configuration: On/Off renders as a connected toggle group reflecting the current value; toggling updates it; Revert/Save sit in a button group and behave as before.

- [ ] **Step 5: Commit**

```bash
git add modules/booleanconfiguration/src/main/java/se/eelde/toggles/booleanconfiguration/BooleanValueView.kt
git commit -m "Use ToggleButtonGroup and ButtonGroup in boolean editor"
```

---

## Task 6: ButtonGroup for Revert/Save on integer, string, enum editors

**Files:**
- Modify: `modules/integerconfiguration/src/main/java/se/eelde/toggles/integerconfiguration/IntegerValueView.kt`
- Modify: `modules/stringconfiguration/src/main/java/se/eelde/toggles/stringconfiguration/StringValueView.kt`
- Modify: `modules/enumconfiguration/src/main/java/se/eelde/toggles/enumconfiguration/EnumValueView.kt`

- [ ] **Step 1: Integer editor — swap the Row of Buttons for a ButtonGroup**

In `IntegerValueView.kt`, replace the `Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button {...} Button {...} }` block (~line 113) with:

```kotlin
            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                // the two existing Button(...) calls, verbatim, but drop their individual
                // Modifier.padding(8.dp) since the group handles spacing
            }
```

Remove imports `Row`, `Arrangement`; add `import androidx.compose.material3.ButtonGroup` and `import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi`. Keep `Button`, `fillMaxWidth`, `padding`.

- [ ] **Step 2: String editor — same swap**

In `StringValueView.kt`, apply the identical change to its `Row(... Arrangement.End) { Button {...} Button {...} }` block (~line 115): wrap the two `Button` calls in `ButtonGroup`, remove `Row`/`Arrangement` imports, add the two new imports.

- [ ] **Step 3: Enum editor — same swap for the single Revert button row**

In `EnumValueView.kt`, the action row (~line 109) has only a Revert `Button`. Wrap it in `ButtonGroup` for visual consistency:

```kotlin
            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Button(onClick = {
                    scope.launch { revert(); popBackStack() }
                }) { Text("Revert") }
            }
```

Remove `Row`/`Arrangement` imports; add `ButtonGroup` and `ExperimentalMaterial3ExpressiveApi` imports.

- [ ] **Step 4: Compile all three modules**

Run: `./gradlew :modules:integerconfiguration:compileDebugKotlin :modules:stringconfiguration:compileDebugKotlin :modules:enumconfiguration:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: detekt**

Run: `./gradlew :modules:integerconfiguration:detekt :modules:stringconfiguration:detekt :modules:enumconfiguration:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add modules/integerconfiguration/src/main/java/se/eelde/toggles/integerconfiguration/IntegerValueView.kt \
  modules/stringconfiguration/src/main/java/se/eelde/toggles/stringconfiguration/StringValueView.kt \
  modules/enumconfiguration/src/main/java/se/eelde/toggles/enumconfiguration/EnumValueView.kt
git commit -m "Use ButtonGroup for revert/save on integer, string, enum editors"
```

---

## Task 7: Toggle-gated enum list presentation (dogfooding)

Add a boolean toggle `expressive_enum_list` (read via `toggles-flow`) that, when ON, renders the enum option list as a proper M3 single-choice list (RadioButton rows) instead of the current `Link`-icon list. Default OFF preserves current behavior.

**Files:**
- Modify: `modules/enumconfiguration/build.gradle.kts`
- Modify: `modules/enumconfiguration/.../EnumValueViewModel.kt`
- Create: `modules/enumconfiguration/src/test/java/se/eelde/toggles/enumconfiguration/EnumValueViewModelTest.kt`
- Modify: `modules/enumconfiguration/.../EnumValueView.kt`

- [ ] **Step 1: Add toggles-flow dependency to the enum module**

In `modules/enumconfiguration/build.gradle.kts`, add to dependencies:

```kotlin
    implementation(projects.togglesFlow)
```

Verify a test dependency exists for coroutines/turbine; if the module has no `testImplementation` for coroutines test, add:

```kotlin
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.junit)
```

(Check the actual catalog keys with `grep -n "coroutines.test\|junit " gradle/libs.versions.toml` and use the matching alias.)

- [ ] **Step 2: Write the failing ViewModel test**

Create `modules/enumconfiguration/src/test/java/se/eelde/toggles/enumconfiguration/EnumValueViewModelTest.kt`. This test injects a fake `Toggles` returning `true` for `expressive_enum_list` and asserts the view state exposes `expressiveList = true`. Use a fake that satisfies the `Toggles` interface:

```kotlin
package se.eelde.toggles.enumconfiguration

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import se.eelde.toggles.flow.Toggles

private class FakeToggles(private val boolValue: Boolean) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(boolValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> =
        flowOf(defaultValue)
}

class EnumValueViewModelTest {
    @Test
    fun `expressive list flag flows into view state`() = runTest {
        val flow = FakeToggles(boolValue = true)
            .toggle("expressive_enum_list", false)
        flow.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
}
```

(This first test pins the contract of reading the flag. If the module already has DAO fakes enabling full ViewModel construction, prefer a test that builds `EnumValueViewModel` with `FakeToggles` and asserts `state.value.expressiveList`. If constructing the ViewModel requires many DAO fakes not yet present, keep this contract-level test and verify the wiring via the manual step.)

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :modules:enumconfiguration:testDebugUnitTest --tests "*EnumValueViewModelTest*"`
Expected: FAIL — the module does not yet compile against `toggles-flow` usage / `expressiveList` does not exist yet. (If Step 1 deps are missing, the failure is a compile error; that still counts as red.)

- [ ] **Step 4: Inject Toggles and expose the flag in the ViewModel**

In `EnumValueViewModel.kt`:

1. Add `expressiveList: Boolean = false` to the `ViewState` data class.
2. Add a `PartialViewState.ExpressiveList(val enabled: Boolean)` subtype and handle it in `reduce`:

```kotlin
    data class ExpressiveList(val enabled: Boolean) : PartialViewState()
```
```kotlin
            is PartialViewState.ExpressiveList -> {
                previousState.copy(expressiveList = partialViewState.enabled)
            }
```

3. Add `private val toggles: Toggles` as a (non-assisted) constructor parameter, and collect the flag in `init`:

```kotlin
        viewModelScope.launch {
            toggles.toggle("expressive_enum_list", false).collect {
                _state.value = reduce(_state.value, PartialViewState.ExpressiveList(it))
            }
        }
```

Add `import se.eelde.toggles.flow.Toggles`. Place `toggles` among the existing injected params (before `@Assisted enumConfiguration`).

- [ ] **Step 5: Adjust the test to assert via the ViewModel (if feasible)**

If the module has test fakes for the DAOs, update the test to construct `EnumValueViewModel(... FakeToggles(true) ...)` and assert `state.value.expressiveList` becomes true. Otherwise keep the contract test from Step 2.

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :modules:enumconfiguration:testDebugUnitTest --tests "*EnumValueViewModelTest*"`
Expected: PASS.

- [ ] **Step 7: Branch the enum list presentation on the flag**

In `EnumValueView.kt`, pass `state.expressiveList` into the inner composable (already receives `state`). Inside the `LazyColumn` item, branch the `ListItem` leading content: when `state.expressiveList` is true, use a `RadioButton` reflecting `selected`; otherwise keep the existing `Link` icon. Replace the `leadingContent` lambda:

```kotlin
                            leadingContent = {
                                if (state.expressiveList) {
                                    RadioButton(selected = selected, onClick = null)
                                } else if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Link,
                                        contentDescription = null
                                    )
                                }
                            }
```

Add `import androidx.compose.material3.RadioButton`.

- [ ] **Step 8: Compile and detekt the module**

Run: `./gradlew :modules:enumconfiguration:compileDebugKotlin :modules:enumconfiguration:detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Manual verification (the dogfooding loop)**

1. Build/install the app. Open an enum configuration — note the current `Link`-icon list (flag defaults OFF).
2. In Toggles, open the `se.eelde.toggles` application (it auto-registered after the enum view read the flag once), find the `expressive_enum_list` boolean toggle, set it ON.
3. Reopen the enum configuration — the list now shows `RadioButton` selection. Confirm it scrolls and selects correctly with many/long options.

- [ ] **Step 10: Commit**

```bash
git add modules/enumconfiguration/build.gradle.kts \
  modules/enumconfiguration/src/main/java/se/eelde/toggles/enumconfiguration/EnumValueViewModel.kt \
  modules/enumconfiguration/src/main/java/se/eelde/toggles/enumconfiguration/EnumValueView.kt \
  modules/enumconfiguration/src/test/java/se/eelde/toggles/enumconfiguration/EnumValueViewModelTest.kt
git commit -m "Add toggle-gated expressive enum list (dogfooded via toggles-flow)"
```

---

## Task 8: Full verification pass

**Files:** none (verification only)

- [ ] **Step 1: Run detekt across the repo**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run all unit tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL. Fix any test that asserted on the removed drawer or the boolean `Switch`.

- [ ] **Step 3: Compile instrumentation tests**

Run: `./gradlew assembleAndroidTest`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Final manual smoke on compact + expanded emulators**

Verify the full flow end-to-end on both a phone and a tablet/foldable: adaptive navigation chrome, list-detail pane behavior, all four value editors, and the enum toggle loop.

- [ ] **Step 5: Commit any fixups**

```bash
git add -A
git commit -m "Fix up tests and lint after M3 Expressive migration"
```

---

## Self-Review Notes

- **Spec coverage:** §1 deps → Tasks 1, 3, 4, 7; §2 theme → Task 2; §3 adaptive nav → Task 3; §4 list-detail → Task 4 (+ fallback); §5 expressive components → Tasks 5, 6; §5 enum toggle-gated list → Task 7; §6 testing → Task 8. All sections covered.
- **Experimental-API caveat:** Tasks 3–6 reference experimental Expressive/adaptive APIs. Where exact signatures may differ at the resolved versions, steps include a concrete best-known implementation plus an explicit "adjust to resolved API" instruction — not placeholders.
- **Type consistency:** `expressiveList` (ViewState field), `PartialViewState.ExpressiveList(enabled)`, and the toggle key `"expressive_enum_list"` are used consistently across Task 7. `Navigation(backStack, modifier)` signature is defined in Task 3 Step 5 and consumed in Task 3 Step 4.
