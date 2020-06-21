package com.izettle.wrench.applicationlist


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import javax.inject.Inject

internal class ApplicationViewModel
@Inject constructor(applicationDao: WrenchApplicationDao) : ViewModel() {

    private val mediatedApplications: MediatorLiveData<PagedList<WrenchApplication>>

    private val listEmpty: MutableLiveData<Boolean> = MutableLiveData()

    internal val applications: LiveData<PagedList<WrenchApplication>>
        get() = mediatedApplications

    internal val isListEmpty: LiveData<Boolean>
        get() = listEmpty

    init {

        listEmpty.value = true

        val applications = LivePagedListBuilder(applicationDao.getApplications(),
                PagedList.Config.Builder()
                        .setEnablePlaceholders(true)
                        .setPageSize(10)
                        .setPrefetchDistance(10)
                        .build()).build()

        mediatedApplications = MediatorLiveData()

        mediatedApplications.addSource(applications) { wrenchApplications ->
            listEmpty.value = wrenchApplications == null || wrenchApplications.size == 0
            mediatedApplications.setValue(wrenchApplications)
        }
    }
}
