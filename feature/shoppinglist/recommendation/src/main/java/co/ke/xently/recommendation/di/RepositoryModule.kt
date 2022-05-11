package co.ke.xently.recommendation.di

import co.ke.xently.recommendation.repository.IRecommendationRepository
import co.ke.xently.recommendation.repository.RecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRecommendationRepository(repository: RecommendationRepository): IRecommendationRepository
}