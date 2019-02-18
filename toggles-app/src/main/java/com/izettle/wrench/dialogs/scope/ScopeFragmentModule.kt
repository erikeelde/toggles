package com.izettle.wrench.dialogs.scope

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ScopeFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): ScopeFragment

    @Binds
    @IntoMap
    @ViewModelKey(ScopeFragmentViewModel::class)
    abstract fun bindViewModel(viewmodel: ScopeFragmentViewModel): ViewModel
}