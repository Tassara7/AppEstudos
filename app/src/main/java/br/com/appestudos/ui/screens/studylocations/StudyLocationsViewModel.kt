package br.com.appestudos.ui.screens.studylocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.location.LocationResult
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.repository.StudyTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyLocationsViewModel(
    private val studyTrackingRepository: StudyTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyLocationsState())
    val uiState = _uiState.asStateFlow()

    init {
        loadLocations()
        refreshCurrentLocation()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            studyTrackingRepository.getAllActiveLocations().collect { locations ->
                _uiState.update { it.copy(locations = locations) }
            }
        }
    }

    fun refreshCurrentLocation() {
        _uiState.update { it.copy(isLoadingLocation = true) }
        
        viewModelScope.launch {
            when (val result = studyTrackingRepository.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val address = studyTrackingRepository.getAddressFromCoordinates(
                        result.location.latitude,
                        result.location.longitude
                    )
                    _uiState.update {
                        it.copy(
                            currentLocation = address ?: "Endereço não disponível",
                            isLoadingLocation = false,
                            currentLatitude = result.location.latitude,
                            currentLongitude = result.location.longitude
                        )
                    }
                }
                is LocationResult.Error -> {
                    _uiState.update {
                        it.copy(
                            currentLocation = result.message,
                            isLoadingLocation = false
                        )
                    }
                }
            }
        }
    }

    fun addCurrentLocationAsStudyLocation(name: String, radius: Double = 100.0) {
        val currentState = _uiState.value
        if (currentState.currentLatitude != null && currentState.currentLongitude != null) {
            viewModelScope.launch {
                try {
                    studyTrackingRepository.createLocation(
                        name = name,
                        latitude = currentState.currentLatitude,
                        longitude = currentState.currentLongitude,
                        radius = radius
                    )
                    _uiState.update { it.copy(message = "Local adicionado com sucesso!") }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Erro ao adicionar local: ${e.message}") }
                }
            }
        } else {
            _uiState.update { it.copy(error = "Localização atual não disponível") }
        }
    }

    fun editLocation(location: StudyLocation) {
        _uiState.update { it.copy(selectedLocation = location) }
    }

    fun updateLocation(location: StudyLocation) {
        viewModelScope.launch {
            try {
                studyTrackingRepository.updateLocation(location)
                _uiState.update { 
                    it.copy(
                        selectedLocation = null,
                        message = "Local atualizado com sucesso!"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao atualizar local: ${e.message}") }
            }
        }
    }

    fun deleteLocation(location: StudyLocation) {
        viewModelScope.launch {
            try {
                studyTrackingRepository.deleteLocation(location)
                _uiState.update { it.copy(message = "Local removido com sucesso!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao remover local: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSelectedLocation() {
        _uiState.update { it.copy(selectedLocation = null) }
    }
}

data class StudyLocationsState(
    val locations: List<StudyLocation> = emptyList(),
    val currentLocation: String? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val isLoadingLocation: Boolean = false,
    val selectedLocation: StudyLocation? = null,
    val message: String? = null,
    val error: String? = null
)