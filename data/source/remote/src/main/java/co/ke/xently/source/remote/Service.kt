package co.ke.xently.source.remote

import co.ke.xently.source.remote.services.*
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Service @Inject constructor(val retrofit: Retrofit) {
    val shoppingList: ShoppingListService = retrofit.create(ShoppingListService::class.java)
    val product: ProductService = retrofit.create(ProductService::class.java)
    val attribute: AttributeService = retrofit.create(AttributeService::class.java)
    val brand: BrandService = retrofit.create(BrandService::class.java)
    val account: AccountService = retrofit.create(AccountService::class.java)
    val shop: ShopService = retrofit.create(ShopService::class.java)
    val measurementUnit: MeasurementUnitService =
        retrofit.create(MeasurementUnitService::class.java)
}
