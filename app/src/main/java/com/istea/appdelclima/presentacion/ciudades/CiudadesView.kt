package com.istea.appdelclima.presentacion.ciudades

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import com.istea.appdelclima.repository.modelos.Ciudad
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import  androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CiudadesView(
    modifier: Modifier = Modifier,
    state: CiudadesEstado,
    onAction: (CiudadesIntencion) -> Unit,
    mostrarCiudades: Boolean,
    onMostrarCiudadesChanged: (Boolean) -> Unit
) {
    var value by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = value,
                label = { Text(text = "Buscar por nombre") },
                onValueChange = {
                    value = it
                    onAction(CiudadesIntencion.Buscar(value))
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    onAction(CiudadesIntencion.ObtenerUbicacion)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Obtener ubicación",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        when (state) {
            CiudadesEstado.Cargando -> Text(text = "Cargando...", modifier = Modifier.align(Alignment.CenterHorizontally))
            is CiudadesEstado.Error -> Text(text = state.mensaje, modifier = Modifier.align(Alignment.CenterHorizontally))
            is CiudadesEstado.Resultado -> ListaDeCiudades(state.ciudades) {
                onAction(CiudadesIntencion.Seleccionar(it))
                onMostrarCiudadesChanged(false)
            }
            CiudadesEstado.Vacio -> {
                Text(
                    text = "No hay resultados aún",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDeCiudades(ciudades: List<Ciudad>, onSelect: (Ciudad) -> Unit) {
    LazyColumn {
        items(items = ciudades) { ciudad ->
            Card(onClick = { onSelect(ciudad) }) {
                Text(text = ciudad.name, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}