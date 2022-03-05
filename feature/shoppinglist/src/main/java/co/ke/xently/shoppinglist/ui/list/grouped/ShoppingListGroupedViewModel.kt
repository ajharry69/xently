package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.AbstractAuthViewModel
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    authRepository: IAuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val repository: IShoppingListRepository,
) : AbstractAuthViewModel(authRepository) {
    private val cacheControlKey = "${ShoppingListGroupedViewModel::class.java.name}.cacheControl"

    private val cacheControl = MutableSharedFlow<CacheControl>()

    val isRefreshing = cacheControl.mapLatest { it is CacheControl.NoCache }.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
    )

    private val groupBy = MutableSharedFlow<GroupBy>()

    val shoppingListResult = combineTransform(
        currentlyActiveUser,
        groupBy,
        cacheControl.stateIn(
            scope = viewModelScope,
            initialValue = savedStateHandle.get<String>(cacheControlKey)?.let { getOrThrow(it) }
                ?: CacheControl.OnlyIfCached,
            started = SharingStarted.WhileSubscribed(),
        ),
    ) { _, by, cacheCtrl ->
        emitAll(
            repository.get(by, cacheCtrl).flagLoadingOnStart().onCompletion {
                // TODO: Fix case where refresh would trigger new network request
                if (it == null) {
                    setCacheControl(CacheControl.OnlyIfCached)
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
        initialValue = TaskResult.Success(emptyList()),
    )

    val shoppingListCount = groupBy.flatMapLatest(repository::getCount).stateIn(
        scope = viewModelScope,
        initialValue = emptyMap(),
        started = DEFAULT_SHARING_STARTED,
    )

    fun refresh() {
        setCacheControl(CacheControl.NoCache)
    }

    fun initFetch(by: GroupBy) {
        viewModelScope.launch {
            this@ShoppingListGroupedViewModel.groupBy.emit(by)
        }
    }

    private fun setCacheControl(control: CacheControl) {
        viewModelScope.launch {
            this@ShoppingListGroupedViewModel.cacheControl.emit(control)
        }
        savedStateHandle[cacheControlKey] = control.toString()
    }
}