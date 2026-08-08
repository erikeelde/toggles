---
name: toggles-agent
description: Use when inspecting or changing an Android app's feature toggles during a development session with a device connected over adb - reads and writes the Toggles app's agent API to list applications, read toggles and scopes, set values, switch scopes, and create or delete toggles.
---

# Toggles Agent API

The Toggles app exposes a read/write API to adb. It is **self-describing**: this skill hardcodes
only the bootstrap command, and everything else comes from the device — so it cannot go stale
against the installed version.

## Bootstrap

Always start here:

```bash
adb shell content read --uri content://se.eelde.toggles.agentprovider/describe
```

The response lists every read endpoint and every mutation method, each with a runnable example
command, plus the configuration types, the required value format per type, and how scope resolution
works. Follow it rather than guessing.

## Requirements

- The Toggles app (`se.eelde.toggles`) must be installed on the device.
- Callable only from adb (uid 2000) or root. Not reachable from an app.
- An application appears under `/apps` once it has contacted Toggles — or once you pre-create a
  configuration for it.
- The agent API itself is opt-in — see below.

## The API is opt-in

The whole agent API is switched off by default, even on a device where Toggles is installed and
`/describe` responds. `/describe` always works and always reports the current state in its
`enabled` field — that is how you tell "Toggles is installed but the API is off" apart from
"Toggles is not installed." Every other endpoint and every `content call` method returns
`agent_api_disabled` instead of serving data or applying a change while it is off.

It is enabled with the `beta_agent_api` toggle on the `se.eelde.toggles` application's own entry
inside the Toggles app — Toggles dogfoods its own client library for this. That entry appears only
once the Toggles app has been opened at least once.

**If a call returns `agent_api_disabled`, relay that to the user rather than trying to work around
it.** Only the user can flip the toggle inside the Toggles app; there is no way to enable it over
adb (that would defeat the point of an opt-in gate).

## Multiple devices

```bash
adb devices -l
adb -s <serial> shell content read --uri content://se.eelde.toggles.agentprovider/describe
```

## Reading

Reads use `content read` and return raw JSON on stdout — no wrapper — so they pipe straight into a
parser. Piping through something that stops early (`head`, an aborting parser) is safe.

```bash
adb shell content read --uri content://se.eelde.toggles.agentprovider/apps | python3 -m json.tool
adb shell content read --uri "content://se.eelde.toggles.agentprovider/apps/<package>" | python3 -m json.tool
```

## Writing

Mutations use `content call`. **Output is wrapped** — `Result: Bundle[{result=<json>}]` — so strip
the wrapper before parsing.

```bash
adb shell content call --uri content://se.eelde.toggles.agentprovider \
  --method setConfigurationValue \
  --extra configurationId:l:47 --extra scopeId:l:3 --extra value:s:true
```

The `--extra` binding format is `<key>:<type>:<value>`, where `l` is long and `s` is string.
Getting the type letter wrong is the most common mistake; the API reports it as `invalid_argument`
rather than failing silently.

Read `/describe`'s `methods` array for the full list and exact arguments. Available: set a value,
remove an override, create and select scopes, delete a scope, and create and delete configurations.

**Changes reach a running app immediately** — no restart needed. Verified end to end.

## Errors

Failures come back as JSON, not as a non-zero exit code. **Check for an `error` key before
believing a call worked.**

```json
{"error": {"code": "invalid_argument", "message": "..."}}
```

Common codes: `not_authorized`, `agent_api_disabled`, `agent_control_disabled`, `unknown_package`,
`unknown_id`, `unknown_endpoint`, `invalid_argument`, `internal_error`. See "The API is opt-in"
above for `agent_api_disabled` specifically.

If a command prints `No result found.` instead of JSON, the provider was not reached at all — check
the Toggles app is installed and current.

## Getting values right

Values are strings, but they must match the configuration's declared type, and **nothing else in
Toggles enforces this**:

- **boolean** — exactly `"true"` or `"false"`. Anything else is rejected by this API, and would
  otherwise be read by the client as a silent `false` with no error.
- **integer** — a decimal `Int`. A malformed value would otherwise throw inside the consuming app.
- **enum** — must be one of that configuration's `predefinedValues`.

Read `valueFormats` in `/describe` for the authoritative wording.

## Reading a toggle's value correctly

Each configuration carries an `effectiveValue`: the value the app resolves **right now**. Prefer it
over scanning the raw per-scope values.

Resolution consults exactly **two** scopes — the selected one, then the default one.

Two traps:

**`effectiveValue: null` does not mean "no value exists."** It means the app observes no value from
Toggles. A value may exist in a scope that is neither selected nor default — resolution never looks
there, but it is still reported in `values`. Check the `values` array before concluding a toggle is
unset. When there is genuinely no value, the app falls back to the default compiled into its own
source, which this API cannot see.

**`values` contains every scope, `effectiveValue` reflects only two.** Do not infer what the app
sees by scanning `values` yourself.

## Things to be careful about

- **`createConfiguration` accepts any package name.** Android's package visibility filtering means
  Toggles often cannot confirm a package is installed, so it does not try to block you. If the
  package could not be confirmed, the response sets `packageVerified: false` and says so in
  `summary` — treat that as "check your spelling", since a typo creates a real application entry.
  It is recoverable: open that app in the Toggles UI and use Delete in the overflow menu.
- **Creating a scope does not select it.** Those are two separate calls, deliberately.
- **Removing an override is not the same as setting the value to match the default.** An override
  that happens to equal the default still shadows it, so a later change to the default will not
  propagate. Use `deleteConfigurationValue` when the intent is "make this fall back", not
  `setConfigurationValue` with the default's current value.
- **The default scope cannot be deleted.**
- **Deleting the selected scope silently changes what every toggle in that app resolves to.** The
  response reports which scope became selected — relay it.
- **The user can switch you off per application.** If a call returns `agent_control_disabled`, that
  is a deliberate choice made in the Toggles app — say so rather than trying to work around it.
- The first mutation for an application posts a notification so the user can see agent activity.
  Do not treat that as an error.

## Reporting back

State what you changed in terms a human can check: the toggle key, the scope name, and the old and
new values. Every mutation response carries a `summary` written for exactly this — use it. Do not
report a change as made without checking the response for an `error` key first.
