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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.tooling.preview.Preview
import  androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.LocationOn

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

    val state = viewModel.uiState
    val ciudadesGuardadas = viewModel.ciudadesGuardadas.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val mostrarCiudades = remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
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
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Componente de búsqueda
                CiudadesView(
                    state = state,
                    onAction = { intencion -> viewModel.ejecutar(intencion) },
                    mostrarCiudades = mostrarCiudades.value,
                    onMostrarCiudadesChanged = {mostrarCiudades.value = it}
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar ciudades guardadas
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

                // Resultados de búsqueda en LazyColumn
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
        // Mostrar el mensaje emergente si es necesario
        LaunchedEffect(viewModel.showSuccessMessage) {
            if (viewModel.showSuccessMessage) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ciudad guardada correctamente",
                        duration = SnackbarDuration.Short // Duración más corta
                    )

                    viewModel.showSuccessMessage = false // Resetear el estado
                }
            }
        }
        LaunchedEffect(viewModel.showDeletionMessage) {
            if (viewModel.showDeletionMessage) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ciudad eliminada",
                        duration = SnackbarDuration.Short // Duración más corta
                    )

                    viewModel.showDeletionMessage = false // Resetear el estado
                }
            }
        }
    }
}

@Composable
fun CiudadCard(
    ciudad: Ciudad,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de ciudad
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Ciudad",
                tint = Color(0xFF5C6BC0),
                modifier = Modifier.size(48.dp).padding(end = 16.dp)
            )

            // Información de la ciudad
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ciudad.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "País: ${ciudad.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Lat: ${ciudad.lat}, Lon: ${ciudad.lon}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Botón "Guardar"
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
            ) {
                Text(text = "Guardar", color = Color.White)
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
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ciudadNombre,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red
                )
            }
        }
    }
}




/*@Composable
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
}*/

/*@Composable
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
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red
                )
            }
        }
    }
}*/
