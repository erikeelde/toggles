package se.eelde.toggles.provider

import android.content.ContentResolver
import android.net.Uri
import android.os.Build

fun ContentResolver.notifyInsert(uri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val flags: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ContentResolver.NOTIFY_INSERT else 0
        try {
            // In ContentResolver.java circa L2828 getContentService() returns null while running robolectric
            notifyChange(uri, null, flags)
        } catch (ignored: NullPointerException) {
        }
    } else {
        @Suppress("DEPRECATION")
        notifyChange(uri, null, false)
    }
}

fun ContentResolver.notifyUpdate(uri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val flags: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ContentResolver.NOTIFY_UPDATE else 0
        try {
            // In ContentResolver.java circa L2828 getContentService() returns null while running robolectric
            notifyChange(uri, null, flags)
        } catch (ignored: NullPointerException) {
        }
    } else {
        @Suppress("DEPRECATION")
        notifyChange(uri, null, false)
    }
}
