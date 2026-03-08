# Toggles - Feature Switching for Android Development

A powerful development tool that stores app settings and feature toggles in an external application, ensuring they persist across clean data operations and app reinstalls. Perfect for Android developers who need reliable feature flag management during development.

## Quick Start with Toggles-Flow (Recommended)

The **toggles-flow** library provides a reactive approach to feature toggles using Kotlin Flow, making it easy to respond to configuration changes in real-time.

### Installation

Add the toggles-flow library to your project:

```gradle
implementation("se.eelde.toggles:toggles-flow:0.0.3")
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

- Download from [Google Play Store](https://play.google.com/store/apps/details?id=se.eelde.toggles)
- View source code on [GitHub](https://github.com/eelde/toggles)

The app stores your settings behind a content provider, ensuring configurations persist across app reinstalls and clean data operations.

## Advanced Examples

### Scoped Toggles (Per-User Feature Flags)

Want to enable different features for different users or environments? Use scoped instances:

```kotlin
import se.eelde.toggles.flow.TogglesImpl

class MultiUserActivity : AppCompatActivity() {
    // Create separate toggle instances for each user
    private val adminToggles = TogglesImpl(this, scope = "admin")
    private val guestToggles = TogglesImpl(this, scope = "guest")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            // Admin sees advanced features
            adminToggles.toggle("advanced_mode", false).collect { enabled ->
                if (enabled) showAdminFeatures()
            }
        }
        
        lifecycleScope.launch {
            // Guest has limited features
            guestToggles.toggle("advanced_mode", false).collect { enabled ->
                // Will use guest scope value
                if (enabled) showAdminFeatures()
            }
        }
    }
}
```

**Use cases for scoped toggles:**
- Per-user feature flags (admin vs regular users)
- A/B testing with different user groups
- Environment-specific settings (dev, staging, prod)
- Multi-tenant applications

The scope parameter is optional. If not provided, the system uses the currently selected scope in the Toggles app.

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

### Toggles-prefs library (One-shot API)
[![Prefs](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs)

For simple one-time toggle fetching without reactive updates. Similar API to Android's SharedPreferences.

```gradle
implementation("se.eelde.toggles:toggles-prefs:0.0.2")
```

```kotlin
import se.eelde.toggles.prefs.TogglesPreferences
import se.eelde.toggles.prefs.TogglesPreferencesImpl

// Default scope
val togglesPrefs = TogglesPreferencesImpl(context)
val isEnabled = togglesPrefs.getBoolean("feature_toggle_key", false)

// With custom scope for per-user toggles
val userToggles = TogglesPreferencesImpl(context, scope = "user_123")
val userFeature = userToggles.getBoolean("feature_toggle_key", false)
```

**Note:** Consider using toggles-flow for reactive updates and better integration with modern Android development patterns. Scoped toggles are also supported in the prefs library.

### Toggles-core library (Advanced)
[![Core](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core)

Low-level library for communicating with the toggles application via content provider. Generally not needed unless you're implementing custom toggle management.

```gradle
implementation("se.eelde.toggles:toggles-core:0.0.3")
```

## Build Variant Configuration

For release builds, you have two options to disable the toggles functionality:

1. **Use the provided no-op libraries** (recommended for convenience)
2. **Implement your own no-op variant** of the `Toggles` or `TogglesPreferences` interface

The no-op versions return default values immediately without connecting to the Toggles app, eliminating unnecessary overhead in production builds.

### Option 1: Using the No-Op Libraries (Recommended)

To enable feature toggles in debug builds while automatically disabling them in release builds, configure your dependencies based on build type:

#### For Toggles-Flow (Recommended)

```gradle
dependencies {
    debugImplementation("se.eelde.toggles:toggles-flow:0.0.3")
    releaseImplementation("se.eelde.toggles:toggles-flow-noop:0.0.3")
}
```

#### For Toggles-Prefs

```gradle
dependencies {
    debugImplementation("se.eelde.toggles:toggles-prefs:0.0.2")
    releaseImplementation("se.eelde.toggles:toggles-prefs-noop:0.0.2")
}
```

### How No-Op Implementations Work

The no-op libraries provide identical APIs but with minimal implementations:
- They **do not** connect to the Toggles app
- They **immediately return** the default values you provide
- They **add zero runtime overhead** to your release builds
- They **maintain the same API**, so no code changes are needed

#### Example: No-Op Behavior

```kotlin
// In debug builds: reads from Toggles app, observes changes
// In release builds: immediately returns 'false', never changes
toggles.toggle("enable_new_feature", false).collect { isEnabled ->
    // Debug: reflects actual toggle value from Toggles app
    // Release: always receives 'false' (the default value)
}
```

### Benefits

- **Development flexibility**: Full feature toggle control during development
- **Production efficiency**: Zero overhead in release builds
- **Seamless transition**: Same code works in both debug and release
- **No app dependency**: Release builds don't require the Toggles app to be installed

### Option 2: Implementing Your Own No-Op Variant

If you prefer more control or want to customize the behavior, you can implement your own no-op version:

#### For Toggles-Flow

```kotlin
class MyNoOpToggles(context: Context) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> = flowOf(defaultValue)
}
```

#### For Toggles-Prefs

```kotlin
class MyNoOpTogglesPreferences(context: Context) : TogglesPreferences {
    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
    override fun getInt(key: String, defValue: Int): Int = defValue
    override fun getString(key: String, defValue: String): String = defValue
    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T = defValue
}
```

Then configure your dependency injection or factory to provide the appropriate implementation based on build type. The provided no-op libraries exist for convenience so you don't have to write this boilerplate yourself.

## Contribution Guidelines

We welcome contributions! Please follow these steps to contribute:

1. Fork the repository on GitHub.
2. Create a new branch for your feature or bugfix.
3. Write your code and tests.
4. Submit a pull request with a clear description of your changes.

## Reporting Issues

If you encounter any issues or have questions, please open an issue on GitHub.

## Development Setup

### Building and Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/eelde/toggles.git
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

4. Install the sample app to test the libraries:
   ```bash
   ./gradlew :toggles-sample:installDebug
   ```

### Requirements

- Android Studio Arctic Fox or newer
- JDK 17 or higher
- Android SDK API 21+ (for using the libraries)
- Android SDK API 35 (for building the sample app)
