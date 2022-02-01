package co.ke.xently.source.remote.di

import android.content.Context
import android.content.SharedPreferences
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.isReleaseBuild
import co.ke.xently.source.remote.BuildConfig
import co.ke.xently.source.remote.JSON_CONVERTER
import co.ke.xently.source.remote.di.qualifiers.CacheInterceptor
import co.ke.xently.source.remote.di.qualifiers.RequestHeadersInterceptor
import co.ke.xently.source.remote.di.qualifiers.RequestQueriesInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CacheControl
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
    @RequestQueriesInterceptor
    fun provideRequestQueriesInterceptors(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            return@Interceptor chain.proceed(
                request.newBuilder().apply {
                    val url = build().url
                    if (url.queryParameter("relatedAsId") == null) {
                        // Return IDs of related objects instead of returning a browsable link. This
                        // helps reduce the size of response payload and also makes it easier to
                        // cache object relationships.
                        url(url.newBuilder().addQueryParameter("relatedAsId", "true").build())
                    }
                }.build(),
            )
        }
    }

    @Provides
    @Singleton
    @RequestHeadersInterceptor
    fun provideRequestHeadersInterceptors(@EncryptedSharedPreference preferences: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            return@Interceptor chain.proceed(
                request.newBuilder().apply {
                    // Add the following headers iff they weren't already added by the
                    // incoming request

                    if (request.header("Accept-Language") == null) {
                        addHeader("Accept-Language", Locale.getDefault().language)
                    }

                    if (request.header("Accept") == null) {
                        val version = if (BuildConfig.API_VERSION.isNotBlank()) {
                            "; version=${BuildConfig.API_VERSION}"
                        } else ""
                        addHeader("Accept", "application/json${version}")
                    }

                    if (!isReleaseBuild() && request.header("Authorization") == null) {
                        preferences.getString(
                            TOKEN_VALUE_SHARED_PREFERENCE_KEY,
                            BuildConfig.API_DEFAULT_AUTH_TOKEN,
                        )?.also { addHeader("Authorization", "Bearer $it") }
                    }
                }.build(),
            )
        }
    }

    @Provides
    @Singleton
    @CacheInterceptor
    fun provideCacheInterceptors(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(
                request.newBuilder()
                    .cacheControl(CacheControl.parse(request.headers)).build()
            )
            if (response.code == 504 && response.request.cacheControl.onlyIfCached) {
                // See, https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control#other
                response = chain.proceed(
                    response.request.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK).build()
                )
            }
            return@Interceptor response
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
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        @CacheInterceptor cacheInterceptor: Interceptor,
        @RequestHeadersInterceptor headerInterceptor: Interceptor,
        @RequestQueriesInterceptor queriesInterceptor: Interceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, (5 * 1024 * 1024).toLong()))
            .addInterceptor(headerInterceptor)
            .addInterceptor(queriesInterceptor)
            .addInterceptor(cacheInterceptor) // maintain order - cache may depend on the headers
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
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(JSON_CONVERTER))
            .client(okHttpClient)
            .build()
    }
}