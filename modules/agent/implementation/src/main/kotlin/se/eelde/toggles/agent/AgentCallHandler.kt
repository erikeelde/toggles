package se.eelde.toggles.agent

import android.content.pm.PackageManager
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

/**
 * Every method name and extra-key name AgentCallHandler actually reads, in one place.
 *
 * [AgentDescription] documents each method's arguments by referencing these same constants rather
 * than typing the names out again, so an agent reading `/describe` and a developer reading this
 * class see identical names by construction — there is nowhere for the two to drift apart.
 */
internal object AgentCallContract {
    const val METHOD_SET_CONFIGURATION_VALUE = "setConfigurationValue"
    const val METHOD_CREATE_SCOPE = "createScope"
    const val METHOD_SELECT_SCOPE = "selectScope"
    const val METHOD_CREATE_CONFIGURATION = "createConfiguration"
    const val METHOD_DELETE_CONFIGURATION = "deleteConfiguration"

    const val KEY_CONFIGURATION_ID = "configurationId"
    const val KEY_SCOPE_ID = "scopeId"
    const val KEY_VALUE = "value"
    const val KEY_PACKAGE = "package"
    const val KEY_NAME = "name"
    const val KEY_KEY = "key"
    const val KEY_TYPE = "type"
}

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
    private val controlNotifier: AgentControlNotifier,
    private val clock: Clock,
    packageManager: PackageManager,
) {

    private val applicationProvisioner =
        AgentApplicationProvisioner(agentDao, agentMutationDao, packageManager, clock)

    @Suppress("TooGenericExceptionCaught") // must never throw across the binder; see class kdoc
    fun handle(method: String, extras: Bundle?): String = try {
        when (method) {
            AgentCallContract.METHOD_SET_CONFIGURATION_VALUE -> setConfigurationValue(extras)
            AgentCallContract.METHOD_CREATE_SCOPE -> createScope(extras)
            AgentCallContract.METHOD_SELECT_SCOPE -> selectScope(extras)
            AgentCallContract.METHOD_CREATE_CONFIGURATION -> createConfiguration(extras)
            AgentCallContract.METHOD_DELETE_CONFIGURATION -> deleteConfiguration(extras)
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
        val configurationId = extras.longExtra(AgentCallContract.KEY_CONFIGURATION_ID)
            ?: return missingArgument(AgentCallContract.KEY_CONFIGURATION_ID)
        val scopeId = extras.longExtra(AgentCallContract.KEY_SCOPE_ID)
            ?: return missingArgument(AgentCallContract.KEY_SCOPE_ID)
        val value = extras.stringExtra(AgentCallContract.KEY_VALUE)
            ?: return missingArgument(AgentCallContract.KEY_VALUE)

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
        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

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
        val packageName = extras.stringExtra(AgentCallContract.KEY_PACKAGE)
            ?: return missingArgument(AgentCallContract.KEY_PACKAGE)
        val name = extras.stringExtra(AgentCallContract.KEY_NAME)
            ?: return missingArgument(AgentCallContract.KEY_NAME)

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

        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_CREATE_SCOPE,
                summary = "created scope \"$name\" for $packageName",
                applicationPackage = packageName,
                configurationId = null,
                configurationKey = null,
                scopeId = scopeId,
                scopeName = name,
                value = null,
                packageVerified = null
            )
        )
    }

    // Each early return is a distinct validation gate, same rationale as setConfigurationValue.
    @Suppress("ReturnCount")
    private fun selectScope(extras: Bundle?): String {
        val packageName = extras.stringExtra(AgentCallContract.KEY_PACKAGE)
            ?: return missingArgument(AgentCallContract.KEY_PACKAGE)
        val scopeId = extras.longExtra(AgentCallContract.KEY_SCOPE_ID)
            ?: return missingArgument(AgentCallContract.KEY_SCOPE_ID)

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
        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_SELECT_SCOPE,
                summary = "selected scope \"${scope.name}\" for $packageName",
                applicationPackage = packageName,
                configurationId = null,
                configurationKey = null,
                scopeId = scope.id,
                scopeName = scope.name,
                value = null,
                packageVerified = null
            )
        )
    }

    // Each early return is a distinct validation gate, same rationale as setConfigurationValue.
    // Type is validated before touching PackageManager or the database so an invalid type never
    // has the side effect of creating an application row for a package name that turns out to be
    // rejected anyway.
    @Suppress("ReturnCount")
    private fun createConfiguration(extras: Bundle?): String {
        val packageName = extras.stringExtra(AgentCallContract.KEY_PACKAGE)
            ?: return missingArgument(AgentCallContract.KEY_PACKAGE)
        val key = extras.stringExtra(AgentCallContract.KEY_KEY)
            ?: return missingArgument(AgentCallContract.KEY_KEY)
        val type = extras.stringExtra(AgentCallContract.KEY_TYPE)
            ?: return missingArgument(AgentCallContract.KEY_TYPE)

        if (type !in AgentValueValidator.VALID_TYPES) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "unknown configuration type \"$type\"; must be one of " +
                    "${AgentValueValidator.VALID_TYPES}."
            )
        }

        // Always resolves — see AgentApplicationProvisioner's kdoc for why an unresolvable
        // package is no longer a failure, only a note in the response below.
        val resolution = applicationProvisioner.resolveOrCreate(packageName)
        val application = resolution.application

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for $packageName. Enable it in the Toggles app."
            )
        }

        val configurationId = try {
            agentMutationDao.insertConfiguration(
                TogglesConfiguration(
                    id = 0,
                    applicationId = application.id,
                    key = key,
                    type = type,
                    lastUse = clock.now()
                )
            )
        } catch (_: SQLiteConstraintException) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "$packageName already has a configuration keyed \"$key\"; configuration keys " +
                    "must be unique per application."
            )
        }

        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_CREATE_CONFIGURATION,
                summary = "created configuration \"$key\" ($type) for $packageName" +
                    unverifiedPackageNote(packageName, resolution.packageVerified),
                applicationPackage = packageName,
                configurationId = configurationId,
                configurationKey = key,
                scopeId = null,
                scopeName = null,
                value = null,
                packageVerified = resolution.packageVerified
            )
        )
    }

    // Each early return is a distinct validation gate, same rationale as setConfigurationValue.
    @Suppress("ReturnCount")
    private fun deleteConfiguration(extras: Bundle?): String {
        val configurationId = extras.longExtra(AgentCallContract.KEY_CONFIGURATION_ID)
            ?: return missingArgument(AgentCallContract.KEY_CONFIGURATION_ID)

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

        // ON DELETE CASCADE (see TogglesConfigurationValue's foreign key) removes every value row
        // for this configuration along with it.
        agentMutationDao.deleteConfiguration(configurationId)
        changeNotifier.notifyConfigurationChanged(configurationId)
        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_DELETE_CONFIGURATION,
                summary = "deleted configuration \"${configuration.key}\" from " +
                    application.packageName,
                applicationPackage = application.packageName,
                configurationId = configurationId,
                configurationKey = configuration.key,
                scopeId = null,
                scopeName = null,
                value = null,
                packageVerified = null
            )
        )
    }

    private fun successResponse(
        application: TogglesApplication,
        configuration: TogglesConfiguration,
        scope: TogglesScope,
        value: String,
    ) = AgentMutationResponse(
        method = AgentCallContract.METHOD_SET_CONFIGURATION_VALUE,
        summary = "${configuration.key} = $value in \"${scope.name}\"",
        applicationPackage = application.packageName,
        configurationId = configuration.id,
        configurationKey = configuration.key,
        scopeId = scope.id,
        scopeName = scope.name,
        value = value,
        packageVerified = null
    )

    private fun missingArgument(key: String): String = AgentError.json(
        AgentErrorCode.INVALID_ARGUMENT,
        "missing or wrong-typed required extra \"$key\". `adb shell content call` needs " +
            "<key>:<type>:<value> bindings, e.g. --extra $key:l:123 for a long or " +
            "--extra $key:s:foo for a string."
    )

    private fun unknownId(message: String): String = AgentError.json(AgentErrorCode.UNKNOWN_ID, message)
}

