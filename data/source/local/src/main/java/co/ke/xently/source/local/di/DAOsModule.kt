package co.ke.xently.source.local.di

import co.ke.xently.source.local.AssistantDatabase
import co.ke.xently.source.local.daos.ShopDao
import co.ke.xently.source.local.daos.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DAOsModule {
    @Provides
    fun provideShopDao(db: AssistantDatabase): ShopDao = db.shopDao

    @Provides
    fun provideShoppingListItemDao(db: AssistantDatabase): ShoppingListDao = db.shoppingListDao
}