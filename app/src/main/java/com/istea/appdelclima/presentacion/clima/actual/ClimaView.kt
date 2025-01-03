package com.istea.appdelclima.presentacion.clima.actual

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import java.time.LocalTime
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.istea.appdelclima.R
import android.util.Log

// Función principal ClimaView
@Composable
fun ClimaView(
    modifier: Modifier = Modifier,
    state: ClimaEstado,
    onAction: (ClimaIntencion) -> Unit
) {
    LifecycleEventEffect(ON_RESUME) {
        onAction(ClimaIntencion.actualizarClima)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is ClimaEstado.Error -> ErrorView(mensaje = state.mensaje)
            is ClimaEstado.Exitoso -> {
                // Verificar si icono es válido
                val icono = if (state.icono.isNotEmpty()) state.icono else "01d"  // Valor predeterminado si está vacío
                val iconoUrl = "https://openweathermap.org/img/wn/${icono}@2x.png"

                // Verificamos la URL del ícono
                Log.d("ClimaView", "URL del ícono VALEE: $iconoUrl")

                ClimaView(
                    ciudad = state.ciudad,
                    temperatura = state.temperatura,
                    descripcion = state.descripcion,
                    sensacionTermica = state.st,
                    iconoUrl = iconoUrl
                )
            }
            ClimaEstado.Vacio -> LoadingView()
            ClimaEstado.Cargando -> EmptyView()
        }
    }
}

@Composable
fun ClimaView(
    ciudad: String,
    temperatura: Double,
    descripcion: String,
    sensacionTermica: Double,
    iconoUrl: String, // URL del icono del clima
    modifier: Modifier = Modifier,
    cardColor: Color = Color(0xB76B30D2),
    iconContentDescription: String = "Icono del clima"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val horaActual = LocalTime.now()
        val saludo = when {
            horaActual.hour in 5..12 -> "Buenos días"
            horaActual.hour in 13..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }

        Text(
            text = "$saludo, este es el clima actual!",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Start
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = ciudad)
                Text(
                    text = "${temperatura.roundToInt()}°C",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(text = descripcion.capitalize(), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Sensación térmica: ${sensacionTermica.roundToInt()}°C",
                    style = MaterialTheme.typography.bodyMedium
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(iconoUrl)
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_error)
                        .build(),
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyView() {
    Text(
        text = "No hay nada que mostrar",
        color = Color.White
    )
}

@Composable
fun LoadingView() {
    Text(
        text = "Cargando",
        color = Color.White
        )
}

@Composable
fun ErrorView(mensaje: String) {
    Text(
        text = mensaje,
        color = Color.White
    )
}