// Bundle.getLong/getString on a value stored under a different type either throws or silently
// coerces depending on Android version; Bundle.get() sidesteps both by handing back the raw stored
// Object (or null when the key is absent) so a type mismatch is just a failed `as?` cast rather
// than a surprise exception or a wrong value read as if valid. Deliberately top-level rather than
// members of AgentCallHandler: they don't touch any of its state, and keeping them out of the
// class body keeps AgentCallHandler under detekt's TooManyFunctions threshold.
@Suppress("DEPRECATION")
private fun Bundle?.longExtra(key: String): Long? = this?.get(key) as? Long

@Suppress("DEPRECATION")
private fun Bundle?.stringExtra(key: String): String? = this?.get(key) as? String

// Only createConfiguration can provision a new, possibly-unverifiable application row, so this
// note is appended to that endpoint's summary alone; every other AgentMutationResponse leaves
// packageVerified null and carries no such text. Top-level for the same TooManyFunctions reason
// as longExtra/stringExtra above — it doesn't touch AgentCallHandler's state either.
private fun unverifiedPackageNote(packageName: String, packageVerified: Boolean?): String =
    if (packageVerified == false) {
        " (note: $packageName could not be confirmed as installed on this device — possibly a " +
            "typo, possibly Android's package visibility filtering hiding a real package; " +
            "double check the name)"
    } else {
        ""
    }
