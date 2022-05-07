package co.ke.xently.recommendation.repository

import co.ke.xently.common.DEFAULT_SERVER_DATE_FORMAT
import co.ke.xently.common.Retry
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.recommendation.ui.ShopRecommendationScreenArgs
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    override fun getShoppingListItems(args: ShopRecommendationScreenArgs): Flow<List<ShoppingListItem>> {
        return if (args.group != null) {
            val date = DEFAULT_SERVER_DATE_FORMAT.parse(args.group.group.toString())!!
            dependencies.database.shoppingListDao.getList(date)
        } else {
            dependencies.database.shoppingListDao.getList(args.itemId!!)
        }.map { shoppingList ->
            shoppingList.map {
                it.item
            }
        }
    }
}