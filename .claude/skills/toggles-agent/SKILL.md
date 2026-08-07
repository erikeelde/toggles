---
name: toggles-agent
description: Use when inspecting an Android app's feature toggles during a development session with a device connected over adb - reads the Toggles app's agent API to list applications, toggles, scopes and the value each toggle currently resolves to.
---

# Toggles Agent API

The Toggles app exposes a read API to adb. It is **self-describing**: this skill hardcodes only the
bootstrap command, and everything else comes from the device — so it cannot go stale against the
installed version.

## Bootstrap

Always start here:

```bash
adb shell content read --uri content://se.eelde.toggles.agentprovider/describe
```

The response lists every endpoint with a runnable example command, the configuration types, the
required value format for each type, and how scope resolution works. Follow it rather than guessing
URIs.

## Requirements

- The Toggles app (`se.eelde.toggles`) must be installed on the device.
- Callable only from adb (uid 2000) or root. It is not reachable from an app.
- An application appears under `/apps` only after it has contacted Toggles at least once.

## Multiple devices

Pass a device selector when more than one is attached:

```bash
adb devices -l
adb -s <serial> shell content read --uri content://se.eelde.toggles.agentprovider/describe
```

## Reading the output

Responses are raw JSON on stdout — no `Row:` prefix, no `Bundle[...]` wrapper — so they pipe
straight into a parser:

```bash
adb shell content read --uri content://se.eelde.toggles.agentprovider/apps | python3 -m json.tool
```

Piping through something that stops early (`head`, a parser that aborts) is safe; the provider
handles the closed pipe.

## Errors

Failures come back as JSON, not as a non-zero exit code. **Check for an `error` key before using a
response.**

```json
{"error": {"code": "unknown_package", "message": "..."}}
```

Common codes:

- `not_authorized` — the call did not come from adb or root.
- `agent_control_disabled` — agent control is switched off for that application in the Toggles app.
- `unknown_package` — Toggles has no record of that package; read `/apps` for the known ones.
- `unknown_endpoint` — bad path or method; read `/describe`.

If a command prints `No result found.` instead of JSON, the provider was not reached at all — check
that the Toggles app is installed and current.

## Reading a toggle's value correctly

This is the part most worth getting right.

Each configuration carries an `effectiveValue`: the value the app resolves **right now**. Prefer it
over reading the raw per-scope values.

Resolution consults exactly **two** scopes — the selected one, then the default one:

```bash
adb shell content read --uri "content://se.eelde.toggles.agentprovider/apps/<package>"
```

```json
{
  "scopes": [
    { "id": 1, "name": "toggles_default",   "selected": false, "default": true  },
    { "id": 2, "name": "Development scope", "selected": true,  "default": false }
  ],
  "configurations": [
    {
      "id": 5, "key": "boolean configuration:", "type": "boolean",
      "effectiveValue": "true",
      "values": [
        { "id": 5, "scopeId": 1, "scopeName": "toggles_default", "value": "true" }
      ],
      "predefinedValues": []
    }
  ]
}
```

Two traps:

**`effectiveValue: null` does not mean "no value exists."** It means the app observes no value from
Toggles. A value may well exist in a scope that is neither selected nor default — resolution never
looks there, but it is still reported in `values`. Check the `values` array before concluding a
toggle is unset. When there is genuinely no value, the app falls back to the default compiled into
its own source, which this API cannot see.

**`values` contains every scope, `effectiveValue` reflects only two.** Do not infer what the app
sees by scanning `values` yourself; use `effectiveValue`.

## Value formats

`/describe` carries a `valueFormats` map. Read it before interpreting or reasoning about a value —
notably, a `boolean` is exactly `"true"` or `"false"`, and the client library parses anything else
as `false` **without erroring**, so a malformed boolean looks like a deliberate `false`.

## Scope of this API

Read-only. It cannot change values, switch scopes, or create toggles. If you need a toggle changed,
tell the user what to change and where — do not claim to have changed it.
