package se.eelde.toggles

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.flow.Toggles
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    toggles: Toggles,
) : ViewModel() {
    /**
     * When true, the leaf editor/scope screens render as a dialog overlay; when false they
     * render as the adaptive extra pane. Dogfooded through the app's own toggles-flow instance.
     */
    val editorAsDialog: Flow<Boolean> =
        toggles.toggle(EDITOR_PRESENTATION_DIALOG, true)

    /**
     * Global kill switch for the adb-facing agent API (`modules/agent`). Defaults to off: the
     * feature is a beta/evaluated toggle (hence the `beta_` prefix, which also groups it with
     * other evaluated features in the alphabetically-listed UI), and a fresh install should not
     * expose the agent provider until the user has opened this app and opted in.
     *
     * The agent ContentProvider (`TogglesAgentProvider`, via `AgentApiGate`) reads this exact same
     * key straight from the database — synchronously, bypassing this flow and toggles-flow
     * entirely, since a binder call inside the provider's own call path cannot go back through the
     * provider it is serving. Both surfaces resolve the value through the same scope-chain logic,
     * so they can never disagree.
     */
    val agentApiEnabled: Flow<Boolean> = toggles.toggle(BETA_AGENT_API, false)

    companion object {
        const val EDITOR_PRESENTATION_DIALOG = "editor_presentation_dialog"
        const val BETA_AGENT_API = "beta_agent_api"
    }
}
