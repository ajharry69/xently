package co.ke.xently.shoppinglist.ui.list.recommendation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.LocationPermissionViewModel
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListRecommendationViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IShoppingListRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    /*private val _recommendationReportResult =
        MutableStateFlow<TaskResult<RecommendationReport>>(TaskResult.Loading)*/
    var recommendationReportResult: StateFlow<TaskResult<RecommendationReport>>
        private set

    private val recommend = MutableStateFlow(Recommend())

    init {
        recommendationReportResult = combineTransform(recommend) {
            emitAll(repository.get(it[0])
                .flagLoadingOnStart())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 5000), TaskResult.Loading)
        /*viewModelScope.launch {
            recommend.collectLatest { r ->
                repository.get(r)
                    .flagLoadingOnStartCatchingErrors()
                    .collectLatest { _recommendationReportResult.value = it }
            }
        }*/
    }

    fun setRecommend(recommend: Recommend) {
        this.recommend.value = recommend
    }
}