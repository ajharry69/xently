package co.ke.xently.source.remote

sealed class CacheControl(private val name: String) {
    override fun toString(): String {
        return name
    }

    object NoCache : CacheControl("no-cache")

    object OnlyIfCached : CacheControl("only-if-cached")
}

val Default = CacheControl.OnlyIfCached

fun getOrThrow(lookup: String): CacheControl {
    return when (lookup) {
        CacheControl.NoCache.toString() -> {
            CacheControl.NoCache
        }
        CacheControl.OnlyIfCached.toString() -> {
            CacheControl.OnlyIfCached
        }
        else -> {
            throw NotImplementedError()
        }
    }
}
