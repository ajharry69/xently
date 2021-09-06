package co.ke.xently.source.remote.di

import android.content.SharedPreferences
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.retrofit.RequestHeadersInterceptor
import co.ke.xently.common.utils.JSON_CONVERTER
import co.ke.xently.common.utils.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.utils.isReleaseBuild
import co.ke.xently.source.remote.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @RequestHeadersInterceptor
    fun provideRequestHeadersInterceptors(@EncryptedSharedPreference preferences: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
                .addHeader("Accept-Language", Locale.getDefault().language)
                .addHeader("Accept", "application/json; version=1.0")
            // Add authorization header iff it wasn't already added by the incoming request
            if (request.header("Authorization") == null) {
                val authData = preferences.getString(TOKEN_VALUE_SHARED_PREFERENCE_KEY, "eyJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTI1NkdDTSIsImVwayI6eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6IkUwdXJvQ05INE96SWtUNlg3d1BHR2pGcHd4a1JYOWFWTG55UkNvcUhVTE0iLCJ5IjoiWFl1UzRwNTluRWtINWQ1ZjBjQ2E5eXIydldfVVR6SXFxMkRvVmZBaXpMRSJ9fQ..g0UHHQPq_dkT09tx.Zu74_fn0G2ubVdbfMMkI9kbrRzNG04GP9VHU4qTomdGy1emfoDNuWFsQ5YSXllwzNWu1n9dtgmRso-9hzwSXqZjSBsV5V-4jxPXvWwy0SrG3ou06cQm6nkx9VqrH65ifyo8ftUjn_XnG74mLUrULoHUdDkyK2BqzhthmoLYokk3Cm1vSrwlntc6kNqTjoUY-ik8tiIZax9SF-emJSMODJqhliFEdC7iMeQLTi1Zh0ajFHQBBChrp4W6Q5dklwAPBtZ2amTNOS7DRblJddxl4QRLwHSonq8m-4ZyL_nIhZVlIpaG6Mtrog6X0MvhFHCo3dLze_XbqN01bNQvIx1rH9-PwpN0b3y4ii6hE19dDiOFb9m6dlmKW8H66xVF7F4LOL3W18I0f7zDHqZtamy5ylioJUUUYVAobG8qBe4FpMN1f95ksGNGH54mWS0oY9py450IZTJWjT75iE1iH-tTdkjnfOTLHf35whMU3gfvIqu5WhOpBGAE4rOVflFfGqdGNvee4PJ9xWrN8J_kJFiuISELfpMMj6ssxyYeIswIVXCsGOIf12GDt94WtXZxmLYKUmmYmzN9oDUrYDfxRWQWs3YzXmBnEfsXavk_1BnWfw5i2fO6Y2XXCG3Umec-AUOwbulHa6Yk6mvhwa2FURhygrX6oqOkv2qNMFxIuZuQIgIjDPFzTi-hw0DYT5CmJbNgUC5DCf8K6ODPONvRaqjS2rnfFuD0PityAlVn53A.2bJwvOpAspNNIi2u2etT8g")
                if (!authData.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $authData")
                }
            }

            return@Interceptor chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = (if (!isReleaseBuild()) {
                HttpLoggingInterceptor.Level.BODY
            } else HttpLoggingInterceptor.Level.NONE)
            redactHeader("Authorization")  // Prevents header content logging
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @RequestHeadersInterceptor headerInterceptor: Interceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(15L, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.SERVER_BASE_URL}/api/")
            .addConverterFactory(GsonConverterFactory.create(JSON_CONVERTER))
            .client(okHttpClient)
            .build()
    }
}