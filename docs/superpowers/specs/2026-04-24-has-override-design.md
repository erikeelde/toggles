# hasOverride API Design

**Date:** 2026-04-24

## Goal

Allow consumers of `toggles-flow` and `toggles-prefs` to check whether a toggle configuration is currently being overridden from its default scope value. The primary use cases are debug/QA UI indicators, runtime logging, and guard logic.

## Architectural constraint

`toggles-core` is the ContentProvider contract module only. `ScopeComparator` and `DefaultScopeComparator` are not placed there. They live in `toggles-flow` and `toggles-prefs` respectively, with acceptable duplication between the two.

## Public API

### `ScopeComparator` (defined separately in each library)

```kotlin
fun interface ScopeComparator {
    fun hasOverride(state: ToggleState): Boolean
}
```

A `fun interface` so callers can pass a lambda for custom behavior. Takes a `ToggleState` (from `toggles-core`) which contains the full configuration, all scope values, and the scope list — everything needed to make an override determination.

### `DefaultScopeComparator`

```kotlin
object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean {
        val defaultScope = state.scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }
        val selectedScope = state.scopes.maxByOrNull { it.timeStamp }
        if (defaultScope == null || selectedScope == null) return false
        if (defaultScope.id == selectedScope.id) return false
        return state.configurationValues.any { it.scope == selectedScope.id }
    }
}
```

Returns `true` when: the selected scope (highest timestamp) differs from the default scope AND there is a stored value for the selected scope for this toggle.

### New interface methods

**`toggles-flow` — `Toggles` interface:**
```kotlin
fun hasOverride(key: String, comparator: ScopeComparator = DefaultScopeComparator): Flow<Boolean>
```

**`toggles-prefs` — `TogglesPreferences` interface:**
```kotlin
fun hasOverride(key: String, comparator: ScopeComparator = DefaultScopeComparator): Boolean
```

## Implementation

### `TogglesImpl` (`toggles-flow`)

```kotlin
override fun hasOverride(key: String, comparator: ScopeComparator): Flow<Boolean> =
    provider.observeToggleState(key).map { comparator.hasOverride(it) }
```

Reactive — emits whenever the underlying `ToggleState` changes (scope switches, value edits).

### `TogglesPreferencesImpl` (`toggles-prefs`)

```kotlin
override fun hasOverride(key: String, comparator: ScopeComparator): Boolean =
    comparator.hasOverride(provider.getToggleState(key))
```

Synchronous point-in-time read.

### Noop variants

Both `toggles-flow-noop` and `toggles-prefs-noop` return `false` unconditionally — no Toggles app connection means no overrides.

### `TogglesProvider` prerequisite

`ToggleState` is currently used internally. Verify during implementation whether `provider.getToggleState(key)` is already exposed or needs to be added to the internal `TogglesProvider` interface.

## Testing

### `DefaultScopeComparator` unit tests (one class per library)

- Default scope is selected → `false`
- Non-default scope selected, value exists for that scope → `true`
- Non-default scope selected, no value for that scope → `false`
- No scopes in state → `false`

### `TogglesImpl` / `TogglesPreferencesImpl` integration tests

- Flow emits updated value when scope changes
- Custom `ScopeComparator` lambda is called with the correct `ToggleState`

### Noop variant tests

- `hasOverride()` returns `false` for any key
