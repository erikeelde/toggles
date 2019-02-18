package com.izettle.wrench.oss.list

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class OssFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): OssFragment

    @Binds
    @IntoMap
    @ViewModelKey(OssListViewModel::class)
    abstract fun bindViewModel(viewmodel: OssListViewModel): ViewModel
}