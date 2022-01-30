package co.ke.xently.source.remote.di

import co.ke.xently.source.remote.services.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {
    @Provides
    @Singleton
    fun provideShoppingListService(retrofit: Retrofit): ShoppingListService =
        retrofit.create(ShoppingListService::class.java)

    @Provides
    @Singleton
    fun provideProductService(retrofit: Retrofit): ProductService =
        retrofit.create(ProductService::class.java)

    @Provides
    @Singleton
    fun provideAccountService(retrofit: Retrofit): AccountService =
        retrofit.create(AccountService::class.java)

    @Provides
    @Singleton
    fun provideShopService(retrofit: Retrofit): ShopService =
        retrofit.create(ShopService::class.java)

    @Provides
    @Singleton
    fun provideLocationUpdateService(retrofit: Retrofit): LocationUpdateService =
        retrofit.create(LocationUpdateService::class.java)
}