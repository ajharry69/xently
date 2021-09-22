package co.ke.xently.source.local.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.common.utils.ENCRYPTED_SHARED_PREFERENCE_KEY
import co.ke.xently.common.utils.UNENCRYPTED_SHARED_PREFERENCE_KEY
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

    @Provides
    @EncryptedSharedPreference
    @Singleton
    fun provideEncryptedSharedPreference(
        @ApplicationContext context: Context
    ): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_SHARED_PREFERENCE_KEY,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    @Provides
    @UnencryptedSharedPreference
    @Singleton
    fun provideUnencryptedSharedPreference(
        @ApplicationContext context: Context
    ): SharedPreferences =
        context.getSharedPreferences(UNENCRYPTED_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
}