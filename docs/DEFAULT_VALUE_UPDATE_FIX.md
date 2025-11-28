# Default Value Update Fix

## Problem Statement

The original issue described a scenario where it was difficult to differentiate between:
1. **Old default values** - values set when a toggle was first accessed with an initial default
2. **Updated default values** - new defaults provided in subsequent code with updated feature flags  
3. **User-set values** - values explicitly changed by developers using the Toggles UI

### Concrete Scenario

```kotlin
// Initial release - feature not ready
val featureEnabled = toggles.getBoolean("feature-x-rollout", defaultValue = false)
// Stores: false in default scope

// After feature rollout - code updated with new default
val featureEnabled = toggles.getBoolean("feature-x-rollout", defaultValue = true)
// Problem: Still returns false (old default), ignoring the new default value
```

## Root Cause Analysis

The problem had two root causes:

### 1. Database Schema Issue

The `configurationValue` table had a unique constraint on `(configurationId, value, scope)` instead of `(configurationId, scope)`. This meant:

- Multiple rows could exist for the same configuration in the same scope with different values
- There was no enforcement that each configuration has exactly ONE value per scope
- Queries would return an arbitrary row when multiple values existed

### 2. Provider Implementation Issue

In `TogglesProvider.insert()`, when attempting to insert a new default value:

```kotlin
try {
    configurationValueDao.insertSync(togglesConfigurationValue)
} catch (e: SQLiteConstraintException) {
    // Silently ignored - value was never updated!
}
```

The constraint violation was caught and silently ignored, so existing default values were never updated when new defaults were provided.

## Solution

### Part 1: Database Schema Fix

**Changed the unique constraint** from `(configurationId, value, scope)` to `(configurationId, scope)`:

```kotlin
@Entity(
    tableName = ConfigurationValueTable.TABLE_NAME,
    indices = [
        Index(
            value = arrayOf(
                ConfigurationValueTable.COL_CONFIG_ID,
                ConfigurationValueTable.COL_SCOPE  // Removed COL_VALUE
            ),
            unique = true
        )
    ],
    // ...
)
```

This ensures each configuration has exactly ONE value per scope, which is the correct semantics.

### Part 2: Database Migration

Created `MIGRATION_7_8` to safely migrate existing data:

```kotlin
val MIGRATION_7_8: Migration = object : Migration(databaseVersion7, databaseVersion8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new table with updated constraint
        // 2. Migrate data, keeping only most recent value for each (configId, scope)
        // 3. Drop old table and rename new table
    }
}
```

The migration handles edge cases where multiple values existed for the same config+scope by keeping only the most recent one.

### Part 3: Provider Update Logic

Updated `TogglesProvider.insert()` to UPDATE existing default values:

```kotlin
try {
    togglesConfigurationValue.id =
        configurationValueDao.insertSync(togglesConfigurationValue)
} catch (e: SQLiteConstraintException) {
    // A value already exists for this configuration in the default scope.
    // Update it with the new default value. This allows default values to be
    // updated when the code changes, while preserving user-set values in other scopes.
    configurationValueDao.updateConfigurationValueSync(
        togglesConfiguration.id,
        defaultScope.id,
        toggle.value!!
    )
}
```

## How Scopes Separate Defaults from User Values

The existing scope mechanism already provides the infrastructure to differentiate:

- **Default Scope** (`toggles_default`): Stores default values that auto-update
- **User Scope** (`Development scope`): Stores user-modified values that are preserved

When querying a toggle:
1. First checks the user's selected scope (typically `Development scope`)
2. If not found, falls back to the default scope
3. User values always take precedence over defaults

## Behavior After Fix

### Scenario 1: Default Value Updates

```kotlin
// Initial query
toggles.getBoolean("feature-x", defaultValue = false)
// → Stores false in default scope, returns false

// Updated code with new default
toggles.getBoolean("feature-x", defaultValue = true)
// → Updates default scope to true, returns true ✅
```

### Scenario 2: User Values Preserved

```kotlin
// Initial query
toggles.getBoolean("feature-x", defaultValue = false)
// → Stores false in default scope

// User explicitly sets value to true via Toggles UI
// → Stores true in Development scope

// Code updated with new default
toggles.getBoolean("feature-x", defaultValue = false)
// → Updates default scope to false
// → Returns true (from Development scope) ✅
// User's choice is preserved!
```

### Scenario 3: User Resets to Default

```kotlin
// User has custom value in Development scope
// User deletes the custom value via Toggles UI
// → Removes value from Development scope

// Next query
toggles.getBoolean("feature-x", defaultValue = true)
// → Returns true (from default scope) ✅
// Falls back to updated default
```

## Testing

### Unit Tests

Updated tests in `TogglesPreferencesReturnsProviderValues.kt` to verify:
- First query with default stores and returns the default
- Second query with different default updates and returns the new default

### Integration Tests  

Added `TogglesPreferencesDefaultValueUpdateTest.kt` to test the complete scenario from the issue.

### Migration Tests

Added `test7to8()` in `MigrationTests.kt` to verify:
- Migration successfully changes the unique constraint
- Duplicate values are handled correctly
- Data integrity is maintained

## Impact Assessment

### Breaking Changes

**None.** This is a bug fix that makes the system behave as users would naturally expect.

### Backward Compatibility

- Existing apps will seamlessly migrate to v8 on next launch
- Default values will begin updating properly
- User-set values in non-default scopes remain untouched
- No changes required in client code

### Performance

- Minimal impact: One additional UPDATE query when defaults change
- Only affects the default scope (not user-modified values)
- UPDATE is a lightweight operation

## Future Enhancements

Potential improvements to consider:

1. **Last Modified Timestamp**: Add a column to track when values were last modified
2. **Modified By**: Track whether a value was set by system (default) or user
3. **Change History**: Log changes to configuration values for debugging
4. **Sync Mechanism**: For multi-device scenarios, sync user values across devices

## References

- Original Issue: "Difficult to differentiate between old default, updated default and user set value"
- Database Migration: `Migrations.kt` - `MIGRATION_7_8`
- Schema Update: `TogglesConfigurationValue.kt`
- Provider Fix: `TogglesProvider.kt` - `insert()` method
