package co.ke.xently.common.di.modules

import android.os.Looper
import co.ke.xently.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.common.di.qualifiers.coroutines.UIDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @ComputationDispatcher
    fun provideComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @UIDispatcher
    fun provideUIDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    fun provideIOLooper(): Looper? = Looper.myLooper()
}