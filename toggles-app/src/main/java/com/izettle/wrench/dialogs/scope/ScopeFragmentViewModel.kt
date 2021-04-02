package com.izettle.wrench.dialogs.scope

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScopeFragmentViewModel @Inject internal constructor(
    private val savedStateHandle: SavedStateHandle,
    private val scopeDao: WrenchScopeDao
) : ViewModel() {

    private val applicationId: Long = savedStateHandle.get<Long>("applicationId")!!

    internal val selectedScopeLiveData: LiveData<WrenchScope> by lazy {
        scopeDao.getSelectedScopeLiveData(applicationId)
    }

    internal var selectedScope: WrenchScope? = null

    internal val scopes: LiveData<List<WrenchScope>>
        get() = scopeDao.getScopes(applicationId)

    internal fun selectScope(wrenchScope: WrenchScope) {
        viewModelScope.launch {
            wrenchScope.timeStamp = Date()
            scopeDao.update(wrenchScope)
        }
    }

    @Throws(SQLiteException::class)
    internal fun createScope(scopeName: String) {
        viewModelScope.launch {
            val wrenchScope = WrenchScope()
            wrenchScope.name = scopeName
            wrenchScope.applicationId = applicationId
            wrenchScope.id = scopeDao.insert(wrenchScope)
        }
    }

    internal fun removeScope(scope: WrenchScope) {
        viewModelScope.launch {
            scopeDao.delete(scope)
        }
    }
}
