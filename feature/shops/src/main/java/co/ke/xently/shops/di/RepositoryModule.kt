package co.ke.xently.shops.di

import co.ke.xently.shops.repository.IShopsRepository
import co.ke.xently.shops.repository.ShopsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindShopsRepository(repository: ShopsRepository): IShopsRepository
}