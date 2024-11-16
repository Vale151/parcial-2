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
import androidx.datastore.core.DataStore
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey

//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ciudades_guardadas")

class CiudadesViewModel(
    val repositorio: Repositorio,
    val router: Router
) : ViewModel(){

    var uiState by mutableStateOf<CiudadesEstado>(CiudadesEstado.Vacio)
    var ciudades : List<Ciudad> = emptyList()

    // Clave para guardar las ciudades seleccionadas
    private val CITIES_KEY = stringSetPreferencesKey("ciudades_guardadas")

    fun ejecutar(intencion: CiudadesIntencion){
        when(intencion){
            is CiudadesIntencion.Buscar -> buscar(nombre = intencion.nombre)
            is CiudadesIntencion.Seleccionar -> seleccionar(ciudad = intencion.ciudad)
            CiudadesIntencion.ObtenerUbicacion -> obtenerUbicacion() // Nueva lógica para geolocalización
        }
    }

    //buscar city por nombre
    private fun buscar( nombre: String){

        uiState = CiudadesEstado.Cargando
        viewModelScope.launch {
            try {
                ciudades = repositorio.buscarCiudad(nombre)
                if (ciudades.isEmpty()) {
                    uiState = CiudadesEstado.Vacio
                } else {
                    uiState = CiudadesEstado.Resultado(ciudades)
                }
            } catch (exeption: Exception){
                uiState = CiudadesEstado.Error(exeption.message ?: "error desconocido")
            }
        }
    }

    //seleccionar una ciudad
    private fun seleccionar(ciudad: Ciudad){
        val ruta = Ruta.Clima(
            lat = ciudad.lat,
            lon = ciudad.lon,
            nombre = ciudad.name
        )
        router.navegar(ruta)
        // Guarda la ciudad seleccionada
        /*viewModelScope.launch {
            guardarCiudadSeleccionada(ciudad.name)
        }*/
    }

    /*private suspend fun guardarCiudadSeleccionada(nombreCiudad: String) {
        Context.dataStore.edit { preferences ->
            val ciudadesGuardadas = preferences[CITIES_KEY] ?: emptySet()
            preferences[CITIES_KEY] = ciudadesGuardadas + nombreCiudad
        }
    }

    fun obtenerCiudadesGuardadas(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[CITIES_KEY] ?: emptySet()
        }
    }*/


    // Función para obtener la ubicación del usuario
    private fun obtenerUbicacion() {
        // Lógica para obtener la ubicación del usuario
        uiState = CiudadesEstado.Cargando

        // Simulación de obtener la ubicación actual
        val ubicacion = "Buenos Aires"  // Esto sería el resultado de obtener la ubicación real

        // Buscar las ciudades relacionadas con la ubicación obtenida
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

    /*private fun obtenerUbicacion() {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                // Ahora puedes hacer una consulta para obtener ciudades cercanas usando lat, lon
            } ?: run {
                uiState = CiudadesEstado.Error("No se pudo obtener la ubicación.")
            }
        }
    }*/
}



class CiudadesViewModelFactory(
    private val repositorio: Repositorio,
    private val router: Router,
    private val context: Context // Incluimos el contexto
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadesViewModel::class.java)) {
            return CiudadesViewModel(repositorio,router) as T
        }
        throw IllegalArgumentException("Error desconocido ViewModel class")
    }
}