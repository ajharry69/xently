package co.ke.xently.recommendation.ui

import androidx.lifecycle.ViewModel
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.recommendation.repository.IRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ShopRecommendationViewModel @Inject constructor(
    private val repository: IRecommendationRepository
) : ViewModel() {
    fun recommend(request: RecommendationRequest) {

    }
}