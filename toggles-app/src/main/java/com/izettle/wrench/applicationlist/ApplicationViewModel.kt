package com.izettle.wrench.applicationlist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import kotlinx.coroutines.flow.Flow

internal class ApplicationViewModel
@ViewModelInject constructor(applicationDao: WrenchApplicationDao) : ViewModel() {

    internal val applications: Flow<PagingData<WrenchApplication>> = Pager(PagingConfig(pageSize = 20)) {
        applicationDao.getApplications()
    }.flow.cachedIn(viewModelScope)
}
