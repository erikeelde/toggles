package com.izettle.wrench.dialogs.stringvalue

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class StringValueFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): StringValueFragment

    @Binds
    @IntoMap
    @ViewModelKey(FragmentStringValueViewModel::class)
    abstract fun bindViewModel(viewmodel: FragmentStringValueViewModel): ViewModel
}