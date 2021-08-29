package co.ke.xently.shoppinglist.di

import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shoppinglist.di.qualifiers.LocalShoppingListDataSource
import co.ke.xently.shoppinglist.di.qualifiers.RemoteShoppingListDataSource
import co.ke.xently.shoppinglist.repository.AbstractShoppingListRepository
import co.ke.xently.shoppinglist.repository.ShoppingListRepository
import co.ke.xently.shoppinglist.source.IShoppingListDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideShoppingListRepository(
        @LocalShoppingListDataSource
        local: IShoppingListDataSource,
        @RemoteShoppingListDataSource
        remote: IShoppingListDataSource,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
    ): AbstractShoppingListRepository = ShoppingListRepository(local, remote, ioDispatcher)
}