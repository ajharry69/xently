package co.ke.xently.data

data class RecommendationRequest(
    val items: List<Any>,
    // Save shopping list items...
    val persist: Boolean = true,
    val lookupId: String? = null,
    val myLocation: Coordinate? = null,
    val isLocationPermissionGranted: Boolean = false,
    val cacheRecommendationsForLater: Boolean = false,
)