package co.ke.xently.recommendation.repository

import co.ke.xently.data.Recommendation
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.recommendation.ui.RecommendationScreenArgs
import co.ke.xently.source.remote.DeferredRecommendation
import kotlinx.coroutines.flow.Flow

interface IRecommendationRepository {
    fun getRecommendation(lookupId: String): Flow<TaskResult<List<Recommendation>>>

    fun getRecommendation(request: RecommendationRequest): Flow<TaskResult<DeferredRecommendation>>

    fun getShoppingListItems(args: RecommendationScreenArgs): Flow<List<ShoppingListItem>>
}