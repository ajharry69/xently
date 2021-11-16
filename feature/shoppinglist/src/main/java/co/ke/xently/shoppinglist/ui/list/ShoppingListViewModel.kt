package co.ke.xently.shoppinglist.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.IRetryViewModel
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
    @ComputationDispatcher
    private val computationDispatcher: CoroutineDispatcher,
) : ViewModel(), IRetryViewModel {
    private val _shoppingListResult = MutableStateFlow<TaskResult<List<ShoppingListItem>>>(
        TaskResult.Loading
    )
    val shoppingListResult: StateFlow<TaskResult<List<ShoppingListItem>>>
        get() = _shoppingListResult

    init {
        viewModelScope.launch {
            var retry = Retry()
            remote.collectLatest { loadRemote ->
                repository.getShoppingList(loadRemote)
                    .flagLoadingOnStartCatchingErrors()
                    .collectLatest {
                        _shoppingListResult.value = it
                        if (loadRemote && it is TaskResult.Error) {
                            // Fallback to cache if remote failed
                            if (it.error !is ConnectException) {
                                viewModelScope.launch(computationDispatcher) {
                                    retry = retry.signalLoadFromCache()
                                }
                            }
                        } else if (!loadRemote && (it is TaskResult.Error || it.getOrNull()
                                .isNullOrEmpty())
                        ) {
                            // Force refresh from remote if cache is empty
                            shouldLoadRemote(true)
                        }
                    }
            }
        }
    }
}