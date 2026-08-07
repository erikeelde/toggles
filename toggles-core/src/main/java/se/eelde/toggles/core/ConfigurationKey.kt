package se.eelde.toggles.core

/**
 * Validates a configuration key on its way into the Toggles provider.
 *
 * A key is the sole identifier of a toggle: the provider stores configurations under a unique
 * `(applicationId, configurationKey)` index, and every client entry point is keyed by string.
 * A blank key is therefore not a degenerate name but a collision - two toggles built without a
 * key collapse onto the same row, and the second silently reads and writes the first one's value.
 *
 * Applied to every construction path except [Toggle.fromCursor] and
 * [TogglesConfiguration.fromCursor]; throwing on data the caller cannot fix would be the wrong
 * place for this guard.
 */
internal fun requireValidKey(key: String?, columnName: String): String {
    val checked = requireNotNull(key) { "Missing required field: $columnName" }
    require(checked.isNotBlank()) {
        "Blank value for required field: $columnName - toggles are identified by their key"
    }
    return checked
}
