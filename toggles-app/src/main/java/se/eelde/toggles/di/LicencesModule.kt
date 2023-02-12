package se.eelde.toggles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.buffer
import okio.source
import se.eelde.toggles.oss.LicenceParser

@Module
@InstallIn(SingletonComponent::class)
object LicencesModule {
    @Provides
    fun providesLicenceParser(@ApplicationContext context: Context): LicenceParser =
        LicenceParserImpl(context)
}

class LicenceParserImpl(val context: Context) : LicenceParser {
    override fun licenceBufferedSource() =
        context.assets.open("artifacts.json").source().buffer()
}
