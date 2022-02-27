package co.ke.xently.shops.ui.list

import co.ke.xently.feature.AbstractPagedListViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : AbstractPagedListViewModel() {
    val pagingData = pagingConfig.flatMapLatest {
        repository.get(it, "")
    }.cachedState()
}