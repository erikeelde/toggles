/*
 * Copyright (C) 2020 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.eelde.toggles.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Handles the "Reply" action in the chat notification.
 */
class ReplyReceiver : BroadcastReceiver() {

    companion object {
        const val KEY_TEXT_REPLY = "reply"
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onReceive(context: Context, intent: Intent) {
        // val results = RemoteInput.getResultsFromIntent(intent) ?: return
        // The message typed in the notification reply.
        // val input = results.getCharSequence(KEY_TEXT_REPLY)?.toString()
        // val uri = intent.data ?: return
        // val chatId = uri.lastPathSegment?.toLong() ?: return

        // if (chatId > 0 && !input.isNullOrBlank()) {
        //     // repository.sendMessage(chatId, input.toString(), null, null)
        //     // We should update the notification so that the user can see that the reply has been
        //     // sent.
        //     // repository.updateNotification(chatId)
        // }
    }
}
