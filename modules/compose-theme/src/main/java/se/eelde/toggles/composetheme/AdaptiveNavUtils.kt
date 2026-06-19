package se.eelde.toggles.composetheme

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable

/**
 * Returns true when the navigation icon (back arrow) should be shown in the **detail pane**
 * of a list-detail scaffold — i.e. only when the list pane (secondary) is not currently visible.
 */
@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun rememberShowNavigationIconInDetailPane(): Boolean {
    val scope = LocalListDetailSceneScope.current ?: return true
    return scope.scaffoldTransitionScope.scaffoldStateTransition.targetState.secondary == PaneAdaptedValue.Hidden
}

/**
 * Returns true when the navigation icon (back arrow) should be shown in the **extra pane**
 * of a list-detail scaffold — i.e. only when the detail pane (primary) is not currently visible.
 */
@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun rememberShowNavigationIconInExtraPane(): Boolean {
    val scope = LocalListDetailSceneScope.current ?: return true
    return scope.scaffoldTransitionScope.scaffoldStateTransition.targetState.primary == PaneAdaptedValue.Hidden
}
