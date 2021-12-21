package co.ke.xently.source.remote

import co.ke.xently.common.Exclude
import co.ke.xently.data.RemoteKey
import java.net.URI

data class PagedData<T>(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    @Exclude(during = Exclude.During.SERIALIZATION)
    val results: List<T> = emptyList(),
) {
    fun toRemoteKey(defaultEndpoint: String = ""): RemoteKey {
        fun getLatestPage(query: String?): Int? {
            val map: MutableMap<String, Set<String?>> = HashMap()
            for (q in (query ?: "").split("&")) {
                val queryWithValue = q.split("=")
                map[queryWithValue[0]] =
                    map.getOrElse(queryWithValue[0]) { setOf() } + setOf(if (queryWithValue.size > 1) queryWithValue[1] else null)
            }

            return map.toMap()["page"]?.maxByOrNull { it ?: "" }?.toIntOrNull()
        }

        val nUri = URI.create(next ?: "")
        val pUri = URI.create(previous ?: "")
        val endpoint = when {
            nUri.path.isNotBlank() -> nUri.path
            pUri.path.isNotBlank() -> pUri.path
            else -> defaultEndpoint
        }
        return RemoteKey(endpoint, getLatestPage(pUri.query), getLatestPage(nUri.query), count)
    }
}