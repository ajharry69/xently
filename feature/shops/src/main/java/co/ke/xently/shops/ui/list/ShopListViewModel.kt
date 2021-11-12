package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.data.Shop
import co.ke.xently.feature.IRetryViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
    @ComputationDispatcher
    private val computationDispatcher: CoroutineDispatcher,
) : ViewModel(), IRetryViewModel {
    // interpret `null` as loading...
    private val _shopListResult = MutableStateFlow(Result.success<List<Shop>?>(null))
    val shopListResult: StateFlow<Result<List<Shop>?>>
        get() = _shopListResult

    init {
        viewModelScope.launch {
            var retry = Retry()
            remote.collectLatest { loadRemote ->
                repository.getShopList(loadRemote)
                    .catch { emit(Result.failure(it)) }
                    .collectLatest {
                        _shopListResult.value = it
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