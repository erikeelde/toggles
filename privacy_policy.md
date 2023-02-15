## Toggles: Privacy Policy

This is an open source app developed by Erik Eelde.

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to
collect any personally identifiable information. All data created by the you (the user) is stored on
your device only, and can be simply erased by clearing the app's data or uninstalling it.

It sends crashes to crashlytics to help fix crashes - I do not know how to delete that data.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/erikeelde/toggles/blob/main/toggles-app/src/main/AndroidManifest.xml#L16-L17

| Permission | Why it is required                                                                                                                                                        |
| :---: |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `android.permission.INTERNET` | Needed to be able to send crashes to crashlytics.                                                                                                                         |
| `android.permission.KILL_BACKGROUND_PROCESSES` | Allows the app to try to kill your apps process and recreating with default parameters. I've found this to be helpful when you toggle singleton state in the application. |
| `android.permission.POST_NOTIFICATIONS` | Required since the app uses leak canary and I have been experimenting with adding notifications as a port of toggles UX.                                                  |

If you find any security vulnerability that has been inadvertently caused by me, or have any
question regarding how the app protects your privacy, please post a discussion on GitHub, and I will
surely try to fix it/help you.


