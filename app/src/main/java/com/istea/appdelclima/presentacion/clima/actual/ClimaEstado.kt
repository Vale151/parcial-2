package com.istea.appdelclima.presentacion.clima.actual

sealed class ClimaEstado {
    data class Exitoso (
        val ciudad: String = "",
        val temperatura: Double = 0.0,
        val descripcion: String= "",
        val st :Double = 0.0,
        val icono : String = ""
        //val ciudades: List<Ciudad> = emptyList() // Agrega esta lista
        ) : ClimaEstado()
    data class Error(
        val mensaje :String = "",
    ) : ClimaEstado()
    data object Vacio: ClimaEstado()
    data object Cargando: ClimaEstado()

}
