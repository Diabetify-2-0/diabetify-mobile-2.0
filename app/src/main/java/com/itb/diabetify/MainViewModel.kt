package com.itb.diabetify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.usecases.app_entry.AppEntryUseCase
import com.itb.diabetify.domain.usecases.connectivity.ConnectivityUseCases
import com.itb.diabetify.presentation.navgraph.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appEntryUseCase: AppEntryUseCase,
    private val tokenManager: TokenManager,
    private val connectivityUseCases: ConnectivityUseCases
): ViewModel() {
    var splashCondition by mutableStateOf(true)
        private set

    var startDestination by mutableStateOf<String?>(null) // Start as null
        private set

    var isConnected by mutableStateOf(true)
        private set

    private var previousDestination by mutableStateOf(Route.AppStartNavigation.route)
    private var connectivityObserverJob: Job? = null

    init {
        appEntryUseCase.readAppEntry().onEach { shouldStartFromRegisterScreen ->
            val isLoggedIn = tokenManager.isLoggedIn()
            val hasConnection = connectivityUseCases.checkConnectivity()

            val destination = when {
                !hasConnection -> Route.NoInternetScreen.route
                isLoggedIn -> Route.MainNavigation.route
                shouldStartFromRegisterScreen -> Route.AuthNavigation.route
                else -> Route.AppStartNavigation.route
            }

            startDestination = destination

            if (destination != Route.NoInternetScreen.route) {
                previousDestination = destination
            }

            isConnected = hasConnection
            delay(300)
            splashCondition = false

            observeConnectivity()
        }.launchIn(viewModelScope)
    }

    private fun observeConnectivity() {
        if (connectivityObserverJob != null) return

        connectivityObserverJob = connectivityUseCases.observeConnectivity().onEach { connected ->
            if (!connected && startDestination != Route.NoInternetScreen.route) {
                startDestination?.let { previousDestination = it }
                startDestination = Route.NoInternetScreen.route
            } else if (connected && startDestination == Route.NoInternetScreen.route) {
                startDestination = previousDestination
            }
            isConnected = connected
        }.launchIn(viewModelScope)
    }

    fun retryConnection() {
        val hasConnection = connectivityUseCases.checkConnectivity()
        if (hasConnection) {
            startDestination = previousDestination
            isConnected = true
        }
    }
}
