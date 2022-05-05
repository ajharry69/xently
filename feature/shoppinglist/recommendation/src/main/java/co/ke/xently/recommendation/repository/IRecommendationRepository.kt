package co.ke.xently.recommendation.repository

import co.ke.xently.data.Recommendation
import co.ke.xently.data.TaskResult
import co.ke.xently.recommendation.Recommend
import kotlinx.coroutines.flow.Flow

interface IRecommendationRepository {
    fun get(recommend: Recommend): Flow<TaskResult<List<Recommendation>>>
}