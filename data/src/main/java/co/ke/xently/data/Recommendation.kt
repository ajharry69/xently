package co.ke.xently.data

data class Recommendation(
    val shop: Shop,
    val hit: Hit,
    val miss: Miss,
    val expenditure: Expenditure,
) {
    val numberOfItems: Int get() = hit.count + miss.count

    data class Hit(val items: List<String>, val count: Int)
    data class Miss(val items: List<String>, val count: Int)
    data class Expenditure(val unit: Float, val total: Float)
}