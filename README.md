# Toggles - feature switching

Development tool to store app settings / feature toggles in an external application making in persist across clean data / reinstallations. 

## Toggles app

Toggles can be downloaded on [play store](https://play.google.com/store/apps/details?id=se.eelde.toggles).
and it's sources are on [github](https://github.com/eelde/toggles)

Stores settings / toggles behind a content provider.

This is a development tools meant to facilitate feature switching in an external app so that configurations will be retained across clear data / uninstalls.

2 premade libraries to talk to the toggles application. "Prefs" and "Flow" - Backed by a common core library:

## Toggles-flow library
[![Flow](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-flow)

Exposes switches from toggles using a kotlin flow.
``` 
    implementation("se.eelde.toggles:toggles-flow:0.0.1")
```

## Toggles-prefs library
[![Prefs](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-prefs)

One-shot fetch of a toggle. Similar API as androids SharedPreferences.
``` 
    implementation("se.eelde.toggles:toggles-prefs:0.0.1")
```

## Toggles-core library
[![Core](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.eelde.toggles/toggles-core)

Base library exposing common bit to help communicating with the toggles application via the provider. Generally shouldn't be needed unless implementing your own library.
```
    implementation("se.eelde.toggles:toggles-core:0.0.2")
```

#### Previously known as wrench
The idea dates way back to and was inspired by the now removed [Dash Clock Widget](https://play.google.com/store/apps/details?id=net.nurik.roman.dashclock) as well as the still maintained [muzei](https://play.google.com/store/apps/details?id=net.nurik.roman.muzei).

Wrench can be downloaded on [play store](https://play.google.com/store/apps/details?id=com.izettle.wrench).
and it's sources are on [github](https://github.com/iZettle/wrench)
I made wrench while employed at izettle and to be able to continue to provide updates I unfortunately needed to rerelease the application.
