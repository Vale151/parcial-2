package com.istea.appdelclima.presentacion.ciudades

import com.istea.appdelclima.repository.modelos.Ciudad

sealed class CiudadesIntencion {
    data class Buscar( val nombre:String ) : CiudadesIntencion()
    data class Seleccionar(val ciudad: Ciudad) : CiudadesIntencion()
    object ObtenerUbicacion : CiudadesIntencion()
    data class Eliminar(val ciudadNombre: String) : CiudadesIntencion() // Nueva intención
    data class Guardar(val ciudadNombre: String) : CiudadesIntencion()
}

