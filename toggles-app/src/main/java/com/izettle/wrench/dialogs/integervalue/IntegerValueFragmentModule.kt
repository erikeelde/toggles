package com.izettle.wrench.dialogs.integervalue

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class IntegerValueFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): IntegerValueFragment

    @Binds
    @IntoMap
    @ViewModelKey(FragmentIntegerValueViewModel::class)
    abstract fun bindViewModel(viewmodel: FragmentIntegerValueViewModel): ViewModel
}