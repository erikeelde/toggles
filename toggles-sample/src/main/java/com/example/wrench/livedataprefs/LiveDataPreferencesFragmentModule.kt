package com.example.wrench.livedataprefs

import androidx.lifecycle.ViewModel
import com.example.wrench.di.ViewModelBuilder
import com.example.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class LiveDataPreferencesFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun liveDataPreferenceFragment(): LiveDataPreferencesFragment

    @Binds
    @IntoMap
    @ViewModelKey(LiveDataPreferencesFragmentViewModel::class)
    abstract fun bindViewModel(viewmodel: LiveDataPreferencesFragmentViewModel): ViewModel
}
