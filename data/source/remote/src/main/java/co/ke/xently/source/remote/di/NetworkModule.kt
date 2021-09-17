package co.ke.xently.source.remote.di

import android.content.SharedPreferences
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.retrofit.RequestHeadersInterceptor
import co.ke.xently.common.utils.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.utils.isReleaseBuild
import co.ke.xently.source.remote.BuildConfig
import co.ke.xently.source.remote.JSON_CONVERTER
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.*
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
                val authData = preferences.getString(
                    TOKEN_VALUE_SHARED_PREFERENCE_KEY,
                    BuildConfig.DEFAULT_AUTH_TOKEN,
                )
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