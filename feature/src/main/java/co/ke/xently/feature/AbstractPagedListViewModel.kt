package co.ke.xently.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

abstract class AbstractPagedListViewModel : ViewModel() {
    private val _pagingConfig =
        MutableStateFlow(PagingConfig(pageSize = 30, enablePlaceholders = true))
    protected val pagingConfig = _pagingConfig.asStateFlow()

    @Suppress("unused")
    fun setPagingConfig(config: PagingConfig) {
        _pagingConfig.value = config
    }

    fun <T : Any> Flow<PagingData<T>>.cachedState() = cachedIn(viewModelScope).stateIn(
        scope = viewModelScope,
        initialValue = PagingData.empty(),
        started = DEFAULT_SHARING_STARTED,
    )
}