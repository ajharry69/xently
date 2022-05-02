package co.ke.xently.shops.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IShopsRepository {
    fun add(shop: Shop): Flow<TaskResult<Shop>>
    fun update(shop: Shop): Flow<TaskResult<Shop>>
    fun get(id: Long): Flow<TaskResult<Shop>>
    fun get(config: PagingConfig, query: String): Flow<PagingData<Shop>>
    fun getShopName(shopId: Long): Flow<String?>
}