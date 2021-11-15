package co.ke.xently.shoppinglist.ui.list.recommendation

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.RecommendationReport
import co.ke.xently.feature.LocationPermissionViewModel
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListRecommendationViewModel @Inject constructor(
    app: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: IShoppingListRepository,
) : LocationPermissionViewModel(app, savedStateHandle) {
    private val _recommendationReportResult =
        MutableStateFlow(Result.success<RecommendationReport?>(null))
    val recommendationReportResult: StateFlow<Result<RecommendationReport?>>
        get() = _recommendationReportResult
    private val recommend = MutableStateFlow(Recommend())

    init {
        viewModelScope.launch {
            recommend.collectLatest { r ->
                repository.getRecommendations(r)
                    .catch { emit(Result.failure(it)) }
                    .collectLatest { _recommendationReportResult.value = it }
            }
        }
    }

    fun setRecommend(recommend: Recommend) {
        this.recommend.value = recommend
    }
}