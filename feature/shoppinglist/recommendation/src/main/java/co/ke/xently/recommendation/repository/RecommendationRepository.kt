package co.ke.xently.recommendation.repository

import co.ke.xently.common.Retry
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.recommendation.Recommend
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RecommendationRepository @Inject constructor(
    private val dependencies: Dependencies
) : IRecommendationRepository {
    override fun get(recommend: Recommend) = Retry().run {
        flow {
            emit(sendRequest {
                when (recommend.from) {
                    Recommend.From.Item -> {
                        val item = if (recommend.by !is ShoppingListItem) {
                            dependencies.database.shoppingListDao.get(
                                recommend.by.toString().toLong()
                            ).first()!!.item
                        } else {
                            recommend.by
                        }
                        dependencies.service.shoppingList.getRecommendations(
                            RecommendationRequest(listOf(item), recommend.saveBy)
                        )
                    }
                    Recommend.From.ItemList -> {
                        @Suppress("UNCHECKED_CAST")
                        dependencies.service.shoppingList.getRecommendations(
                            RecommendationRequest(
                                recommend.by as List<Any>,
                                recommend.saveBy
                            )
                        )
                    }
                    Recommend.From.GroupedList -> {
                        dependencies.service.shoppingList.getRecommendations(
                            recommend.by.toString(),
                            recommend.groupBy.name.lowercase(),
                        )
                    }
                }
            })
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}