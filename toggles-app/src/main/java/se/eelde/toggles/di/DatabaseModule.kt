package se.eelde.toggles.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.migrations.Migrations
import java.io.File
import javax.inject.Singleton

const val LEGACY_DATABASE_NAME = "wrench_database.db"
const val DATABASE_NAME = "toggles_database.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideTogglesDb(@ApplicationContext context: Context): TogglesDatabase {
        val oldName = LEGACY_DATABASE_NAME
        val newName = DATABASE_NAME

        val oldBase = context.getDatabasePath(oldName)
        val newBase = context.getDatabasePath(newName)

        val suffixes = listOf("", "-shm", "-wal")
        val allNewFilesExist = suffixes.all { suffix ->
            File(newBase.absolutePath + suffix).exists()
        }

        if (oldBase.exists() && !allNewFilesExist) {
            suffixes.forEach { suffix ->
                val oldFile = File(oldBase.absolutePath + suffix)
                val newFile = File(newBase.absolutePath + suffix)

                if (oldFile.exists()) {
                    oldFile.copyTo(target = newFile, overwrite = false)
                }
            }
        }

        val db = Room.databaseBuilder(context, TogglesDatabase::class.java, newName)
            .addMigrations(Migrations.MIGRATION_1_2)
            .addMigrations(Migrations.MIGRATION_2_3)
            .addMigrations(Migrations.MIGRATION_3_4)
            .addMigrations(Migrations.MIGRATION_4_5)
            .addMigrations(Migrations.MIGRATION_5_6)
            .addMigrations(Migrations.MIGRATION_6_7)
            .build()

        // OPTIONAL CLEANUP: Only delete after confirming migration worked.
        val cleanupOld = true
        if (cleanupOld) {
            suffixes.forEach { suffix ->
                val oldFile = File(oldBase.absolutePath + suffix)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }
        }

        return db
    }
}
