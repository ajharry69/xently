package co.ke.xently.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.*

abstract class AbstractListViewModel : ViewModel() {
    protected val defaultSharingStarted = SharingStarted.WhileSubscribed(
        replayExpirationMillis = 5000,
    )
    private val _pagingConfig = MutableStateFlow(PagingConfig(20, enablePlaceholders = false))
    protected val pagingConfig = _pagingConfig.asStateFlow()

    @Suppress("unused")
    fun setPagingConfig(config: PagingConfig) {
        _pagingConfig.value = config
    }

    fun <T : Any> Flow<PagingData<T>>.cachedState() = cachedIn(viewModelScope).stateIn(
        scope = viewModelScope,
        started = defaultSharingStarted,
        initialValue = PagingData.empty()
    )
}