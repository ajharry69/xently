package co.ke.xently.feature.di

import co.ke.xently.feature.repository.AuthRepository
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.repository.ILocationServiceRepository
import co.ke.xently.feature.repository.LocationServiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(repository: AuthRepository): IAuthRepository

    @Binds
    abstract fun bindLocationServiceRepository(repository: LocationServiceRepository): ILocationServiceRepository
}