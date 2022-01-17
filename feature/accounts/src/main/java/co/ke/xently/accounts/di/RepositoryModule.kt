package co.ke.xently.accounts.di

import co.ke.xently.accounts.repository.AccountRepository
import co.ke.xently.accounts.repository.IAccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindsRepository(repository: AccountRepository): IAccountRepository
}