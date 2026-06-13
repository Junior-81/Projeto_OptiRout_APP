package com.optirout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optirout.data.model.RouteResponse
import com.optirout.data.model.TransportMode
import com.optirout.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RouteState {
    object Idle : RouteState()
    object Loading : RouteState()
    data class Success(val response: RouteResponse) : RouteState()
    data class Error(val message: String) : RouteState()
}

class RouteViewModel : ViewModel() {

    private val repository = RouteRepository()

    private val _selectedMode = MutableStateFlow<TransportMode?>(TransportMode.AUTO)
    val selectedMode: StateFlow<TransportMode?> = _selectedMode.asStateFlow()

    private val _routeState = MutableStateFlow<RouteState>(RouteState.Idle)
    val routeState: StateFlow<RouteState> = _routeState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectMode(mode: TransportMode) {
        _selectedMode.value = mode
    }

    fun calculateRoute() {
        val mode = _selectedMode.value ?: return
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val response = repository.calculateRoute(mode.apiValue)
                _routeState.value = RouteState.Success(response)
            } catch (e: Exception) {
                val msg = e.message ?: "Erro ao calcular rota"
                _routeState.value = RouteState.Error(msg)
                _errorMessage.value = msg
            }
        }
    }

    fun resetState() {
        _routeState.value = RouteState.Idle
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
