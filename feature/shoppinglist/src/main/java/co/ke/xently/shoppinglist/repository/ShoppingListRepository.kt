package co.ke.xently.shoppinglist.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import co.ke.xently.common.ENABLE_LOCATION_TRACKING_PREFERENCE_KEY
import co.ke.xently.common.LATITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.LONGITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.GroupBy.DateAdded
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShoppingListRepository @Inject constructor(
    private val dao: ShoppingListDao,
    private val service: ShoppingListService,
    @EncryptedSharedPreference
    private val sharedPreference: SharedPreferences,
    @UnencryptedSharedPreference
    private val unencryptedPreference: SharedPreferences,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShoppingListRepository {
    // TODO: Use memoization to retrieve all grouped shopping list items...
    override fun saveLocationTrackingPref(requestLocationUpdates: Boolean) {
        unencryptedPreference.edit {
            putBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY, requestLocationUpdates)
        }
    }

    override fun getLocationTrackingPref() =
        unencryptedPreference.getBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY, false)

    override fun updateLocation(location: Array<Double>) = Retry().run {
        flow {
            emit(sendRequest(401) { service.updateLocation(location = location) })
        }.onEach {
            sharedPreference.edit(commit = true) {
                putString(LATITUDE_SHARED_PREFERENCE_KEY, location.component1().toString())
                putString(LONGITUDE_SHARED_PREFERENCE_KEY, location.component2().toString())
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun addShoppingListItem(item: ShoppingListItem) = Retry().run {
        flow {
            emit(sendRequest(401) { service.addShoppingListItem(item) })
        }.onEach {
            dao.addShoppingListItems(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getShoppingList(remote: Boolean): Flow<Result<List<ShoppingListItem>>> =
        Retry().run {
            if (remote) {
                flow { emit(sendRequest(401) { service.getShoppingList() }) }
                    .map { result ->
                        result.mapCatching {
                            it.results.apply {
                                dao.addShoppingListItems(*toTypedArray())
                            }
                        }
                    }
                    .retryCatchIfNecessary(this)
                    .flowOn(ioDispatcher)
                    .onCompletion {
                        // Return cached records. Caveats:
                        //  1. No error propagation
                        if (it == null) emitAll(getShoppingList(false))
                    }
            } else {
                dao.getShoppingList().map { shoppingList ->
                    Result.success(shoppingList)
                }
            }
        }

    override fun getGroupedShoppingList(groupBy: GroupBy) = Retry().run {
        flow {
            emit(sendRequest(401) { service.getShoppingList(groupBy = groupBy.name.lowercase()) })
        }.map { result ->
            result.mapCatching {
                it.map { entry ->
                    val shoppingList = entry.value.apply {
                        dao.addShoppingListItems(*toTypedArray())
                    }
                    GroupedShoppingList(group = entry.key, shoppingList = shoppingList)
                }
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getGroupedShoppingListCount(groupBy: GroupBy) = when (groupBy) {
        DateAdded -> dao.getGroupCountByDateAdded()
    }.mapLatest {
        mutableMapOf<Any, Int>().apply {
            for (item in it) put(item.group, item.numberOfItems)
        }.toMap()
    }

    override fun getShoppingListItem(id: Long) = Retry().run {
        dao.getShoppingListItem(id).map { item ->
            if (item == null) {
                sendRequest(401) { service.getShoppingListItem(id) }.apply {
                    getOrNull()?.also {
                        dao.addShoppingListItems(it)
                    }
                }
            } else {
                Result.success(item)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getRecommendations(recommend: Recommend) = Retry().run {
        flow {
            emit(sendRequest(401) {
                when (recommend.from) {
                    Recommend.From.Item -> {
                        val item = if (recommend.by !is ShoppingListItem)
                            dao.getShoppingListItem(recommend.by.toString().toLong())
                                .first()!! else recommend.by
                        service.getRecommendations(
                            RecommendationRequest(listOf(item), recommend.saveBy)
                        )
                    }
                    Recommend.From.ItemList -> {
                        service.getRecommendations(
                            RecommendationRequest(
                                recommend.by as List<ShoppingListItem>,
                                recommend.saveBy
                            )
                        )
                    }
                    Recommend.From.GroupedList -> {
                        service.getRecommendations(
                            recommend.by.toString(),
                            recommend.groupBy.name.lowercase()
                        )
                    }
                }
            })
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}