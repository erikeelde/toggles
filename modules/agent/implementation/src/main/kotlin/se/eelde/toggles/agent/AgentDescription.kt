package se.eelde.toggles.agent

import kotlinx.serialization.Serializable

@Serializable
data class AgentEndpoint(
    val path: String,
    val returns: String,
    val example: String,
)

@Serializable
data class AgentMethodArgument(
    val name: String,
    val type: String,
    val required: Boolean,
    val description: String,
)

@Serializable
data class AgentMethod(
    val method: String,
    val arguments: List<AgentMethodArgument>,
    val returns: String,
    val example: String,
)

@Serializable
data class AgentModelDocumentation(
    val configurationTypes: List<String>,
    val scopeResolution: String,
    val agentControl: String,
    val agentApiEnablement: String,
    val callers: String,
    val valueFormats: Map<String, String>,
    val callResultWrapping: String,
)

@Serializable
data class AgentDescriptionDocument(
    val agentApiVersion: Int,
    val togglesAppVersion: String,
    val authority: String,
    val endpoints: List<AgentEndpoint>,
    val methods: List<AgentMethod>,
    val model: AgentModelDocumentation,
    val errors: List<String>,
    /**
     * Whether the agent API is currently switched on. `/describe` is reachable regardless of this
     * flag — that is deliberate, see [AgentErrorCode.AGENT_API_DISABLED] — so this field is how an
     * agent tells "Toggles is installed but the API is off" apart from "Toggles is not installed at
     * all". When false, every other endpoint and every `call()` method returns
     * [AgentErrorCode.AGENT_API_DISABLED] instead of serving data. Enable it with the
     * `beta_agent_api` toggle on the `se.eelde.toggles` application, which appears in the Toggles
     * app once it has been opened at least once.
     */
    val enabled: Boolean,
)

/**
 * The self-describing API contract. Host-side tooling hardcodes only the `/describe` command and
 * discovers everything else from here, so it can never drift from the installed APK.
 */
object AgentDescription {

    const val AGENT_API_VERSION = 1
    const val AGENT_AUTHORITY = "se.eelde.toggles.agentprovider"

    fun document(appVersionName: String, apiEnabled: Boolean): AgentDescriptionDocument =
        AgentDescriptionDocument(
            agentApiVersion = AGENT_API_VERSION,
            togglesAppVersion = appVersionName,
            authority = AGENT_AUTHORITY,
            endpoints = endpoints(),
            methods = Methods.all(),
            model = model(),
            errors = AgentErrorCode.entries.map { it.wireValue },
            enabled = apiEnabled
        )

    fun json(appVersionName: String, apiEnabled: Boolean): String =
        agentJson.encodeToString(document(appVersionName, apiEnabled))

    private fun endpoints(): List<AgentEndpoint> = listOf(
        AgentEndpoint(
            path = "/describe",
            returns = "This document.",
            example = "adb shell content read --uri content://$AGENT_AUTHORITY/describe"
        ),
        AgentEndpoint(
            path = "/apps",
            returns = "Every application Toggles knows about, with its agentControlEnabled flag.",
            example = "adb shell content read --uri content://$AGENT_AUTHORITY/apps"
        ),
        AgentEndpoint(
            path = "/apps/{package}",
            returns = "One application's scopes, configurations, every per-scope value, " +
                "predefined values and effective values.",
            example = "adb shell content read --uri content://$AGENT_AUTHORITY/apps/com.example.app"
        )
    )

    // Split out of AgentDescription itself (one builder function per mutation method, plus the
    // shared example-command builder) so that object stays under detekt's TooManyFunctions
    // threshold instead of reaching for a blanket suppression.
    private object Methods {

        private const val TYPE_LONG = "long"
        private const val TYPE_STRING = "string"

        fun all(): List<AgentMethod> = listOf(
            setConfigurationValue(),
            createConfiguration(),
            deleteConfiguration(),
            deleteConfigurationValue(),
            createScope(),
            selectScope(),
            deleteScope()
        )

        private fun callExample(method: String, vararg extras: Pair<String, String>): String {
            val extraArgs = extras.joinToString(separator = " ") { (binding, value) ->
                "--extra $binding:$value"
            }
            return "adb shell content call --uri content://${AgentDescription.AGENT_AUTHORITY} " +
                "--method $method $extraArgs"
        }

