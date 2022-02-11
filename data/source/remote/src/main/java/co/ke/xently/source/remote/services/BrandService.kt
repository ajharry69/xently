package co.ke.xently.source.remote.services

import co.ke.xently.data.Brand
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BrandService {
    @GET("brands/")
    suspend fun get(
        @Query("q")
        query: String = "",
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Brand>>
}