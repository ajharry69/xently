package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.viewmodels.AbstractAuthViewModel
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    authRepository: IAuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val repository: IShoppingListRepository,
) : AbstractAuthViewModel(authRepository) {
    private val cacheControlKey = "${ShoppingListGroupedViewModel::class.java.name}.cacheControl"

    private val _cacheControl =
        MutableStateFlow(savedStateHandle.get<String>(cacheControlKey)?.let { getOrThrow(it) }
            ?: CacheControl.OnlyIfCached)
    private val cacheControl = _cacheControl.asStateFlow()
    val isRefreshing = cacheControl.mapLatest { it is CacheControl.NoCache }.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
    )

    private val groupBy = MutableStateFlow(GroupBy.DateAdded)

    val shoppingListResult =
        combineTransform(currentlyActiveUser, groupBy, cacheControl) { _, by, cacheCtrl ->
            emitAll(
                repository.get(by, cacheCtrl).flagLoadingOnStart().onCompletion {
                    // TODO: Fix case where refresh would trigger new network request
                    setCacheControl(CacheControl.OnlyIfCached)
                },
            )
        }.stateIn(
            scope = viewModelScope,
            started = DEFAULT_SHARING_STARTED,
            initialValue = TaskResult.Loading,
        )

    val shoppingListCount = groupBy.flatMapLatest(repository::getCount).stateIn(
        scope = viewModelScope,
        initialValue = emptyMap(),
        started = DEFAULT_SHARING_STARTED,
    )

    fun refresh() {
        setCacheControl(CacheControl.NoCache)
    }

    private fun setCacheControl(v: CacheControl) {
        _cacheControl.value = v
        savedStateHandle[cacheControlKey] = v.toString()
    }
}