package se.eelde.toggles.agent

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private const val KEY_CONFIGURATION_ID = "configurationId"
private const val KEY_SCOPE_ID = "scopeId"
private const val KEY_VALUE = "value"
private const val KEY_PACKAGE = "package"
private const val KEY_NAME = "name"

private const val METHOD_SET_CONFIGURATION_VALUE = "setConfigurationValue"
private const val METHOD_CREATE_SCOPE = "createScope"
private const val METHOD_SELECT_SCOPE = "selectScope"

/**
 * Serves every agent mutation endpoint (`adb shell content call ...`) as a JSON string.
 *
 * Never throws: see [AgentReadHandler]'s kdoc for why — `adb shell content call` hides binder
 * exceptions, so every failure must come back as an [AgentErrorEnvelope] rather than an exception.
 */
class AgentCallHandler(
    private val agentDao: AgentDao,
    private val agentMutationDao: AgentMutationDao,
    private val changeNotifier: AgentChangeNotifier,
    private val clock: Clock,
) {

    @Suppress("TooGenericExceptionCaught") // must never throw across the binder; see class kdoc
    fun handle(method: String, extras: Bundle?): String = try {
        when (method) {
            METHOD_SET_CONFIGURATION_VALUE -> setConfigurationValue(extras)
            METHOD_CREATE_SCOPE -> createScope(extras)
            METHOD_SELECT_SCOPE -> selectScope(extras)
            else -> AgentError.json(
                AgentErrorCode.UNKNOWN_ENDPOINT,
                "no such method: $method. Read /describe for the available endpoints."
            )
        }
    } catch (throwable: Throwable) {
        AgentError.json(
            AgentErrorCode.INTERNAL_ERROR,
            throwable.message ?: throwable::class.java.name
        )
    }

    // Each early return here is a distinct validation gate (missing extra, unknown id, disabled
    // app, cross-application scope, rejected value) — flattening them into one boolean expression
    // would make the failure a caller hits harder to see, not easier.
    @Suppress("ReturnCount")
    private fun setConfigurationValue(extras: Bundle?): String {
        val configurationId = extras.longExtra(KEY_CONFIGURATION_ID)
            ?: return missingArgument(KEY_CONFIGURATION_ID)
        val scopeId = extras.longExtra(KEY_SCOPE_ID) ?: return missingArgument(KEY_SCOPE_ID)
        val value = extras.stringExtra(KEY_VALUE) ?: return missingArgument(KEY_VALUE)

        val configuration = agentMutationDao.getConfiguration(configurationId)
            ?: return unknownId("no configuration with id $configurationId")

        val application = agentDao.getApplicationById(configuration.applicationId)
            ?: return unknownId(
                "configuration $configurationId has no owning application " +
                    "(applicationId ${configuration.applicationId})"
            )

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for ${application.packageName}. Enable it in the " +
                    "Toggles app."
            )
        }

        val scope = agentMutationDao.getScope(scopeId)
            ?: return unknownId("no scope with id $scopeId")

        if (scope.applicationId != configuration.applicationId) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "scope $scopeId belongs to a different application than configuration " +
                    "$configurationId; cross-application writes are not allowed."
            )
        }

        val predefinedValues = agentDao.getPredefinedConfigurationValues(application.id)
            .filter { it.configurationId == configurationId }
            .map { it.value }

        val rejectionReason =
            AgentValueValidator.rejectionReason(configuration.type, value, predefinedValues)
        if (rejectionReason != null) {
            return AgentError.json(AgentErrorCode.INVALID_ARGUMENT, rejectionReason)
        }

        upsertConfigurationValue(configurationId, scopeId, value)
        agentMutationDao.touch(configurationId, clock.now())
        changeNotifier.notifyConfigurationChanged(configurationId)

        return agentJson.encodeToString(
            successResponse(application, configuration, scope, value)
        )
    }

    private fun upsertConfigurationValue(configurationId: Long, scopeId: Long, value: String) {
        val existingId = agentMutationDao.findConfigurationValueId(configurationId, scopeId)
        if (existingId != null) {
            agentMutationDao.updateConfigurationValue(configurationId, scopeId, value)
        } else {
            agentMutationDao.insertConfigurationValue(
                TogglesConfigurationValue(
                    id = 0,
                    configurationId = configurationId,
                    value = value,
                    scope = scopeId
                )
            )
        }
    }

    // Each early return is a distinct validation gate, same rationale as setConfigurationValue.
    @Suppress("ReturnCount")
    private fun createScope(extras: Bundle?): String {
        val packageName = extras.stringExtra(KEY_PACKAGE) ?: return missingArgument(KEY_PACKAGE)
        val name = extras.stringExtra(KEY_NAME) ?: return missingArgument(KEY_NAME)

        val application = agentDao.getApplicationByPackageName(packageName)
            ?: return AgentError.json(
                AgentErrorCode.UNKNOWN_PACKAGE,
                "Toggles has no record of $packageName. Read /apps for the known packages."
            )

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for $packageName. Enable it in the Toggles app."
            )
        }

        // A newly created scope must never become the selected one merely by being newer than
        // the currently selected scope's timestamp — creating a scope and selecting it are
        // deliberately separate operations (an agent that wants both makes two calls). Anchoring
        // the new scope's timestamp one second behind the application's current newest scope
        // (or "now" when it has none yet) keeps creation from ever perturbing the selection.
        val timestamp = agentDao.getScopes(application.id).maxOfOrNull { it.timeStamp }
            ?.minus(1.seconds)
            ?: clock.now()

        val scopeId = try {
            agentMutationDao.insertScope(
                TogglesScope(id = 0, applicationId = application.id, name = name, timeStamp = timestamp)
            )
        } catch (_: SQLiteConstraintException) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "$packageName already has a scope named \"$name\"; scope names must be unique " +
                    "per application."
            )
        }

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = METHOD_CREATE_SCOPE,
                summary = "created scope \"$name\" for $packageName",
                applicationPackage = packageName,
                configurationId = null,
                configurationKey = null,
                scopeId = scopeId,
                scopeName = name,
                value = null
            )
        )
    }

    // Each early return is a distinct validation gate, same rationale as setConfigurationValue.
    @Suppress("ReturnCount")
    private fun selectScope(extras: Bundle?): String {
        val packageName = extras.stringExtra(KEY_PACKAGE) ?: return missingArgument(KEY_PACKAGE)
        val scopeId = extras.longExtra(KEY_SCOPE_ID) ?: return missingArgument(KEY_SCOPE_ID)

        val application = agentDao.getApplicationByPackageName(packageName)
            ?: return AgentError.json(
                AgentErrorCode.UNKNOWN_PACKAGE,
                "Toggles has no record of $packageName. Read /apps for the known packages."
            )

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for $packageName. Enable it in the Toggles app."
            )
        }

        val scope = agentMutationDao.getScope(scopeId)
            ?: return unknownId("no scope with id $scopeId")

        if (scope.applicationId != application.id) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "scope $scopeId belongs to a different application than $packageName; " +
                    "cross-application scope selection is not allowed."
            )
        }

        agentMutationDao.touchScope(scopeId, clock.now())
        // Selection changes what every configuration in the application resolves to.
        // notifyScopesChanged() alone is sufficient to reach a running toggles-flow client:
        // observeToggleState registers the same observer on scopeUri() (in addition to
        // configurationUri() and toggleUri()), and that observer re-resolves the full toggle
        // state on any change regardless of which URI fired.
        changeNotifier.notifyScopesChanged()

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = METHOD_SELECT_SCOPE,
                summary = "selected scope \"${scope.name}\" for $packageName",
                applicationPackage = packageName,
                configurationId = null,
                configurationKey = null,
                scopeId = scope.id,
                scopeName = scope.name,
                value = null
            )
        )
    }

    private fun successResponse(
        application: TogglesApplication,
        configuration: TogglesConfiguration,
        scope: TogglesScope,
        value: String,
    ) = AgentMutationResponse(
        method = METHOD_SET_CONFIGURATION_VALUE,
        summary = "${configuration.key} = $value in \"${scope.name}\"",
        applicationPackage = application.packageName,
        configurationId = configuration.id,
        configurationKey = configuration.key,
        scopeId = scope.id,
        scopeName = scope.name,
        value = value
    )

    private fun missingArgument(key: String): String = AgentError.json(
        AgentErrorCode.INVALID_ARGUMENT,
        "missing or wrong-typed required extra \"$key\". `adb shell content call` needs typed " +
            "prefixes, e.g. --extra l:$key:123 for a long or --extra s:$key:foo for a string."
    )

    private fun unknownId(message: String): String = AgentError.json(AgentErrorCode.UNKNOWN_ID, message)

    // Bundle.getLong/getString on a value stored under a different type either throws or
    // silently coerces depending on Android version; Bundle.get() sidesteps both by handing back
    // the raw stored Object (or null when the key is absent) so a type mismatch is just a failed
    // `as?` cast rather than a surprise exception or a wrong value read as if valid.
    @Suppress("DEPRECATION")
    private fun Bundle?.longExtra(key: String): Long? = this?.get(key) as? Long

    @Suppress("DEPRECATION")
    private fun Bundle?.stringExtra(key: String): String? = this?.get(key) as? String
}
