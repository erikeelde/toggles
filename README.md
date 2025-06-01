# Toggles - feature switching

Development tool to store app settings / feature toggles in an external application making in persist across clean data / reinstallations. 

## Toggles app

Toggles can be downloaded on [play store](https://play.google.com/store/apps/details?id=se.eelde.toggles).
and it's sources are on [github](https://github.com/eelde/toggles)

Stores settings / toggles behind a content provider.

This is a development tools meant to facilitate feature switching in an external app so that configurations will be retained across clear data / uninstalls.

## Usage Instructions

1. Install the Toggles app from the Play Store.
2. Add the Toggles library to your project.
3. Use the provided APIs to manage feature toggles in your app.

## Examples

### Toggles-flow library
[![Flow](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow)

Exposes switches from toggles using a kotlin flow.
``` 
    implementation("se.eelde.toggles:toggles-flow:0.0.1")
```

Example usage:
```kotlin
import se.eelde.toggles.flow.Toggles

val toggles = Toggles(context)
toggles.getToggleFlow("feature_toggle_key").collect { isEnabled ->
    // Use the toggle value
}
```

### Toggles-prefs library
[![Prefs](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs)

One-shot fetch of a toggle. Similar API as androids SharedPreferences.
``` 
    implementation("se.eelde.toggles:toggles-prefs:0.0.1")
```

Example usage:
```kotlin
import se.eelde.toggles.prefs.TogglesPreferences

val togglesPrefs = TogglesPreferences(context)
val isEnabled = togglesPrefs.getBoolean("feature_toggle_key", false)
```

## Toggles-core library
[![Core](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core)

Base library exposing common bit to help communicating with the toggles application via the provider. Generally shouldn't be needed unless implementing your own library.
```
    implementation("se.eelde.toggles:toggles-core:0.0.2")
```

## Contribution Guidelines

We welcome contributions! Please follow these steps to contribute:

1. Fork the repository on GitHub.
2. Create a new branch for your feature or bugfix.
3. Write your code and tests.
4. Submit a pull request with a clear description of your changes.

## Reporting Issues

If you encounter any issues or have questions, please open an issue on GitHub.

## Building and Running Locally

To build and run the project locally, follow these steps:

1. Clone the repository: `git clone https://github.com/eelde/toggles.git`
2. Open the project in Android Studio.
3. Build and run the project on an emulator or physical device.
