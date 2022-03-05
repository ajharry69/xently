package co.ke.xently.shoppinglist.ui.list.recommendation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListRecommendationViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IShoppingListRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    private val recommend = MutableSharedFlow<Recommend>()

    val result = recommend.flatMapLatest {
        repository.get(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun initRecommendation(recommend: Recommend) {
        viewModelScope.launch {
            this@ShoppingListRecommendationViewModel.recommend.emit(recommend)
        }
    }
}