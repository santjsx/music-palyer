package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.sync.EmbeddedSyncServer
import com.ipodmodern.audio.core.sync.SyncServerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val syncServer = EmbeddedSyncServer(application)
    val serverState: StateFlow<SyncServerState> = syncServer.serverState

    init {
        startServer()
    }

    fun startServer() {
        syncServer.startServer(8080)
    }

    fun stopServer() {
        syncServer.stopServer()
    }

    override fun onCleared() {
        super.onCleared()
        syncServer.stopServer()
    }
}
