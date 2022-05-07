package co.ke.xently.recommendation.ui

import org.junit.Test

class ShopRecommendationScreenArgsTest {
    @Test(expected = IllegalStateException::class)
    fun `itemId and group property cannot both be null`() {
        ShopRecommendationScreenArgs()
    }
}