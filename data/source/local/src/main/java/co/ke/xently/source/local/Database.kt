package co.ke.xently.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.data.*
import co.ke.xently.source.local.daos.*

@Database(
    entities = [
        Shop::class,
        User::class,
        Product::class,
        RemoteKey::class,
        Product.Brand::class,
        MeasurementUnit::class,
        ShoppingListItem::class,
        Product.Attribute::class,
        ShoppingListItem.Brand::class,
        ShoppingListItem.Attribute::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    RoomTypeConverters.UriConverter::class,
    RoomTypeConverters.DateConverter::class,
    RoomTypeConverters.LocationConverter::class,
)
abstract class Database : RoomDatabase() {
    abstract val remoteKeyDao: RemoteKeyDao
    abstract val accountDao: AccountDao
    abstract val shopDao: ShopDao
    abstract val brandDao: BrandDao
    abstract val attributeDao: AttributeDao
    abstract val productDao: ProductDao
    abstract val shoppingListDao: ShoppingListDao
    abstract val measurementUnitDao: MeasurementUnitDao
}