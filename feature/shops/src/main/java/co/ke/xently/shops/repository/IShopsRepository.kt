package co.ke.xently.shops.repository

import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IShopsRepository {
    fun addShop(shop: Shop): Flow<TaskResult<Shop>>
    fun updateShop(shop: Shop): Flow<TaskResult<Shop>>
    fun getShop(id: Long): Flow<TaskResult<Shop>>
    fun getShopList(remote: Boolean): Flow<TaskResult<List<Shop>>>
}