package co.ke.xently.shoppinglist.di

import android.content.SharedPreferences
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.shoppinglist.repository.ShoppingListRepository
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
internal object RepositoryModule {
    @Provides
    @Singleton
    fun provideShoppingListRepository(
        dao: ShoppingListDao,
        service: ShoppingListService,
        @EncryptedSharedPreference
        sharedPreference: SharedPreferences,
        @UnencryptedSharedPreference
        unencryptedPreference: SharedPreferences,
        @IODispatcher
        ioDispatcher: CoroutineDispatcher,
    ): IShoppingListRepository = ShoppingListRepository(dao, service, sharedPreference, unencryptedPreference, ioDispatcher)
}