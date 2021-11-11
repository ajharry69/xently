package co.ke.xently.shops.repository

import co.ke.xently.data.Shop
import co.ke.xently.source.local.daos.ShopsDao
import co.ke.xently.source.remote.services.ShopService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopsRepository @Inject constructor(
    private val dao: ShopsDao,
    private val service: ShopService,
) : IShopsRepository {
    override fun addShop(shop: Shop): Flow<Result<Shop>> {
        TODO("Not yet implemented")
    }

    override fun updateShop(shop: Shop): Flow<Result<Shop>> {
        TODO("Not yet implemented")
    }

    override fun getShop(id: Long): Flow<Result<Shop>> {
        TODO("Not yet implemented")
    }

    override fun getShopList(): Flow<Result<List<Shop>>> {
        TODO("Not yet implemented")
    }
}