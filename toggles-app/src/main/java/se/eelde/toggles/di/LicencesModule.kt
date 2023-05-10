package se.eelde.toggles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.oss.Artifact
import se.eelde.toggles.oss.LicenceProvider
import se.eelde.toggles.oss.Scm
import se.eelde.toggles.oss.SpdxLicenses
import se.eelde.toggles.oss.UnknownLicenses
import se.premex.gross.Gross

@Module
@InstallIn(SingletonComponent::class)
object LicencesModule {
    @Provides
    fun providesLicenceParser(): LicenceProvider = LicenceParserImpl()
}

class LicenceParserImpl : LicenceProvider {
    override fun licences(): List<Artifact> =
        Gross.artifacts.map { artifact ->
            Artifact(
                groupId = artifact.groupId,
                artifactId = artifact.artifactId,
                version = artifact.version,
                name = artifact.name,
                spdxLicenses = artifact.spdxLicenses.map { spdxLicenses ->
                    SpdxLicenses(
                        identifier = spdxLicenses.identifier,
                        name = spdxLicenses.name,
                        url = spdxLicenses.url
                    )
                },
                scm = if (artifact.scm != null) {
                    Scm(url = artifact.scm.url)
                } else {
                    null
                },
                unknownLicenses = artifact.unknownLicenses.map { unknownLicenses ->
                    UnknownLicenses(
                        name = unknownLicenses.name,
                        url = unknownLicenses.url
                    )
                }
            )
        }
}
