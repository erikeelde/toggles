# hasOverride Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `hasOverride(key, comparator)` to both `Toggles` and `TogglesPreferences` so consumers can detect when a toggle is being served from a non-default scope.

**Architecture:** A `ScopeComparator` fun interface (defined independently in each library) takes a `ToggleState` and returns `Boolean`. A `DefaultScopeComparator` object implements the active-override check: selected scope ≠ default scope AND a value exists for the selected scope. The noop variants always return `false`. `ToggleState` is already available from `toggles-core`; no changes to `toggles-core` are needed.

**Tech Stack:** Kotlin, Robolectric (unit tests), `toggles-core` types (`ToggleState`, `ToggleScope`, `TogglesConfigurationValue`, `TogglesConfiguration`, `ColumnNames`)

---

## File Map

| Action | Path |
|--------|------|
| Create | `toggles-flow/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt` |
| Create | `toggles-flow/src/test/java/se/eelde/toggles/flow/DefaultScopeComparatorTest.kt` |
| Modify | `toggles-flow/src/main/java/se/eelde/toggles/flow/Toggles.kt` |
| Modify | `toggles-flow/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt` |
| Modify | `toggles-flow/src/test/java/se/eelde/toggles/flow/FlowTest.kt` |
| Create | `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt` |
| Modify | `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/Toggles.kt` |
| Modify | `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt` |
| Create | `toggles-prefs/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt` |
| Create | `toggles-prefs/src/test/java/se/eelde/toggles/prefs/DefaultScopeComparatorTest.kt` |
| Modify | `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt` |
| Modify | `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt` |
| Modify | `toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesReturnsProviderValues.kt` |
| Create | `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt` |
| Modify | `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt` |
| Modify | `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt` |

---

## Task 1: `ScopeComparator` in `toggles-flow`

**Files:**
- Create: `toggles-flow/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt`
- Create: `toggles-flow/src/test/java/se/eelde/toggles/flow/DefaultScopeComparatorTest.kt`

- [ ] **Step 1: Write the failing test**

Create `toggles-flow/src/test/java/se/eelde/toggles/flow/DefaultScopeComparatorTest.kt`:

```kotlin
package se.eelde.toggles.flow

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class DefaultScopeComparatorTest {

    private val defaultScope = ToggleScope {
        id = 1L
        name = ColumnNames.ToggleScope.DEFAULT_SCOPE
        timeStamp = Date(1000)
    }

    private val nonDefaultScope = ToggleScope {
        id = 2L
        name = "development"
        timeStamp = Date(2000)
    }

    private val configuration = TogglesConfiguration {
        id = 1L
        type = "BOOLEAN"
        key = "feature_x"
    }

    @Test
    fun `returns false when scopes list is empty`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = emptyList(),
            scopes = emptyList()
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when only default scope exists`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when non-default scope selected but has no value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns true when non-default scope is selected and has a value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                },
                TogglesConfigurationValue {
                    id = 2L
                    configurationId = 1L
                    value = "false"
                    scope = nonDefaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertTrue(DefaultScopeComparator.hasOverride(state))
    }
}
```

- [ ] **Step 2: Run test — expect compile failure (DefaultScopeComparator not defined)**

```
./gradlew :toggles-flow:test --tests "se.eelde.toggles.flow.DefaultScopeComparatorTest" --no-daemon
```

Expected: build failure — `Unresolved reference: DefaultScopeComparator`

- [ ] **Step 3: Create `ScopeComparator.kt`**

Create `toggles-flow/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt`:

```kotlin
package se.eelde.toggles.flow

import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean {
        val defaultScope = state.scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }
        val selectedScope = state.scopes.maxByOrNull { it.timeStamp }
        if (defaultScope == null || selectedScope == null) return false
        if (defaultScope.id == selectedScope.id) return false
        return state.configurationValues.any { it.scope == selectedScope.id }
    }
}
```

- [ ] **Step 4: Run tests — expect all 4 pass**

```
./gradlew :toggles-flow:test --tests "se.eelde.toggles.flow.DefaultScopeComparatorTest" --no-daemon
```

Expected output includes: `BUILD SUCCESSFUL` and 4 tests passed.

- [ ] **Step 5: Commit**

```bash
git add toggles-flow/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt \
        toggles-flow/src/test/java/se/eelde/toggles/flow/DefaultScopeComparatorTest.kt
