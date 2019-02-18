package com.izettle.wrench.applicationlist

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class ApplicationsFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): ApplicationsFragment

    @Binds
    @IntoMap
    @ViewModelKey(ApplicationViewModel::class)
    abstract fun bindViewModel(viewmodel: ApplicationViewModel): ViewModel
}