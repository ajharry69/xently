package co.ke.xently.source.local.di

import co.ke.xently.source.local.AssistantDatabase
import co.ke.xently.source.local.daos.ProductsDao
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.local.daos.ShopsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DAOsModule {
    @Provides
    @Singleton
    fun provideShopsDao(db: AssistantDatabase): ShopsDao = db.shopsDao

    @Provides
    @Singleton
    fun provideProductsDao(db: AssistantDatabase): ProductsDao = db.productsDao

    @Provides
    @Singleton
    fun provideShoppingListItemDao(db: AssistantDatabase): ShoppingListDao = db.shoppingListDao
}