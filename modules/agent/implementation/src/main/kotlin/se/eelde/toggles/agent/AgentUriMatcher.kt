package se.eelde.toggles.agent

import android.content.UriMatcher
import android.net.Uri

enum class AgentUriMatch {
    DESCRIBE,
    APPLICATIONS,
    APPLICATION,
    UNKNOWN,
}

class AgentUriMatcher(agentAuthority: String) {
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    fun match(uri: Uri): AgentUriMatch {
        val match = uriMatcher.match(uri)
        return if (match == -1) AgentUriMatch.UNKNOWN else AgentUriMatch.entries[match]
    }

    init {
        uriMatcher.addURI(agentAuthority, "describe", AgentUriMatch.DESCRIBE.ordinal)
        uriMatcher.addURI(agentAuthority, "apps", AgentUriMatch.APPLICATIONS.ordinal)
        uriMatcher.addURI(agentAuthority, "apps/*", AgentUriMatch.APPLICATION.ordinal)
    }
}
