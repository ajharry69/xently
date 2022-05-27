package co.ke.xently.recommendation.ui.list

import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.recommendation.repository.IRecommendationRepository
import co.ke.xently.recommendation.ui.MyUpdatedLocationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecommendationListViewModel @Inject constructor(
    private val repository: IRecommendationRepository,
) : MyUpdatedLocationViewModel() {
    private val lookupId = MutableSharedFlow<String>()

    val result = lookupId.flatMapLatest {
        repository.getRecommendation(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED, replay = 1)

    fun recommend(lookupId: String) {
        viewModelScope.launch {
            this@RecommendationListViewModel.lookupId.emit(lookupId)
        }
    }
}