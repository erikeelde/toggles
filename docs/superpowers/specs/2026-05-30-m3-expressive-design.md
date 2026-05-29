# Material 3 Expressive Adoption — Design

Date: 2026-05-30
Status: Approved (pending spec review)

## Goal

Adopt Material 3 Expressive in the Toggles management app: an adaptive navigation
pattern that responds to screen size, an adaptive list-detail layout for the
configurations → value-editor flow, and the new Expressive components and motion
scheme throughout.

## Scope

In scope: `toggles-app` and the UI feature modules under `modules/` (`compose-theme`,
`applications`, `configurations`, `booleanconfiguration`, `integerconfiguration`,
`stringconfiguration`, `enumconfiguration`, and the scope dialog).

Out of scope: the published libraries `toggles-core`, `toggles-flow`, `toggles-prefs`
and their `-noop` variants — they have no UI and are untouched.

## Chosen architecture

`NavigationSuiteScaffold` for top-level navigation + the `material3-adaptive-navigation3`
scene strategy for the list-detail flow, with the existing Navigation 3 (`NavDisplay`)
backstack remaining the single source of truth. This is the least-custom, Google-blessed
adaptive path (per Google I/O 2025 guidance) and reuses dependencies already pinned in the
version catalog.

Rejected alternatives:
- Pure Nav3 custom scene strategies — more custom code, reimplements what
  `NavigationSuiteScaffold` provides for free.
- Manual `WindowSizeClass` branching — most boilerplate, brittle on foldables / partial
  resize. Retained only as a fallback for the list-detail pane if the adaptive-navigation3
  dependency cannot be resolved.

## Work breakdown

### 1. Dependencies & versioning

Current state (user already bumped): Compose BOM `2026.05.01`, `material3AdaptiveNav3`
`1.3.0-beta02`, nav3 core `1.2.0-alpha03`.

- Add `androidx.compose.material3.adaptive:adaptive-navigation-suite` (for
  `NavigationSuiteScaffold`) to the version catalog.
- Add `androidx.compose.material3.adaptive:adaptive` (window-size / adaptive-info APIs) to
  the version catalog.
- Confirm `adaptive-navigation3` `1.3.0-beta02` resolves from a release repo (no longer
  snapshot-only); if confirmed, remove the snapshot-only comment in `libs.versions.toml`.
- Verify the pinned Compose BOM exposes the stable/experimental M3 Expressive APIs
  (`MaterialExpressiveTheme`, `MotionScheme`, `ButtonGroup`, `ToggleButtonGroup`). The
  Expressive and adaptive APIs are `@Experimental*`; add `@OptIn` at call sites, consistent
  with the existing `@OptIn(ExperimentalMaterial3Api::class)` usage in the codebase.

### 2. Theme foundation (`modules/compose-theme`)

- In `TogglesTheme` (`Theme.kt`), replace `MaterialTheme(...)` with
  `MaterialExpressiveTheme(...)`, passing the existing `colorScheme` and `typography` and
  adding `motionScheme = MotionScheme.expressive()`.
- Leave the dark/light/dynamic/android color-scheme selection logic unchanged — Expressive
  is additive.

### 3. Adaptive navigation shell (`toggles-app/MainActivity.kt`, `modules/applications`)

- Remove `ModalNavigationDrawer` and the `TogglesDrawer` composable from
  `ApplicationEntry.kt`.
- Wrap `NavDisplay` in a top-level `NavigationSuiteScaffold` with three destinations:
  Applications, OSS, Help. It auto-renders a bottom `NavigationBar` on compact width and a
  `NavigationRail` / `WideNavigationRail` on medium/expanded width.
- Selecting a destination drives the Nav3 backstack. The existing `navigateToOss` /
  `navigateToHelp` / `navigateToApplications` lambdas are rewired from drawer items to the
  suite's navigation-item `onClick` callbacks.

### 4. List-detail pane (configurations → value editor)

- Apply the `material3-adaptive-navigation3` scene strategy to `NavDisplay` so the
  `Configurations` entry (list) and the value-editor entries (`BooleanConfiguration`,
  `IntegerConfiguration`, `StringConfiguration`, `EnumConfiguration`, `Scope`) render as
  `ListDetailPaneScaffold` panes side-by-side on expanded width, and as push navigation on
  compact width.
- Back behavior: on dual-pane, back clears the detail pane; on single-pane, back pops the
  backstack. The scene strategy handles this distinction.

### 5. Expressive component swaps

- `BooleanValueView`: replace the `Switch` + Revert/Save `Row` with a `ToggleButtonGroup`
  for the on/off choice and a `ButtonGroup` for the Revert/Save actions.
- Integer / String / Enum value editors: wrap their Revert/Save actions in `ButtonGroup`
  for consistency. Use `ToggleButtonGroup` for the Enum option list only if the option
  count is small; otherwise leave the existing presentation (YAGNI).
- Leave `SearchBar`, `TopAppBar`, and `DropdownMenu` as-is unless the pane refactor forces
  changes.

### 6. Testing & verification

- Existing unit and instrumentation tests in the affected feature modules must still pass.
  Update any test asserting on the drawer or the boolean `Switch`.
- Manual verification on a compact (phone) and an expanded (tablet / unfolded foldable)
  emulator: confirm the navigation pattern switches correctly and the list-detail pane
  renders side-by-side on expanded and as push navigation on compact.
- Run `./gradlew detekt` and the module `check` tasks; CI uses `warningsAsErrors=true`.

## Risks

- **Dependency availability** (primary): if `adaptive-navigation3` `1.3.0-beta02` cannot be
  resolved from a release repo, section 4 is blocked. Fallback: manual `WindowSizeClass`
  branching for the list-detail pane only, keeping sections 2, 3, and 5 intact.
- **Experimental API churn**: Expressive and adaptive APIs are experimental and may shift
  signatures between Compose releases. Mitigated by pinning via the BOM.