package co.ke.xently.shops.repository

import co.ke.xently.data.Shop
import kotlinx.coroutines.flow.Flow

interface IShopsRepository {
    fun addShop(shop: Shop): Flow<Result<Shop>>
    fun updateShop(shop: Shop): Flow<Result<Shop>>
    fun getShop(id: Long): Flow<Result<Shop>>
    fun getShopList(): Flow<Result<List<Shop>>>
}