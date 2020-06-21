package com.izettle.wrench.oss.detail

import androidx.lifecycle.ViewModel
import com.izettle.wrench.di.ViewModelBuilder
import com.izettle.wrench.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class OssDetailFragmentModule {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contributesFragment(): OssDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(OssDetailViewModel::class)
    abstract fun bindViewModel(viewmodel: OssDetailViewModel): ViewModel
}