package co.ke.xently.source.local

import android.content.SharedPreferences
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Preference @Inject constructor(
    @EncryptedSharedPreference
    val encrypted: SharedPreferences,
    @UnencryptedSharedPreference
    val unencrypted: SharedPreferences,
)
