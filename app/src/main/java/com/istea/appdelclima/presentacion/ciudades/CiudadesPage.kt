package com.istea.appdelclima.presentacion.ciudades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.istea.appdelclima.repository.RepositorioApi
import com.istea.appdelclima.router.Enrutador
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material3.Text
import com.istea.appdelclima.repository.modelos.Ciudad
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import coil.compose.rememberImagePainter
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.tooling.preview.Preview
import  androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState

@Composable
fun CiudadesPage(
    navHostController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: CiudadesViewModel = viewModel(
        factory = CiudadesViewModelFactory(
            repositorio = RepositorioApi(),
            router = Enrutador(navHostController),
            context = context // Pasamos el contexto para DataStore
        )
    )

    val state = viewModel.uiState // Ahora obtenemos el estado del ViewModel
    val ciudadesGuardadas = viewModel.ciudadesGuardadas.collectAsState().value // Lista de ciudades guardadas desde el ViewModel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5C6BC0), // Azul suave
                        Color(0xFF8E24AA)  // Violeta suave
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Componente de búsqueda y geolocalización
            CiudadesView(
                state = state,
                onAction = { intencion -> viewModel.ejecutar(intencion) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar ciudades guardadas solo si hay alguna
            if (ciudadesGuardadas.isNotEmpty()) {
                Text(
                    text = "Ciudades guardadas:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
                CiudadesGuardadas(
                    ciudadesGuardadas = ciudadesGuardadas.toSet(),
                    onRemove = { ciudad ->
                        viewModel.ejecutar(CiudadesIntencion.Eliminar(ciudad))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenedor de tarjetas de ciudades
            LazyColumn {
                when (state) {
                    is CiudadesEstado.Cargando -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is CiudadesEstado.Error -> {
                        item {
                            Text(
                                text = "Error: ${state.mensaje}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is CiudadesEstado.Resultado -> {
                        items(state.ciudades) { ciudad ->
                            CiudadCard(ciudad = ciudad) {
                                viewModel.ejecutar(CiudadesIntencion.Seleccionar(ciudad))
                            }
                        }
                    }
                    CiudadesEstado.Vacio -> {
                        item {
                            Text(
                                text = "No hay ciudades disponibles.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CiudadCard(
    ciudad: Ciudad, // Ahora pasa un objeto Ciudad
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Blue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aquí puedes poner un icono si lo deseas, en base al clima u otros datos
            Image(
                painter = rememberImagePainter(data = "url_o_ruta_a_un_icono"),
                contentDescription = "Icono del clima",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
            )

            // Información de la ciudad
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ciudad.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Aquí puedes incluir más detalles sobre la ciudad si es necesario
                Text(text= "Pais: ${ciudad.country}")
                Text(text = "Ciudad: ${ciudad.name}")
                Text(text = "Estado: ${ciudad.state}")
                Text(text = "Lat: ${ciudad.lat}, Lon: ${ciudad.lon}")
            }

            // Botón para guardar o realizar alguna acción
            Button(
                onClick = onSave,
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Text(text = "Guardar")
            }
        }
    }
}


@Composable
fun CiudadesGuardadas(
    ciudadesGuardadas: Set<String>,
    onRemove: (String) -> Unit
) {
    LazyColumn {
        items(ciudadesGuardadas.toList()) { ciudad ->
            CiudadCardGuardada(
                ciudadNombre = ciudad,
                onRemove = { onRemove(ciudad) }
            )
        }
    }
}

@Composable
fun CiudadCardGuardada(
    ciudadNombre: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ciudadNombre,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
