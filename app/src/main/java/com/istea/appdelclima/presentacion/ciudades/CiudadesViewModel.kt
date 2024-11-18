package com.istea.appdelclima.presentacion.ciudades

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.istea.appdelclima.repository.Repositorio
import com.istea.appdelclima.repository.modelos.Ciudad
import com.istea.appdelclima.router.Router
import com.istea.appdelclima.router.Ruta
import kotlinx.coroutines.launch
import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import data.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class CiudadesViewModel(
    val repositorio: Repositorio,
    val router: Router,
    val context: Context
) : ViewModel() {

    var uiState by mutableStateOf<CiudadesEstado>(CiudadesEstado.Vacio)
    var ciudades: List<Ciudad> = emptyList()
    var showSuccessMessage by mutableStateOf(false)
    private val _ciudadesGuardadas = MutableStateFlow<List<String>>(emptyList())
    val ciudadesGuardadas: StateFlow<List<String>> get() = _ciudadesGuardadas

    private val dataStore = context.dataStore

    private val CITIES_KEY = stringSetPreferencesKey("ciudades_guardadas")
    var mostrarCiudades: Boolean by mutableStateOf(true)
    var showDeletionMessage by mutableStateOf(false)

    init {
        viewModelScope.launch {
            cargarCiudadesGuardadas()
        }
    }

    fun ejecutar(intencion: CiudadesIntencion) {
        when (intencion) {
            is CiudadesIntencion.Buscar -> buscar(intencion.nombre)
            CiudadesIntencion.ObtenerUbicacion -> obtenerUbicacion()
            is CiudadesIntencion.Seleccionar -> seleccionar(intencion.ciudad)
            is CiudadesIntencion.Eliminar -> eliminarCiudad(intencion.ciudadNombre)
            is CiudadesIntencion.Guardar -> guardarCiudad(intencion.ciudadNombre)
        }
    }

    private fun buscar(nombre: String) {
        ciudades = emptyList()
        uiState = CiudadesEstado.Cargando
        viewModelScope.launch {
            try {
                ciudades = repositorio.buscarCiudad(nombre)
                mostrarCiudades = true
                if (ciudades.isEmpty()) {
                    uiState = CiudadesEstado.Vacio
                } else {
                    uiState = CiudadesEstado.Resultado(ciudades)
                }
            } catch (exception: Exception) {
                uiState = CiudadesEstado.Error(exception.message ?: "Error desconocido")
            }
        }
    }

    private fun seleccionar(ciudad: Ciudad) {
        val ruta = Ruta.Clima(
            lat = ciudad.lat,
            lon = ciudad.lon,
            nombre = ciudad.name
        )
        router.navegar(ruta)

        viewModelScope.launch {
            guardarCiudadSeleccionada(ciudad.name)
        }
    }

    private suspend fun guardarCiudadSeleccionada(ciudad: String) {
        context.dataStore.edit { preferences ->
            val ciudadesGuardadas = preferences[CITIES_KEY] ?: emptySet()
            preferences[CITIES_KEY] = ciudadesGuardadas + ciudad
        }
        showSuccessMessage = true
        updateCiudadesGuardadas()
    }

    private suspend fun cargarCiudadesGuardadas() {
        dataStore.data.map { preferences ->
            preferences[CITIES_KEY] ?: emptySet()
        }.collect { ciudades ->
            _ciudadesGuardadas.value = ciudades.toList()
        }
    }

    private fun eliminarCiudad(ciudad: String) {
        viewModelScope.launch {
            val nuevasCiudades = _ciudadesGuardadas.value - ciudad
            dataStore.edit { preferences ->
                preferences[CITIES_KEY] = nuevasCiudades.toSet()
            }
            _ciudadesGuardadas.value = nuevasCiudades
            showDeletionMessage = true
        }
    }

    private fun obtenerUbicacion() {
        uiState = CiudadesEstado.Cargando
        val ubicacion = "Buenos Aires"  // Este sería el resultado de obtener la ubicación real

        viewModelScope.launch {
            try {
                ciudades = repositorio.buscarCiudad(ubicacion)
                if (ciudades.isEmpty()) {
                    uiState = CiudadesEstado.Vacio
                } else {
                    uiState = CiudadesEstado.Resultado(ciudades)
                }
            } catch (exception: Exception) {
                uiState = CiudadesEstado.Error(exception.message ?: "Error al obtener la ubicación")
            }
        }
    }

    private fun updateCiudadesGuardadas() {
        _ciudadesGuardadas.value = _ciudadesGuardadas.value
    }

    private fun guardarCiudad(ciudadNombre: String) {
        viewModelScope.launch {
            guardarCiudadSeleccionada(ciudadNombre)
        }
    }
}

class CiudadesViewModelFactory(
    private val repositorio: Repositorio,
    private val router: Router,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadesViewModel::class.java)) {
            return CiudadesViewModel(repositorio, router, context) as T
        }
        throw IllegalArgumentException("Error desconocido ViewModel class")
    }
}
