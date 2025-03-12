package su.femboymatrix.buttplug.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import su.femboymatrix.buttplug.data.AppDatabase
import su.femboymatrix.buttplug.data.ChatDao
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .build()
    }

    @Provides
    @Singleton
    fun providesConsoleDao(@ApplicationContext context: Context): ChatDao {
        return providesDatabase(context).chatDao()
    }
}
