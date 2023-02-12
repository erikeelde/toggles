package se.eelde.toggles.oss

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.BufferedSource
import java.lang.reflect.Type

@JsonClass(generateAdapter = true)
data class SpdxLicenses(val identifier: String, val name: String, val url: String)

@JsonClass(generateAdapter = true)
data class Scm(val url: String)

@JsonClass(generateAdapter = true)
data class UnknownLicenses(val name: String, val url: String)

@JsonClass(generateAdapter = true)
data class Artifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val name: String?,
    val spdxLicenses: List<SpdxLicenses>?,
    val scm: Scm?,
    val unknownLicenses: List<UnknownLicenses>?,
)

interface LicenceParser {
    fun licenceBufferedSource(): BufferedSource

    fun parse(): List<Artifact> {
        val source = licenceBufferedSource()
        val moshi = Moshi.Builder().build()

        val listMyData: Type = Types.newParameterizedType(
            MutableList::class.java,
            Artifact::class.java
        )
        val adapter: JsonAdapter<List<Artifact>> =
            moshi.adapter<List<Artifact>>(listMyData).failOnUnknown()

        val artifacts = adapter.fromJson(source)!!
        return artifacts
    }
}
