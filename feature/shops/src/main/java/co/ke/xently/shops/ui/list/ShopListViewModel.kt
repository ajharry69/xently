package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import co.ke.xently.feature.IRetryViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShopListViewModel @Inject constructor(private val repository: IShopsRepository) : ViewModel(),
    IRetryViewModel