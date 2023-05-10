package se.eelde.toggles.oss

data class SpdxLicenses(val identifier: String, val name: String, val url: String)

data class Scm(val url: String)

data class UnknownLicenses(val name: String, val url: String)

data class Artifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val name: String?,
    val spdxLicenses: List<SpdxLicenses>?,
    val scm: Scm?,
    val unknownLicenses: List<UnknownLicenses>?,
)

interface LicenceProvider {
    fun licences(): List<Artifact>
}
