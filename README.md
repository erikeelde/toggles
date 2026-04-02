# Toggles - Feature Switching for Android Development

Store feature toggles and app settings in an external application via ContentProvider, persisting them across app reinstalls and clean data operations.

## Quick Start with Toggles-Flow (Recommended)

The **toggles-flow** library provides a reactive approach to feature toggles using Kotlin Flow.

### Installation

Add the toggles-flow library to your project:

```gradle
implementation("se.eelde.toggles:toggles-flow:0.1.2")
```

### Basic Usage

```kotlin
import se.eelde.toggles.flow.Toggles

class MyActivity : AppCompatActivity() {
    private val toggles = Toggles(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Boolean toggle with default value
        lifecycleScope.launch {
            toggles.toggle("enable_new_feature", false).collect { isEnabled ->
                if (isEnabled) {
                    showNewFeature()
                } else {
                    showOldFeature()
                }
            }
        }
        
        // String configuration
        lifecycleScope.launch {
            toggles.toggle("api_endpoint", "https://api.example.com").collect { endpoint ->
                configureApiClient(endpoint)
            }
        }
        
        // Integer configuration
        lifecycleScope.launch {
            toggles.toggle("max_retry_count", 3).collect { retryCount ->
                setMaxRetries(retryCount)
            }
        }
    }
}
```

## Toggles App

Install the companion app to manage your feature toggles:

[Google Play Store](https://play.google.com/store/apps/details?id=se.eelde.toggles)

## Advanced Examples

### Working with Enums
[![Flow](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow)

```kotlin
enum class LogLevel { DEBUG, INFO, WARN, ERROR }

lifecycleScope.launch {
    toggles.toggle("log_level", LogLevel::class.java, LogLevel.INFO).collect { level ->
        logger.setLevel(level)
    }
}
```

### ViewModel Integration

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    application: Application,
    private val toggles: Toggles
) : ViewModel() {
    
    val featureEnabled = toggles.toggle("new_dashboard", false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}
```

## Alternative Libraries

### Toggles-prefs (One-shot API)
[![Prefs](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs)

For one-time toggle reads without reactive updates. Similar API to SharedPreferences.

```gradle
implementation("se.eelde.toggles:toggles-prefs:0.1.2")
```

```kotlin
import se.eelde.toggles.prefs.TogglesPreferences

val togglesPrefs = TogglesPreferences(context)
val isEnabled = togglesPrefs.getBoolean("feature_toggle_key", false)
```

### Toggles-core (Advanced)
[![Core](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core)

Low-level library for communicating with the Toggles app via ContentProvider. Generally not needed unless you're implementing custom toggle management.

```gradle
implementation("se.eelde.toggles:toggles-core:0.1.2")
```

## Build Variant Configuration

For release builds, use the no-op library variants. They provide the same API but return default values immediately without connecting to the Toggles app.

### No-Op Libraries

```gradle
dependencies {
    // Toggles-Flow
    debugImplementation("se.eelde.toggles:toggles-flow:0.1.2")
    releaseImplementation("se.eelde.toggles:toggles-flow-noop:0.1.2")

    // Toggles-Prefs
    debugImplementation("se.eelde.toggles:toggles-prefs:0.1.2")
    releaseImplementation("se.eelde.toggles:toggles-prefs-noop:0.1.2")
}
```

The no-op libraries do not connect to the Toggles app, immediately return default values, and add no runtime overhead. No code changes needed between debug and release.

### Custom No-Op Implementation

If you need more control, implement the interfaces yourself:

```kotlin
// For Toggles-Flow
class MyNoOpToggles(context: Context) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> = flowOf(defaultValue)
}

// For Toggles-Prefs
class MyNoOpTogglesPreferences(context: Context) : TogglesPreferences {
    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
    override fun getInt(key: String, defValue: Int): Int = defValue
    override fun getString(key: String, defValue: String): String = defValue
    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T = defValue
}
```

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/erikeelde/toggles.git
   cd toggles
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run tests:
   ```bash
   ./gradlew test
   ```

4. Install the sample app:
   ```bash
   ./gradlew :toggles-sample:installDebug
   ```

### Requirements

- JDK 21
- Android SDK API 23+ (for using the libraries)
- Android SDK API 36 (for building the project)
