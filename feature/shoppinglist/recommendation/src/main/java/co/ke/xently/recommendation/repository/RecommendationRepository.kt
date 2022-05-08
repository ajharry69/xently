package co.ke.xently.recommendation.repository

import co.ke.xently.common.DEFAULT_SERVER_DATE_FORMAT
import co.ke.xently.common.Retry
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.recommendation.ui.RecommendationScreenArgs
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RecommendationRepository @Inject constructor(
    private val dependencies: Dependencies
) : IRecommendationRepository {
    override fun getRecommendation(lookupId: String) = Retry().run {
        flow {
            val result = sendRequest {
                dependencies.service.shoppingList.getRecommendations(lookupId)
            }
            emit(result)
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getRecommendation(request: RecommendationRequest) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.shoppingList.getRecommendations(request) })
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getShoppingListItems(args: RecommendationScreenArgs): Flow<List<ShoppingListItem>> {
        return when {
            args.group != null -> {
                val date = DEFAULT_SERVER_DATE_FORMAT.parse(args.group.group.toString())!!
                dependencies.database.shoppingListDao.getList(date).map { shoppingList ->
                    shoppingList.map {
                        it.item
                    }
                }
            }
            args.itemId != null -> {
                dependencies.database.shoppingListDao.getList(args.itemId).map { shoppingList ->
                    shoppingList.map {
                        it.item
                    }
                }
            }
            else -> {
                flowOf(emptyList())
            }
        }
    }
}