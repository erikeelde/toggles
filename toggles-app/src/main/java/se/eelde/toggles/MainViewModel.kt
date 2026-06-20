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

    companion object {
        const val EDITOR_PRESENTATION_DIALOG = "editor_presentation_dialog"
    }
}
