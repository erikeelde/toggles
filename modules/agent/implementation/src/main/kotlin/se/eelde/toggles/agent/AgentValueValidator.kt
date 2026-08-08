package se.eelde.toggles.agent

/**
 * Nothing else in Toggles validates a value against its configuration's type — `@ToggleType` is a
 * lint-only `StringDef` and the ContentProvider accepts any string. The consequences on the client
 * differ by type and are both bad: a malformed boolean is parsed by `String.toBoolean()` and
 * silently becomes `false`, while a malformed integer throws `NumberFormatException` inside the
 * consuming app. This validator exists so the agent API is not the easiest way to corrupt an app's
 * toggle state.
 */
object AgentValueValidator {

    private const val TYPE_BOOLEAN = "boolean"
    private const val TYPE_INTEGER = "integer"
    private const val TYPE_STRING = "string"
    private const val TYPE_ENUM = "enum"

    /**
     * Every configuration type this API accepts. The single source of truth for that set — nothing
     * else (createConfiguration's argument validation, /describe's model documentation) may keep
     * its own copy of this list.
     */
    val VALID_TYPES: List<String> = listOf(TYPE_BOOLEAN, TYPE_INTEGER, TYPE_STRING, TYPE_ENUM)

    /**
     * Returns null when the value is acceptable, otherwise a human-readable reason the agent can
     * act on.
     */
    fun rejectionReason(type: String, value: String, predefinedValues: List<String?>): String? =
        when (type) {
            TYPE_BOOLEAN -> if (value == "true" || value == "false") {
                null
            } else {
                "a boolean configuration accepts exactly \"true\" or \"false\", got \"$value\". " +
                    "The client library parses with String.toBoolean(), which would silently read " +
                    "this as false rather than reporting an error."
            }

            TYPE_INTEGER -> if (value.toIntOrNull() != null) {
                null
            } else {
                "an integer configuration accepts a decimal Int, got \"$value\". The client " +
                    "library parses with String.toInt(), which would throw NumberFormatException " +
                    "inside the consuming app."
            }

            TYPE_STRING -> null

            TYPE_ENUM -> if (predefinedValues.contains(value)) {
                null
            } else {
                "an enum configuration accepts only its predefined values " +
                    "${predefinedValues.filterNotNull()}, got \"$value\"."
            }

            else -> "unknown configuration type \"$type\"; cannot validate the value."
        }
}
