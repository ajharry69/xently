package co.ke.xently.shoppinglist.di

import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shoppinglist.di.qualifiers.LocalShoppingListDataSource
import co.ke.xently.shoppinglist.di.qualifiers.RemoteShoppingListDataSource
import co.ke.xently.shoppinglist.source.IShoppingListDataSource
import co.ke.xently.shoppinglist.source.ShoppingListLocalDataSource
import co.ke.xently.shoppinglist.source.ShoppingListRemoteDataSource
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.remote.services.ShoppingListService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    @RemoteShoppingListDataSource
    fun provideRemoteShoppingListDataSource(
        service: ShoppingListService,
        @IODispatcher
        ioDispatcher: CoroutineDispatcher,
    ): IShoppingListDataSource = ShoppingListRemoteDataSource(service, ioDispatcher)

    @Provides
    @Singleton
    @LocalShoppingListDataSource
    fun provideLocalShoppingListDataSource(dao: ShoppingListDao): IShoppingListDataSource =
        ShoppingListLocalDataSource(dao)
}