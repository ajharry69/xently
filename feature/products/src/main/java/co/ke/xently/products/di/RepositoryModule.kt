package co.ke.xently.products.di

import co.ke.xently.products.repository.IProductsRepository
import co.ke.xently.products.repository.ProductsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindProductsRepository(repository: ProductsRepository): IProductsRepository
}