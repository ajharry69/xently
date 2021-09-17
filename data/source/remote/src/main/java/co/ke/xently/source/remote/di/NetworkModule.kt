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
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// TODO: Add caching support
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
                val authData = preferences.getString(TOKEN_VALUE_SHARED_PREFERENCE_KEY,
                    "eyJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTI1NkdDTSIsImVwayI6eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6IjRQUk40VFE4MnZNVktIdUs3OEEwTUtzTDlTbzVQVGdrOVZPOFJKeG1KVlkiLCJ5IjoiNWRnTXB2eDNQS01sZzIzYktnM2Zibk1lejJWcXVNZzRrYzdBWmJMTFo1NCJ9fQ..Wb8c8ZzdAY0WwowY.XD0blkh8_9VoEzQCojCMWeDPpNddI4tpnxDxXzr4aacoJR61oS6OewAPpmsLbaZxD3P3x2dX5pPVBomQLuHCnRHw2I57hdCGMnG8jUhC93ybXjr3HGxhDRigWMDBHX5HTd4VQ_veFMfqYGAYRjA0KAOpfITQfsXO07SIy0-foPy0TFQIs1_o4jY7F2THIom_vM5sR926pewZZJu6Yq3b-6-1OdHED7BR8yhywO5WT2XXtzSQUBj_jSuclkuptq8ZQ6Tm5U3MimL3dfXu8fO2xlBSXvLyOCsSAezFp83YcxieSNtNcj0wCiBAFQeAIRNU4xGeCGtEJVtRKw1onwl__Zqs_LRjMmJClIhMzV9Dk84-504TgdTQrG1V37bUDeNUi299QHo_RLkpcmWXIGXmDcF4apgBPjRUhouhokQAULCaP38XUtbIwqNFYlmBiRuTkSu8TdCzsSOpA60vXLstJfU-l8nd6ArL4WTLYJRxlRFiuXx_pW_1N49GSx6Dt6EDw3QjZ8k2PwCtikWqriKOiic_WogRP8T6Ih-KKv7abKQjIPELF6XMQznZLQ9Q-ix34u3_5Y6q-qnhY0Jjcv3oyJ3M11lqNTR0pQFADISG223OMMtHcxGaIboWuBKOk-RgeLCxOsSjBycmOcUKxlCUufrST7iKk9sI2L_fdoaRfPg8Wa5Ay_xp6guQsq4eRvQkFXW09AtVJf40cuNJ4ZuvBfImVWHtePskNjaa8g.uDNXK15Yb30duMR0TnvO8Q")
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