git commit -m "Add ScopeComparator and DefaultScopeComparator to toggles-flow"
```

---

## Task 2: `hasOverride` in `toggles-flow` interface, implementation, and integration test

**Files:**
- Modify: `toggles-flow/src/main/java/se/eelde/toggles/flow/Toggles.kt`
- Modify: `toggles-flow/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt`
- Modify: `toggles-flow/src/test/java/se/eelde/toggles/flow/FlowTest.kt`

- [ ] **Step 1: Write the failing integration test**

Add these two test methods to the existing `FlowTest` class in `toggles-flow/src/test/java/se/eelde/toggles/flow/FlowTest.kt`. Add the needed imports too: `import kotlinx.coroutines.flow.first`, `import org.junit.Assert.assertFalse`, `import org.junit.Assert.assertNotNull`, `import se.eelde.toggles.core.ToggleState`.

```kotlin
@Test
fun `hasOverride returns false for key with no overriding scope value`() = runTest {
    val toggles = TogglesImpl(context)
    val result = toggles.hasOverride("no-override-key").first()
    @OptIn(ExperimentalCoroutinesApi::class)
    advanceUntilIdle()
    assertFalse(result)
}

@Test
fun `hasOverride passes ToggleState to custom comparator`() = runTest {
    var capturedState: ToggleState? = null
    val capturingComparator = ScopeComparator { state ->
        capturedState = state
        false
    }
    val toggles = TogglesImpl(context)
    toggles.hasOverride("some-key", capturingComparator).first()
    @OptIn(ExperimentalCoroutinesApi::class)
    advanceUntilIdle()
    assertNotNull(capturedState)
}
```

- [ ] **Step 2: Run test — expect compile failure**

```
./gradlew :toggles-flow:test --tests "se.eelde.toggles.flow.FlowTest" --no-daemon
```

Expected: build failure — `Unresolved reference: hasOverride`

- [ ] **Step 3: Add `hasOverride` to the `Toggles` interface**

In `toggles-flow/src/main/java/se/eelde/toggles/flow/Toggles.kt`, add the import and new method. The full file after changes:

```kotlin
package se.eelde.toggles.flow

import kotlinx.coroutines.flow.Flow

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface Toggles {
    public fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>
    public fun toggle(key: String, defaultValue: String): Flow<String>
    public fun toggle(key: String, defaultValue: Int): Flow<Int>
    public fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>

    public fun hasOverride(
        key: String,
        comparator: ScopeComparator = DefaultScopeComparator
    ): Flow<Boolean>
}
```

- [ ] **Step 4: Implement `hasOverride` in `TogglesImpl`**

In `toggles-flow/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt`, add the override after the last existing `toggle` override:

```kotlin
override fun hasOverride(key: String, comparator: ScopeComparator): Flow<Boolean> =
    provider.observeToggleState(key).map { comparator.hasOverride(it) }
```

The import `import kotlinx.coroutines.flow.map` should already be present. If not, add it.

- [ ] **Step 5: Run tests — expect all pass**

```
./gradlew :toggles-flow:test --tests "se.eelde.toggles.flow.FlowTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`, all tests pass including the two new ones.

- [ ] **Step 6: Commit**

```bash
git add toggles-flow/src/main/java/se/eelde/toggles/flow/Toggles.kt \
        toggles-flow/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt \
        toggles-flow/src/test/java/se/eelde/toggles/flow/FlowTest.kt
git commit -m "Add hasOverride to Toggles interface and TogglesImpl"
```

---

## Task 3: `hasOverride` in `toggles-flow-noop`

**Files:**
- Create: `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt`
- Modify: `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/Toggles.kt`
- Modify: `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt`

The noop module has its own copy of the `Toggles` interface and `ScopeComparator` (same package, separate module, no dependency on the main `toggles-flow` module).

- [ ] **Step 1: Create `ScopeComparator.kt` in the noop module**

Create `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt`:

```kotlin
package se.eelde.toggles.flow

import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean = false
}
```

Note: the noop `DefaultScopeComparator` always returns `false` — it exists only to satisfy the interface default parameter signature.

- [ ] **Step 2: Update the noop `Toggles` interface**

Replace the contents of `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/Toggles.kt`:

```kotlin
package se.eelde.toggles.flow

import kotlinx.coroutines.flow.Flow

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface Toggles {
    public fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>
    public fun toggle(key: String, defaultValue: String): Flow<String>
    public fun toggle(key: String, defaultValue: Int): Flow<Int>
    public fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>

    public fun hasOverride(
        key: String,
        comparator: ScopeComparator = DefaultScopeComparator
    ): Flow<Boolean>
}
```

