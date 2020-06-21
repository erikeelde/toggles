package com.example.wrench.wrenchprefs

import androidx.lifecycle.ViewModel
import com.example.wrench.di.ViewModelBuilder
import com.example.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class WrenchPreferencesFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun wrenchPreferenceFragment(): WrenchPreferencesFragment

    @Binds
    @IntoMap
    @ViewModelKey(WrenchPreferencesFragmentViewModel::class)
    abstract fun bindViewModel(viewmodel: WrenchPreferencesFragmentViewModel): ViewModel
}
