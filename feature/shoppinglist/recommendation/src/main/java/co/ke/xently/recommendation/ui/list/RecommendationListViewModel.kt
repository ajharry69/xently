package co.ke.xently.recommendation.ui.list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.recommendation.repository.IRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecommendationListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IRecommendationRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    private val lookupId = MutableSharedFlow<String>()

    val result = lookupId.flatMapLatest {
        repository.getRecommendation(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun recommend(lookupId: String) {
        viewModelScope.launch {
            this@RecommendationListViewModel.lookupId.emit(lookupId)
        }
    }
}