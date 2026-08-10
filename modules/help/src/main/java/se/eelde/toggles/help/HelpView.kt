package se.eelde.toggles.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpView(modifier: Modifier = Modifier, back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.help)) },
                navigationIcon = {
                    IconButton(onClick = { back() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        // Everything is inside a SelectionContainer so any of it — especially the adb
        // commands — can be long-pressed and copied. Developers reading this are typically
        // sitting at a computer with the device connected, and need to move text across.
        SelectionContainer {
            Column(
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Section(WHY_TITLE)
                Body(WHY_BODY)

                Section(USING_TITLE)
                Body(USING_STEP_1)
                Code(USING_DEPENDENCY)
                Body(USING_STEP_2)
                Code(USING_READ)
                Body(USING_STEP_3)
                Body(USING_SCOPES)

                Section(AGENT_TITLE)
                Body(AGENT_BODY)
                Body(AGENT_ENABLE)
                Body(AGENT_BOOTSTRAP)
                Code(AGENT_COMMAND)
                Body(AGENT_SELF_DESCRIBING)
                Body(AGENT_SAFETY)
            }
        }
    }
}

@Composable
private fun Section(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun Code(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private const val WHY_TITLE = "Why Toggles"

private const val WHY_BODY =
    "Feature toggles let you ship code that is switched off, then turn it on without " +
        "building and installing again.\n\n" +
        "Toggles keeps the values in this app rather than inside yours. They survive " +
        "reinstalls of your app, they are visible and editable here, and a running app " +
        "picks up a change immediately — no restart."

private const val USING_TITLE = "Using it"

private const val USING_STEP_1 = "1. Add the library to your app:"

private const val USING_DEPENDENCY =
    "implementation(\"se.eelde.toggles:toggles-flow:<version>\")"

private const val USING_STEP_2 =
    "2. Read a toggle, passing the value your code should use when this app is not installed:"

private const val USING_READ =
    "val enabled = toggles.toggle(\"my_feature\", false)"

private const val USING_STEP_3 =
    "3. Run your app once. The toggle registers itself on first read and appears under " +
        "your app in the Applications tab. Change it there, and your running app sees the " +
        "new value straight away."

private const val USING_SCOPES =
    "Every app gets a default scope and a development scope. A value in the selected scope " +
        "wins; if it has none, the default scope is used. If neither has a value, your app " +
        "falls back to the default you passed in code."

private const val AGENT_TITLE = "Agent integration (Beta)"

private const val AGENT_BODY =
    "An AI coding agent connected over adb can read and change toggles for you — useful " +
        "when it is working on a feature that sits behind one."

private const val AGENT_ENABLE =
    "It is off by default. To turn it on: Applications → Toggles → beta_agent_api → On."

private const val AGENT_BOOTSTRAP =
    "Then run this on your computer, with the device connected:"

// Deliberately one logical line. A backslash continuation would soft-wrap as well on a phone,
// which reads as broken, and would carry a stray backslash if someone copied it.
private const val AGENT_COMMAND =
    "adb shell content read --uri content://se.eelde.toggles.agentprovider/describe"

private const val AGENT_SELF_DESCRIBING =
    "That prints the whole API as JSON — every endpoint and every method, each with a " +
        "runnable example command. It is the only one you need to type: the device tells " +
        "you the rest, so nothing has to be copied off this screen."

private const val AGENT_SAFETY =
    "The agent API is reachable only over adb or root. Apps on the device cannot use it. " +
        "You can switch it off for a single app from that app's overflow menu, or turn the " +
        "whole thing off again with beta_agent_api."
