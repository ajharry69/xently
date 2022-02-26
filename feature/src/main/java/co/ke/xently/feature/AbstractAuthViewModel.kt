package co.ke.xently.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.source.local.Database
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

abstract class AbstractAuthViewModel(database: Database) : ViewModel() {
    val historicallyFirstUser = database.accountDao.getHistoricallyFirstUser()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000))
}