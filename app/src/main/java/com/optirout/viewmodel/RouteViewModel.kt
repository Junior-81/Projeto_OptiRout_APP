package com.optirout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.optirout.data.model.TransportModal

class RouteViewModel : ViewModel() {
    
    private val _selectedModal = mutableStateOf<TransportModal?>(null)
    val selectedModal: State<TransportModal?> = _selectedModal
    
    private val _modals = mutableStateOf(TransportModal.getDefaultModals())
    val modals: State<List<TransportModal>> = _modals
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    fun selectModal(modal: TransportModal) {
        _selectedModal.value = modal
    }
    
    fun startRoute() {
        if (_selectedModal.value != null) {
            _isLoading.value = true
            // TODO: Chamar API/repository para iniciar rota
            _isLoading.value = false
        }
    }
}