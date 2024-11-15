package com.istea.appdelclima.presentacion.clima

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.istea.appdelclima.presentacion.clima.actual.ClimaView
import com.istea.appdelclima.presentacion.clima.actual.ClimaViewModel
import com.istea.appdelclima.presentacion.clima.actual.ClimaViewModelFactory
import com.istea.appdelclima.presentacion.clima.pronostico.PronosticoView
import com.istea.appdelclima.presentacion.clima.pronostico.PronosticoViewModel
import com.istea.appdelclima.presentacion.clima.pronostico.PronosticoViewModelFactory
import com.istea.appdelclima.repository.RepositorioApi
import com.istea.appdelclima.router.Enrutador
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.istea.appdelclima.presentacion.clima.pronostico.PronosticoEstado
import com.istea.appdelclima.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ClimaPage(
    navHostController: NavHostController,
    lat: Float,
    lon: Float,
    nombre: String
) {
    val viewModel: ClimaViewModel = viewModel(
        factory = ClimaViewModelFactory(
            repositorio = RepositorioApi(),
            router = Enrutador(navHostController),
            lat = lat,
            lon = lon,
            nombre = nombre
        )
    )
    val pronosticoViewModel: PronosticoViewModel = viewModel(
        factory = PronosticoViewModelFactory(
            repositorio = RepositorioApi(),
            router = Enrutador(navHostController),
            nombre = nombre
        )
    )

    val state = pronosticoViewModel.uiState

    val icon = when (state) {
        is PronosticoEstado.Exitoso -> state.climas.firstOrNull()?.weather?.firstOrNull()?.icon
        else -> null
    }

    val backgroundRes = icon?.let { getBackGround(it) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        backgroundRes?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = "Fondo dinámico",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.8f),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ClimaView(
                state = viewModel.uiState,
                onAction = { intencion ->
                    viewModel.ejecutar(intencion)
                }
            )
            PronosticoView(
                state = pronosticoViewModel.uiState,
                onAction = { intencion ->
                    pronosticoViewModel.ejecutar(intencion)
                }
            )
        }
    }
}

fun getBackGround(icon: String): Int {
    return if (icon.endsWith("d")) {
        R.drawable.opc3_dia
    } else {
        R.drawable.opc3_noche
    }
}

