# Fix: Default Value Update Handling

## Summary

This PR fixes the issue where toggle default values could not be updated when code was changed with new feature flag values. The system now properly distinguishes between default values (which should update) and user-set values (which should be preserved).

## Problem

When requesting a toggle for the first time, it would store itself with a default value. Any subsequent request would return the same stored value, even if a new default was passed. This made it impossible to update default values during feature rollouts without manually resetting each toggle.

### Example Scenario

```kotlin
// Step 1: Feature not ready - use default false
val enabled = toggles.getBoolean("feature-rollout", defaultValue = false)
// Stored: false, Returns: false ✓

// Step 2: Feature ready - update code with default true
val enabled = toggles.getBoolean("feature-rollout", defaultValue = true)  
// Before fix: Stored: false, Returns: false ✗
// After fix: Stored: true, Returns: true ✓
```

## Solution

### Database Schema

Changed the unique constraint on `configurationValue` table from:
- **Before**: `(configurationId, value, scope)` - allowed multiple values per config/scope
- **After**: `(configurationId, scope)` - enforces exactly one value per config/scope

### Provider Logic

Updated `TogglesProvider.insert()` to handle constraint violations properly:
- **Before**: Silently ignored insert failures, never updating existing defaults
- **After**: Catches constraint violation and UPDATE existing default values

### Migration

Added `MIGRATION_7_8` to safely migrate existing databases:
- Recreates `configurationValue` table with new constraint
- Preserves data integrity by keeping most recent value for duplicates
- Fully automatic on app upgrade

## How It Works

The system uses **scopes** to differentiate between defaults and user choices:

1. **Default Scope** (`toggles_default`): 
   - Stores default values provided in code
   - **Auto-updates** when code provides new defaults
   - Used as fallback when user hasn't made a choice

2. **User Scopes** (e.g., `Development scope`):
   - Stores values explicitly set via Toggles UI
   - **Never auto-updates** - preserves user's choices
   - Takes precedence over default scope

### Query Priority

```
User's selected scope → Default scope → Code default parameter
```

## Testing

### Updated Tests

- `TogglesPreferencesReturnsProviderValues.kt`: Verifies default updates work
- `TogglesPreferencesDefaultValueUpdateTest.kt`: Tests the complete scenario from the issue
- `MigrationTests.kt`: Validates database migration correctness

### Test Coverage

✅ Default values update when code changes  
✅ User-set values are preserved  
✅ Migration handles duplicate values correctly  
✅ Fallback to default scope works properly  
✅ User can reset to updated defaults by deleting their custom value

## Breaking Changes

**None.** This is a bug fix that makes the system behave intuitively.

## Migration Path

Completely automatic - no action required by users:
1. App upgrades to version with this fix
2. Database migrates from v7 to v8 on first launch
3. Default values begin updating properly
4. Existing user-set values remain untouched

## Impact

- **Developers**: Can now update default values by changing code
- **Users**: Existing custom values preserved, can reset to new defaults anytime
- **Performance**: Minimal - one additional UPDATE when defaults change

## Files Changed

### Core Changes
- `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesConfigurationValue.kt` - Schema update
- `modules/database/implementation/src/main/java/se/eelde/toggles/database/migrations/Migrations.kt` - Migration
- `modules/database/implementation/src/main/java/se/eelde/toggles/database/TogglesDatabase.kt` - Version bump
- `modules/database/wiring/src/main/kotlin/se/eelde/toggles/database/DatabaseModule.kt` - Register migration
- `modules/provider/implementation/src/main/java/se/eelde/toggles/provider/TogglesProvider.kt` - Update logic

### Tests
- `modules/database/implementation/src/test/java/se/eelde/toggles/database/MigrationTests.kt` - Migration test
- `modules/database/implementation/src/test/java/se/eelde/toggles/database/DatabaseHelper.kt` - Test helpers
- `toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesReturnsProviderValues.kt` - Updated expectations
- `toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesDefaultValueUpdateTest.kt` - New test

### Documentation
- `docs/DEFAULT_VALUE_UPDATE_FIX.md` - Comprehensive technical documentation

## Related Issue

Fixes: "Difficult to differentiate between old default, updated default and user set value"

The agent hint suggested this might already be fixed - the infrastructure (scopes) was indeed present, but the implementation needed updates to properly utilize it for default value updates.
