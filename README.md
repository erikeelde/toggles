# Toggles - feature switching

Development tool to store app settings / feature toggles in an external application making in persist across clean data / reinstallations. 

##

## Toggles app

Toggles can be downloaded on [play store](https://play.google.com/store/apps/details?id=se.eelde.toggles).
and it's sources are on [github](https://github.com/eelde/toggles)

Stores settings / toggles behind a content provider.

This is a development tools meant to facilitate feature switching in an external app so that configurations will be retained across clear data / uninstalls.

2 premade libraries to talk to the toggles application. "Prefs" and "Coroutines":

## Toggles-coroutines library
Exposes switches from toggles using a kotlin stream.

## Toggles-prefs library
One-shot fetch of a toggle. Similar API as androids SharedPreferences.

#### Previously known as wrench
The idea dates way back to and was inspired by the now removed [Dash Clock Widget](https://play.google.com/store/apps/details?id=net.nurik.roman.dashclock) as well as the still maintained [muzei](https://play.google.com/store/apps/details?id=net.nurik.roman.muzei).

Wrench can be downloaded on [play store](https://play.google.com/store/apps/details?id=com.izettle.wrench).
and it's sources are on [github](https://github.com/iZettle/wrench)
I made wrench while employed at izettle and to be able to continue to provide updates I unfortunately needed to rerelease the application.
