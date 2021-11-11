package co.ke.xently.shoppinglist.di

import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.shoppinglist.repository.ShoppingListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindShoppingListRepository(
        repository: ShoppingListRepository,
    ): IShoppingListRepository
}