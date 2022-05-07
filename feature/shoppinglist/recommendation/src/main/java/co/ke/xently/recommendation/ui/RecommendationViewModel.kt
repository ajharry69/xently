package co.ke.xently.recommendation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.recommendation.repository.IRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecommendationViewModel @Inject constructor(
    private val repository: IRecommendationRepository
) : ViewModel() {
    private val args = MutableSharedFlow<ShopRecommendationScreenArgs>(replay = 1)
    val persistedShoppingListResult: Flow<TaskResult<List<ShoppingListItem>>> =
        args.flatMapLatest(repository::getShoppingListItems)
            .map<List<ShoppingListItem>, TaskResult<List<ShoppingListItem>>> {
                TaskResult.Success(it)
            }.catch {
                emit(TaskResult.Error(it))
            }.flagLoadingOnStart().shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    private val request = MutableSharedFlow<RecommendationRequest>()

    val result = request.flatMapLatest {
        repository.getRecommendation(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun recommend(request: RecommendationRequest) {
        viewModelScope.launch {
            this@RecommendationViewModel.request.emit(request)
        }
    }

    suspend fun setArgs(args: ShopRecommendationScreenArgs) {
        viewModelScope.launch {
            this@RecommendationViewModel.args.emit(args)
        }
    }
}