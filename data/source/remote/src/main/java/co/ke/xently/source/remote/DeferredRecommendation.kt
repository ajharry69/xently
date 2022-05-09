package co.ke.xently.source.remote

data class DeferredRecommendation(val id: String, val numberOfItems: Int = 0) {
    override fun toString(): String {
        return id
    }
}
