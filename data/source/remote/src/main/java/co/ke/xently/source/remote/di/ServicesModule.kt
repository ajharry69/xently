package co.ke.xently.source.remote.di

import co.ke.xently.source.remote.services.ShoppingListService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {
    @Provides
    fun provideShoppingListService(retrofit: Retrofit): ShoppingListService =
        retrofit.create(ShoppingListService::class.java)
}