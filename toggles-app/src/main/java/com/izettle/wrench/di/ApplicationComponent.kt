package com.izettle.wrench.di

import android.app.Application
import com.izettle.wrench.applicationlist.ApplicationsFragmentModule
import com.izettle.wrench.configurationlist.ConfigurationsFragmentModule
import com.izettle.wrench.dialogs.booleanvalue.BooleanValueFragmentModule
import com.izettle.wrench.dialogs.enumvalue.EnumValueFragmentModule
import com.izettle.wrench.dialogs.integervalue.IntegerValueFragmentModule
import com.izettle.wrench.dialogs.scope.ScopeFragmentModule
import com.izettle.wrench.dialogs.stringvalue.StringValueFragmentModule
import com.izettle.wrench.oss.detail.OssDetailFragmentModule
import com.izettle.wrench.oss.list.OssFragmentModule
import com.izettle.wrench.provider.WrenchProviderModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import se.eelde.toggles.TogglesApplication
import se.eelde.toggles.provider.TogglesProviderModule
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            ApplicationModule::class,
            AndroidSupportInjectionModule::class,
            ApplicationsFragmentModule::class,
            ConfigurationsFragmentModule::class,
            BooleanValueFragmentModule::class,
            EnumValueFragmentModule::class,
            IntegerValueFragmentModule::class,
            ScopeFragmentModule::class,
            StringValueFragmentModule::class,
            WrenchProviderModule::class,
            TogglesProviderModule::class,
            OssFragmentModule::class,
            OssDetailFragmentModule::class
        ])
interface ApplicationComponent : AndroidInjector<TogglesApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ApplicationComponent
    }
}