- [ ] **Step 3: Implement `hasOverride` in the noop `TogglesImpl`**

In `toggles-flow-noop/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt`, add the import and override. The full file after changes:

```kotlin
package se.eelde.toggles.flow

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesImpl(@Suppress("UNUSED_PARAMETER") context: Context) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> = flowOf(defaultValue)
    override fun hasOverride(key: String, comparator: ScopeComparator): Flow<Boolean> = flowOf(false)
}
```

- [ ] **Step 4: Verify compilation**

```
./gradlew :toggles-flow-noop:assembleDebug --no-daemon
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add toggles-flow-noop/src/main/java/se/eelde/toggles/flow/ScopeComparator.kt \
        toggles-flow-noop/src/main/java/se/eelde/toggles/flow/Toggles.kt \
        toggles-flow-noop/src/main/java/se/eelde/toggles/flow/TogglesImpl.kt
git commit -m "Add hasOverride to toggles-flow-noop"
```

---

## Task 4: `ScopeComparator` in `toggles-prefs`

**Files:**
- Create: `toggles-prefs/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt`
- Create: `toggles-prefs/src/test/java/se/eelde/toggles/prefs/DefaultScopeComparatorTest.kt`

Same logic as Task 1, different package.

- [ ] **Step 1: Write the failing test**

Create `toggles-prefs/src/test/java/se/eelde/toggles/prefs/DefaultScopeComparatorTest.kt`:

```kotlin
package se.eelde.toggles.prefs

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
internal class DefaultScopeComparatorTest {

    private val defaultScope = ToggleScope {
        id = 1L
        name = ColumnNames.ToggleScope.DEFAULT_SCOPE
        timeStamp = Date(1000)
    }

    private val nonDefaultScope = ToggleScope {
        id = 2L
        name = "development"
        timeStamp = Date(2000)
    }

    private val configuration = TogglesConfiguration {
        id = 1L
        type = "BOOLEAN"
        key = "feature_x"
    }

    @Test
    fun `returns false when scopes list is empty`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = emptyList(),
            scopes = emptyList()
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when only default scope exists`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when non-default scope selected but has no value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns true when non-default scope is selected and has a value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                },
                TogglesConfigurationValue {
                    id = 2L
                    configurationId = 1L
                    value = "false"
                    scope = nonDefaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertTrue(DefaultScopeComparator.hasOverride(state))
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```
./gradlew :toggles-prefs:test --tests "se.eelde.toggles.prefs.DefaultScopeComparatorTest" --no-daemon
```

Expected: build failure — `Unresolved reference: DefaultScopeComparator`

- [ ] **Step 3: Create `ScopeComparator.kt`**

Create `toggles-prefs/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt`:

```kotlin
package se.eelde.toggles.prefs

import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean {
        val defaultScope = state.scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }
        val selectedScope = state.scopes.maxByOrNull { it.timeStamp }
        if (defaultScope == null || selectedScope == null) return false
        if (defaultScope.id == selectedScope.id) return false
        return state.configurationValues.any { it.scope == selectedScope.id }
    }
}
```

- [ ] **Step 4: Run tests — expect all 4 pass**

```
./gradlew :toggles-prefs:test --tests "se.eelde.toggles.prefs.DefaultScopeComparatorTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 5: Commit**

```bash
git add toggles-prefs/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt \
        toggles-prefs/src/test/java/se/eelde/toggles/prefs/DefaultScopeComparatorTest.kt
git commit -m "Add ScopeComparator and DefaultScopeComparator to toggles-prefs"
```

---

## Task 5: `hasOverride` in `toggles-prefs` interface, implementation, and integration test

**Files:**
- Modify: `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt`
- Modify: `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt`
- Modify: `toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesReturnsProviderValues.kt`

- [ ] **Step 1: Write the failing integration test**

In `toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesReturnsProviderValues.kt`, add these imports at the top of the file:

```kotlin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.prefs.ScopeComparator
```

Then add these test methods to the `TogglesPreferencesReturnsProviderValues` class:

```kotlin
@Test
fun `hasOverride returns false when no value stored for non-default scope`() {
    // Mock returns 2 scopes: default (id=1) and "user" (id=2, higher timestamp).
    // No value is stored for scope 2, so hasOverride must return false.
    assertFalse(togglesPreferences.hasOverride("unknown-key"))
}

@Test
fun `hasOverride passes ToggleState to custom comparator`() {
    var capturedState: ToggleState? = null
    val capturingComparator = ScopeComparator { state ->
        capturedState = state
        false
    }
    togglesPreferences.hasOverride("myKey", capturingComparator)
    assertNotNull(capturedState)
}
```

- [ ] **Step 2: Run test — expect compile failure**

```
./gradlew :toggles-prefs:test --tests "se.eelde.toggles.TogglesPreferencesReturnsProviderValues" --no-daemon
```

Expected: build failure — `Unresolved reference: hasOverride`

- [ ] **Step 3: Add `hasOverride` to the `TogglesPreferences` interface**

Replace the contents of `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt`:

```kotlin
package se.eelde.toggles.prefs

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface TogglesPreferences {
    public fun getBoolean(key: String, defaultValue: Boolean): Boolean
    public fun getInt(key: String, defaultValue: Int): Int
    public fun getString(key: String, defaultValue: String): String
    public fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defaultValue: T): T

    public fun hasOverride(
        key: String,
        comparator: ScopeComparator = DefaultScopeComparator
    ): Boolean
}
```

- [ ] **Step 4: Implement `hasOverride` in `TogglesPreferencesImpl`**

In `toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt`, add the override after the last existing `get*` override:

```kotlin
override fun hasOverride(key: String, comparator: ScopeComparator): Boolean =
    comparator.hasOverride(provider.getToggleState(key))
