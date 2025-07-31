package com.itb.diabetify.e2e.manager

import com.itb.diabetify.domain.manager.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeConnectivityManager @Inject constructor() : ConnectivityManager {
    
    private val _isConnected = MutableStateFlow(true)
    private val isConnectedFlow = _isConnected.asStateFlow()
    
    override fun isConnected(): Boolean = _isConnected.value
    
    override fun observeConnectivity(): Flow<Boolean> = isConnectedFlow
    
    // Test control methods
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
    
    fun simulateConnectionLoss() {
        _isConnected.value = false
    }
    
    fun simulateConnectionRestored() {
        _isConnected.value = true
    }
    
    fun reset() {
        _isConnected.value = true
    }
}
