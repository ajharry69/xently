package co.ke.xently.source.remote.services

import co.ke.xently.data.Attribute
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AttributeService {
    @GET("attributes/")
    suspend fun get(
        @Query("q")
        query: String = "",
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        filters: Map<String, String> = mapOf(),
    ): Response<PagedData<Attribute>>
}