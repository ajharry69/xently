package co.ke.xently.shops.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IShopsRepository {
    fun add(shop: Shop): Flow<TaskResult<Shop>>
    fun update(shop: Shop): Flow<TaskResult<Shop>>
    fun get(id: Long): Flow<TaskResult<Shop>>
    fun get(config: PagingConfig): Pager<Int, Shop>
}