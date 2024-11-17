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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
//import com.istea.appdelclima.data
import data.dataStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
    private val _ciudadesGuardadas = MutableStateFlow<List<String>>(emptyList()) // Cambié a List<String> porque estamos guardando los nombres de las ciudades.
    val ciudadesGuardadas: StateFlow<List<String>> get() = _ciudadesGuardadas

    private val dataStore = context.dataStore

    private val CITIES_KEY = stringSetPreferencesKey("ciudades_guardadas")

    init {
        viewModelScope.launch {
            cargarCiudadesGuardadas()
        }
    }

    // Método para ejecutar las intenciones que llegan desde la vista
    fun ejecutar(intencion: CiudadesIntencion) {
        when (intencion) {
            is CiudadesIntencion.Buscar -> buscar(intencion.nombre)
            CiudadesIntencion.ObtenerUbicacion -> obtenerUbicacion()
            is CiudadesIntencion.Seleccionar -> seleccionar(intencion.ciudad)
            is CiudadesIntencion.Eliminar -> eliminarCiudad(intencion.ciudadNombre)
        }
    }

    // Buscar ciudades por nombre
    private fun buscar(nombre: String) {
        uiState = CiudadesEstado.Cargando
        viewModelScope.launch {
            try {
                ciudades = repositorio.buscarCiudad(nombre)
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

    // Seleccionar una ciudad
    private fun seleccionar(ciudad: Ciudad) {
        val ruta = Ruta.Clima(
            lat = ciudad.lat,
            lon = ciudad.lon,
            nombre = ciudad.name
        )
        router.navegar(ruta)

        // Guardar la ciudad seleccionada
        viewModelScope.launch {
            guardarCiudadSeleccionada(ciudad.name)
        }
    }

    // Guardar una ciudad seleccionada en DataStore
    private suspend fun guardarCiudadSeleccionada(ciudad: String) {
        context.dataStore.edit { preferences ->
            val ciudadesGuardadas = preferences[CITIES_KEY] ?: emptySet()
            preferences[CITIES_KEY] = ciudadesGuardadas + ciudad
        }
        showSuccessMessage = true
        updateCiudadesGuardadas()
    }

    // Cargar las ciudades guardadas desde DataStore
    private suspend fun cargarCiudadesGuardadas() {
        dataStore.data.map { preferences ->
            preferences[CITIES_KEY] ?: emptySet()
        }.collect { ciudades ->
            _ciudadesGuardadas.value = ciudades.toList()
        }
    }

    // Eliminar una ciudad guardada
    private fun eliminarCiudad(ciudad: String) {
        viewModelScope.launch {
            val nuevasCiudades = _ciudadesGuardadas.value - ciudad
            dataStore.edit { preferences ->
                preferences[CITIES_KEY] = nuevasCiudades.toSet()
            }
            _ciudadesGuardadas.value = nuevasCiudades
        }
    }

    // Función para obtener la ubicación del usuario
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

    // Método para actualizar las ciudades guardadas
    private fun updateCiudadesGuardadas() {
        _ciudadesGuardadas.value = _ciudadesGuardadas.value
    }
}

class CiudadesViewModelFactory(
    private val repositorio: Repositorio,
    private val router: Router,
    private val context: Context // Incluimos el contexto
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadesViewModel::class.java)) {
            return CiudadesViewModel(repositorio, router, context) as T
        }
        throw IllegalArgumentException("Error desconocido ViewModel class")
    }
}
