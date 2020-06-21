package com.example.wrench.di

import android.app.Application
import com.example.wrench.SampleApplication
import com.example.wrench.livedataprefs.LiveDataPreferencesFragmentModule
import com.example.wrench.wrenchprefs.WrenchPreferencesFragmentModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidSupportInjectionModule::class,
            ApplicationModule::class,
            LiveDataPreferencesFragmentModule::class,
            WrenchPreferencesFragmentModule::class
        ])
interface ApplicationComponent : AndroidInjector<SampleApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ApplicationComponent
    }
}