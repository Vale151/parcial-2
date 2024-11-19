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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.istea.appdelclima.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

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
    Text(
        text = "Cómo seguirá el clima",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Start,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(10.dp)
    )
    LazyColumn {
        items(items = climas) { forecast ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp), // Bordes más redondeados
                colors = CardDefaults.cardColors(containerColor = Color(0xB76B30D2)) // Color de fondo
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

                    Text(
                        text = "Fecha: $formattedDate",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )

                    val icon = if (forecast.weather.first().icon.isNotEmpty()) forecast.weather.firstOrNull()?.icon else "01d"
                    val iconoUrl = "https://openweathermap.org/img/wn/${icon}@2x.png"

                    if (icon != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(iconoUrl)
                                .placeholder(R.drawable.ic_loading)
                                .error(R.drawable.ic_error)
                                .build(),
                            contentDescription = forecast.weather.first().description,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Text(
                        text = "Temperatura: ${forecast.main.temp.toInt()}°C",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Sensación térmica: ${forecast.main.feels_like.toInt()}°C",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "Humedad: ${forecast.main.humidity}%",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "Descripción: ${forecast.weather.firstOrNull()?.description ?: "No disponible"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