        private fun setConfigurationValue() = AgentMethod(
            method = AgentCallContract.METHOD_SET_CONFIGURATION_VALUE,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_CONFIGURATION_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The configuration's id, from /apps/{package}."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_SCOPE_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The scope to write the value into, from /apps/{package}."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_VALUE,
                    type = TYPE_STRING,
                    required = true,
                    description = "The new value, formatted per the configuration's type — see " +
                        "valueFormats."
                )
            ),
            returns = "An AgentMutationResponse summarizing what changed.",
            example = callExample(
                AgentCallContract.METHOD_SET_CONFIGURATION_VALUE,
                "${AgentCallContract.KEY_CONFIGURATION_ID}:l" to "47",
                "${AgentCallContract.KEY_SCOPE_ID}:l" to "3",
                "${AgentCallContract.KEY_VALUE}:s" to "true"
            )
        )

        private fun createConfiguration() = AgentMethod(
            method = AgentCallContract.METHOD_CREATE_CONFIGURATION,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_PACKAGE,
                    type = TYPE_STRING,
                    required = true,
                    description = "The application's package name. If Toggles has never seen it, " +
                        "the application (with its default and development scopes) is created " +
                        "on demand, even if the package cannot be confirmed as installed — " +
                        "package visibility filtering hides most never-run packages from " +
                        "Toggles, so this call succeeds regardless. Check " +
                        "AgentMutationResponse.packageVerified (and its summary) to see whether " +
                        "the package could actually be confirmed; false is where a typo would " +
                        "show up."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_KEY,
                    type = TYPE_STRING,
                    required = true,
                    description = "The new configuration's key. Must be unique within the " +
                        "application."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_TYPE,
                    type = TYPE_STRING,
                    required = true,
                    description = "One of model.configurationTypes."
                )
            ),
            returns = "An AgentMutationResponse with the new configuration's id. " +
                "packageVerified is non-null only when this call just created the application " +
                "row: true if the package was confirmed installed, false if it could not be " +
                "confirmed (verify the package name before trusting it).",
            example = callExample(
                AgentCallContract.METHOD_CREATE_CONFIGURATION,
                "${AgentCallContract.KEY_PACKAGE}:s" to "com.example.app",
                "${AgentCallContract.KEY_KEY}:s" to "feature_x",
                "${AgentCallContract.KEY_TYPE}:s" to "boolean"
            )
        )

        private fun deleteConfiguration() = AgentMethod(
            method = AgentCallContract.METHOD_DELETE_CONFIGURATION,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_CONFIGURATION_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The configuration's id, from /apps/{package}. Deletes its " +
                        "value in every scope along with it."
                )
            ),
            returns = "An AgentMutationResponse confirming what was deleted.",
            example = callExample(
                AgentCallContract.METHOD_DELETE_CONFIGURATION,
                "${AgentCallContract.KEY_CONFIGURATION_ID}:l" to "47"
            )
        )

        private fun deleteConfigurationValue() = AgentMethod(
            method = AgentCallContract.METHOD_DELETE_CONFIGURATION_VALUE,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_CONFIGURATION_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The configuration's id, from /apps/{package}."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_SCOPE_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The scope whose override to remove, from /apps/{package}."
                )
            ),
            returns = "An AgentMutationResponse whose value field is the configuration's " +
                "effectiveValue after the removal. This removes the override row so resolution " +
                "falls back to the next scope in the chain — it is not the same as setting the " +
                "value to match the default: an override that happens to equal the default still " +
                "shadows it, so a later change to the default would not propagate, whereas a " +
                "removed override does propagate. Removing an override that does not exist is a " +
                "successful no-op — the response says nothing was removed and reports the " +
                "current effectiveValue unchanged; it is not an error.",
            example = callExample(
                AgentCallContract.METHOD_DELETE_CONFIGURATION_VALUE,
                "${AgentCallContract.KEY_CONFIGURATION_ID}:l" to "47",
                "${AgentCallContract.KEY_SCOPE_ID}:l" to "3"
            )
        )

        private fun createScope() = AgentMethod(
            method = AgentCallContract.METHOD_CREATE_SCOPE,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_PACKAGE,
                    type = TYPE_STRING,
                    required = true,
                    description = "The application's package name. Must already be known to " +
                        "Toggles — see /apps."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_NAME,
                    type = TYPE_STRING,
                    required = true,
                    description = "The new scope's name. Must be unique within the application."
                )
            ),
            returns = "An AgentMutationResponse with the new scope's id. The new scope is not " +
                "selected automatically.",
            example = callExample(
                AgentCallContract.METHOD_CREATE_SCOPE,
                "${AgentCallContract.KEY_PACKAGE}:s" to "com.example.app",
                "${AgentCallContract.KEY_NAME}:s" to "staging"
            )
        )

        private fun selectScope() = AgentMethod(
            method = AgentCallContract.METHOD_SELECT_SCOPE,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_PACKAGE,
                    type = TYPE_STRING,
                    required = true,
                    description = "The application's package name."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_SCOPE_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The scope to select, from /apps/{package}."
                )
            ),
            returns = "An AgentMutationResponse confirming which scope is now selected.",
            example = callExample(
                AgentCallContract.METHOD_SELECT_SCOPE,
                "${AgentCallContract.KEY_PACKAGE}:s" to "com.example.app",
                "${AgentCallContract.KEY_SCOPE_ID}:l" to "3"
            )
        )

        private fun deleteScope() = AgentMethod(
            method = AgentCallContract.METHOD_DELETE_SCOPE,
            arguments = listOf(
                AgentMethodArgument(
                    name = AgentCallContract.KEY_PACKAGE,
                    type = TYPE_STRING,
                    required = true,
                    description = "The application's package name."
                ),
                AgentMethodArgument(
                    name = AgentCallContract.KEY_SCOPE_ID,
                    type = TYPE_LONG,
                    required = true,
                    description = "The scope to delete, from /apps/{package}. Its values in " +
                        "every scope-specific row are deleted along with it."
                )
            ),
            returns = "An AgentMutationResponse confirming what was deleted. The default scope " +
                "can never be deleted — deleting it would leave every configuration in the " +
                "application with no resolvable value, so that call is rejected with " +
                "invalid_argument instead. Deleting the currently selected scope is allowed; " +
                "selection then silently moves to the next most recent scope, and the summary " +
                "names which one — relay it, since it changes what every toggle in the " +
                "application resolves to.",
            example = callExample(
                AgentCallContract.METHOD_DELETE_SCOPE,
                "${AgentCallContract.KEY_PACKAGE}:s" to "com.example.app",
                "${AgentCallContract.KEY_SCOPE_ID}:l" to "3"
            )
        )
    }

    private fun model(): AgentModelDocumentation = AgentModelDocumentation(
        configurationTypes = AgentValueValidator.VALID_TYPES,
        scopeResolution = "Each application has a default scope and zero or more additional " +
            "scopes. The selected scope is the one with the most recent selectedTimestamp. " +
            "Resolution consults exactly two scopes: the selected scope, then the default " +
            "scope. Value rows in any other scope are ignored entirely — they are reported " +
            "under each configuration's values array but never affect effectiveValue. The " +
            "fallback to the default scope happens only when the selected scope has no value " +
            "row at all; a row whose value is null still counts as a hit and stops the " +
            "fallback. effectiveValue is the result of that resolution, and null means the " +
            "application observes no value from Toggles — check the values array before " +
            "concluding a configuration has no value anywhere.",
        agentControl = "Every /apps/{package} call requires that application's " +
            "agentControlEnabled flag to be true. It defaults to true and can be turned off " +
            "per application inside the Toggles app.",
        agentApiEnablement = "The entire agent API is switched off by default — this `enabled` " +
            "field on this document reports the current state. Turn it on with the " +
            "\"beta_agent_api\" toggle on the se.eelde.toggles application itself (Toggles " +
            "dogfoods its own client library for this), which appears in the Toggles app once " +
            "the app has been opened at least once. While disabled, /describe keeps working — " +
            "that is how you tell \"Toggles is installed but the API is off\" apart from " +
            "\"Toggles is not installed\" — but every other endpoint and every call() method " +
            "returns agent_api_disabled instead of serving data or applying a change. If a " +
            "call returns that code, relay it to the user rather than trying to work around " +
            "it: only they can flip the toggle inside the Toggles app.",
        callers = "Only uid 2000 (shell) and uid 0 (root) may call this provider. Every other " +
            "caller receives a not_authorized error.",
        valueFormats = mapOf(
            "boolean" to "Exactly \"true\" or \"false\". The client library parses with " +
                "Kotlin's String.toBoolean(), which returns true only for a case-insensitive " +
                "\"true\" and silently returns false for anything else — a malformed value " +
                "does not error, it reads as false.",
            "integer" to "A decimal integer string. The client library parses with " +
                "String.toInt(), which throws NumberFormatException in the consuming app if " +
                "the value is not a valid integer.",
            "string" to "Any string.",
            "enum" to "Must be one of the values listed in that configuration's " +
                "predefinedValues array."
        ),
        callResultWrapping = "adb shell content call wraps its output in " +
            "\"Result: Bundle[{result=<json>}]\" rather than printing the JSON alone — " +
            "confirmed on device. A host parsing this output must strip that " +
            "\"Result: Bundle[{result=...}]\" wrapper (and its trailing \"}]\") before " +
            "decoding what remains as JSON. adb shell content read (used by every /describe, " +
            "/apps and /apps/{package} example above) has no such wrapper; only content call " +
            "(used by every method example above) does."
    )
}
