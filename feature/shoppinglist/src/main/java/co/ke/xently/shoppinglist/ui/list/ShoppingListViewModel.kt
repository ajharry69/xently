package co.ke.xently.shoppinglist.ui.list

import androidx.lifecycle.viewModelScope
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
    @ComputationDispatcher
    private val computationDispatcher: CoroutineDispatcher,
) : AbstractShoppingListViewModel() {
    // interpret `null` as loading...
    private val _shoppingListResult = MutableStateFlow(success<List<ShoppingListItem>?>(null))
    val shoppingListResult: StateFlow<Result<List<ShoppingListItem>?>>
        get() = _shoppingListResult

    init {
        viewModelScope.launch {
            var retry = Retry()
            remote.collectLatest { loadRemote ->
                groupBy.collectLatest { group ->
                    repository.getShoppingList(group, loadRemote).catch { emit(failure(it)) }
                        .collectLatest {
                            _shoppingListResult.value = it
                            if (loadRemote && it.isFailure) {
                                // Fallback to cache if remote failed
                                if (it.exceptionOrNull() !is ConnectException) {
                                    viewModelScope.launch(computationDispatcher) {
                                        retry = retry.signalLoadFromCache()
                                    }
                                }
                            } else if (!loadRemote && (it.isFailure || it.getOrNull()
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
}