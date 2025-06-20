package se.eelde.toggles.example.toggles2

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

internal class ToggleContentObserver(
    handler: Handler?,
    private val changeCallback: (uri: Uri?) -> Unit
) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        changeCallback.invoke(uri)
    }
}