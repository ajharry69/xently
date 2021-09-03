package co.ke.xently.source.local.di

import android.content.Context
import androidx.room.Room
import co.ke.xently.source.local.AssistantDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AssistantDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AssistantDatabase::class.java,
            "xently.db",
        ).fallbackToDestructiveMigration().build()
    }
}