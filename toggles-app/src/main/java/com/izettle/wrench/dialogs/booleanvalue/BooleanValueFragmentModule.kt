package com.izettle.wrench.dialogs.booleanvalue

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class BooleanValueFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): BooleanValueFragment

    @Binds
    @IntoMap
    @ViewModelKey(FragmentBooleanValueViewModel::class)
    abstract fun bindViewModel(viewmodel: FragmentBooleanValueViewModel): ViewModel
}