package com.izettle.wrench.dialogs.enumvalue

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class EnumValueFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): EnumValueFragment

    @Binds
    @IntoMap
    @ViewModelKey(FragmentEnumValueViewModel::class)
    abstract fun bindViewModel(viewmodel: FragmentEnumValueViewModel): ViewModel
}