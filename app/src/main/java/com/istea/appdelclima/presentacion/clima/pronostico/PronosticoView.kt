package com.istea.appdelclima.presentacion.clima.pronostico

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.istea.appdelclima.repository.modelos.ListForecast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.istea.appdelclima.R

@Composable
fun PronosticoView(
    modifier: Modifier = Modifier,
    state : PronosticoEstado,
    onAction: (PronosticoIntencion)->Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onAction(PronosticoIntencion.actualizarClima)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when(state){
            is PronosticoEstado.Error -> ErrorView(mensaje = state.mensaje)
            is PronosticoEstado.Exitoso -> PronosticoView(state.climas)
            PronosticoEstado.Vacio -> LoadingView()
            PronosticoEstado.Cargando -> EmptyView()
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun EmptyView(){
    Text(text = "No hay nada que mostrar")
}

@Composable
fun LoadingView(){
    Text(text = "Cargando")
}

@Composable
fun ErrorView(mensaje: String){
    Text(text = mensaje)
}

@Composable
fun PronosticoView(climas: List<ListForecast>) {
    LazyColumn {
        items(items = climas) { forecast ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp), // Bordes más redondeados
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)) // Color de fondo
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Convertir la fecha (timestamp a fecha legible)
                    val date = java.time.Instant.ofEpochSecond(forecast.dt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                    val formattedDate = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                        .format(date)

                    // Mostrar la fecha
                    Text(
                        text = "Fecha: $formattedDate",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )

                    // Icono del clima
                    val icon = forecast.weather.firstOrNull()?.icon
                    if (icon != null) {
                        Icon(
                            painter = painterResource(id = getIconResource(icon)),
                            contentDescription = "Icono del clima",
                            modifier = Modifier.size(40.dp),
                            tint = Color.DarkGray
                        )
                    }

                    // Temperatura
                    Text(
                        text = "Temperatura: ${forecast.main.temp.toInt()}°C",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )

                    // Sensación térmica
                    Text(
                        text = "Sensación térmica: ${forecast.main.feels_like.toInt()}°C",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )

                    // Humedad
                    Text(
                        text = "Humedad: ${forecast.main.humidity}%",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )

                    // Descripción del clima
                    Text(
                        text = "Descripción: ${forecast.weather.firstOrNull()?.description ?: "No disponible"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}


// Función para obtener el recurso del icono
fun getIconResource(icon: String): Int {
    return when (icon) {
        "01d" -> R.drawable.sun // Día soleado
        "01n" -> R.drawable.moon // Noche clara
        "02d" -> R.drawable.cloudy // Día parcialmente nublado
        "02n" -> R.drawable.cloudy // Noche parcialmente nublada
        "03d", "03n" -> R.drawable.cloud // Nublado
        "04d", "04n" -> R.drawable.cloud // Muy nublado
        "09d", "09n" -> R.drawable.storm // Lluvia
        "10d", "10n" -> R.drawable.storm // Lluvias intermitentes
        "11d", "11n" -> R.drawable.storm // Tormenta
        "13d", "13n" -> R.drawable.snowflake // Nieve
        "50d", "50n" -> R.drawable.fog // Niebla
        else -> R.drawable.unknown // Desconocido
    }
}