```

- [ ] **Step 5: Run tests — expect all pass**

```
./gradlew :toggles-prefs:test --tests "se.eelde.toggles.TogglesPreferencesReturnsProviderValues" --no-daemon
```

Expected: `BUILD SUCCESSFUL`, all tests pass including the two new ones.

- [ ] **Step 6: Commit**

```bash
git add toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt \
        toggles-prefs/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt \
        toggles-prefs/src/test/java/se/eelde/toggles/TogglesPreferencesReturnsProviderValues.kt
git commit -m "Add hasOverride to TogglesPreferences interface and TogglesPreferencesImpl"
```

---

## Task 6: `hasOverride` in `toggles-prefs-noop`

**Files:**
- Create: `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt`
- Modify: `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt`
- Modify: `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt`

- [ ] **Step 1: Create `ScopeComparator.kt` in the noop module**

Create `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt`:

```kotlin
package se.eelde.toggles.prefs

import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean = false
}
```

- [ ] **Step 2: Update the noop `TogglesPreferences` interface**

Replace the contents of `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt`:

```kotlin
package se.eelde.toggles.prefs

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface TogglesPreferences {
    public fun getBoolean(key: String, defValue: Boolean): Boolean
    public fun getInt(key: String, defValue: Int): Int
    public fun getString(key: String, defValue: String): String
    public fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T

    public fun hasOverride(
        key: String,
        comparator: ScopeComparator = DefaultScopeComparator
    ): Boolean
}
```

Note: the noop interface uses `defValue` (not `defaultValue`) for the existing methods — preserve this spelling for consistency.

- [ ] **Step 3: Implement `hasOverride` in the noop `TogglesPreferencesImpl`**

In `toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt`, add the override. The full file after changes (read the current file first to confirm existing content):

```kotlin
package se.eelde.toggles.prefs

import android.content.Context

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesPreferencesImpl(@Suppress("UNUSED_PARAMETER") context: Context) : TogglesPreferences {
    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
    override fun getInt(key: String, defValue: Int): Int = defValue
    override fun getString(key: String, defValue: String): String = defValue
    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T = defValue
    override fun hasOverride(key: String, comparator: ScopeComparator): Boolean = false
}
```

- [ ] **Step 4: Verify compilation**

```
./gradlew :toggles-prefs-noop:assembleDebug --no-daemon
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/ScopeComparator.kt \
        toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferences.kt \
        toggles-prefs-noop/src/main/java/se/eelde/toggles/prefs/TogglesPreferencesImpl.kt
git commit -m "Add hasOverride to toggles-prefs-noop"
```

---

## Task 7: Full check

- [ ] **Step 1: Run all unit tests**

```
./gradlew :toggles-flow:test :toggles-prefs:test --no-daemon
```

Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 2: Run detekt**

```
./gradlew :toggles-flow:detekt :toggles-prefs:detekt :toggles-flow-noop:detekt :toggles-prefs-noop:detekt --no-daemon
```

Expected: `BUILD SUCCESSFUL` with no detekt violations.

- [ ] **Step 3: Run full check on affected modules**

```
./gradlew :toggles-flow:check :toggles-prefs:check --no-daemon
```

Expected: `BUILD SUCCESSFUL`
