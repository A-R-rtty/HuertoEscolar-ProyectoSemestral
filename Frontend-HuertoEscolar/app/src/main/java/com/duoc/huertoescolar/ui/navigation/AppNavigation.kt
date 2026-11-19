package com.duoc.huertoescolar.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.huertoescolar.ui.screens.*
import com.duoc.huertoescolar.ui.viewmodel.HuertoViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val SECTORES = "sectores"
    const val SECTOR_DETAIL = "sector_detail/{sectorId}"
    const val SECTOR_FORM = "sector_form?sectorId={sectorId}"
    const val CULTIVO_FORM = "cultivo_form/{sectorId}?cultivoId={cultivoId}"
    const val TAREAS = "tareas"
    const val TAREA_DETAIL = "tarea_detail/{tareaId}"
    const val TAREA_FORM = "tarea_form?tareaId={tareaId}"
    const val PROBLEMAS = "problemas"
    const val REPORTE_FORM = "reporte_form"
    const val HISTORIAL = "historial"
}

@Composable
fun AppNavigation(viewModel: HuertoViewModel) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensajeFeedback) {
        state.mensajeFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarFeedback()
        }
    }

    LaunchedEffect(state.usuarioActual) {
        if (state.usuarioActual != null) {
            navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
        } else {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController = navController, startDestination = Routes.LOGIN) {
                composable(Routes.LOGIN) {
                    LoginScreen(state.loginErrors, state.loginGeneralError, viewModel::iniciarSesion)
                }
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        state = state,
                        viewModel = viewModel,
                        onNavigateSectores = { navController.navigate(Routes.SECTORES) },
                        onNavigateTareas = { navController.navigate(Routes.TAREAS) },
                        onNavigateProblemas = { navController.navigate(Routes.PROBLEMAS) },
                        onNavigateHistorial = { navController.navigate(Routes.HISTORIAL) },
                        onLogout = viewModel::cerrarSesion
                    )
                }
                composable(Routes.SECTORES) {
                    SectoresListScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onSelectSector = { id -> navController.navigate("sector_detail/$id") },
                        onNuevoSector = { navController.navigate("sector_form") }
                    )
                }
                composable(
                    route = Routes.SECTOR_DETAIL,
                    arguments = listOf(navArgument("sectorId") { type = NavType.LongType })
                ) { backStack ->
                    val sectorId = backStack.arguments?.getLong("sectorId") ?: 0L
                    SectorDetailScreen(
                        sectorId = sectorId,
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEditarSector = { id -> navController.navigate("sector_form?sectorId=$id") },
                        onNuevoCultivo = { sId -> navController.navigate("cultivo_form/$sId") },
                        onEditarCultivo = { sId, cId -> navController.navigate("cultivo_form/$sId?cultivoId=$cId") }
                    )
                }
                composable(
                    route = Routes.SECTOR_FORM,
                    arguments = listOf(navArgument("sectorId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStack ->
                    val id = backStack.arguments?.getLong("sectorId")?.takeIf { it > 0 }
                    SectorFormScreen(id, state, { navController.popBackStack() }, viewModel::guardarSector)
                }
                composable(
                    route = Routes.CULTIVO_FORM,
                    arguments = listOf(
                        navArgument("sectorId") { type = NavType.LongType },
                        navArgument("cultivoId") { type = NavType.LongType; defaultValue = -1L }
                    )
                ) { backStack ->
                    val sId = backStack.arguments?.getLong("sectorId") ?: 0L
                    val cId = backStack.arguments?.getLong("cultivoId")?.takeIf { it > 0 }
                    CultivoFormScreen(sId, cId, state, { navController.popBackStack() }, viewModel::guardarCultivo)
                }
                composable(Routes.TAREAS) {
                    TareasListScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onSelectTarea = { id -> navController.navigate("tarea_detail/$id") },
                        onNuevaTarea = { navController.navigate("tarea_form") }
                    )
                }
                composable(
                    route = Routes.TAREA_DETAIL,
                    arguments = listOf(navArgument("tareaId") { type = NavType.LongType })
                ) { backStack ->
                    val tId = backStack.arguments?.getLong("tareaId") ?: 0L
                    TareaDetailScreen(
                        tareaId = tId,
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEditarTarea = { id -> navController.navigate("tarea_form?tareaId=$id") },
                        onIniciarTarea = viewModel::iniciarTarea,
                        onEntregarTarea = viewModel::entregarTarea,
                        onRevisarTarea = viewModel::validarORechazarTarea
                    )
                }
                composable(
                    route = Routes.TAREA_FORM,
                    arguments = listOf(navArgument("tareaId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStack ->
                    val tId = backStack.arguments?.getLong("tareaId")?.takeIf { it > 0 }
                    TareaFormScreen(tId, state, { navController.popBackStack() }, viewModel::guardarTarea)
                }
                composable(Routes.PROBLEMAS) {
                    ProblemasListScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onNuevoReporte = { navController.navigate(Routes.REPORTE_FORM) },
                        onActualizarEstado = viewModel::actualizarEstadoProblema
                    )
                }
                composable(Routes.REPORTE_FORM) {
                    ReporteFormScreen(state, { navController.popBackStack() }, viewModel::reportarProblema)
                }
                composable(Routes.HISTORIAL) {
                    HistorialAlertasScreen(state, { navController.popBackStack() })
                }
            }
        }
    }
}