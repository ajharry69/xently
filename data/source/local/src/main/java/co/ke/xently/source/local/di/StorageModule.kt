package co.ke.xently.source.local.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.ke.xently.common.ENCRYPTED_SHARED_PREFERENCE_KEY
import co.ke.xently.common.UNENCRYPTED_SHARED_PREFERENCE_KEY
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.common.isReleaseBuild
import co.ke.xently.source.local.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    private val TAG: String = StorageModule::class.java.simpleName

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, "xently.db")
            .fallbackToDestructiveMigration()
            .apply {
                if (!isReleaseBuild()) {
                    setQueryCallback(
                        { query, args ->
                            Log.d(TAG, "Query <${query}>. Args: <${args.joinToString()}>")
                        },
                        Executors.newSingleThreadExecutor(),
                    )
                }
            }.build()
    }

    @Provides
    @EncryptedSharedPreference
    @Singleton
    fun provideEncryptedSharedPreference(
        @ApplicationContext context: Context,
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
        @ApplicationContext context: Context,
    ): SharedPreferences =
        context.getSharedPreferences(UNENCRYPTED_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
}