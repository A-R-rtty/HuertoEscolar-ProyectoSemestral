package com.duoc.huertoescolar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.huertoescolar.data.local.entity.RolUsuario
import com.duoc.huertoescolar.ui.components.ClimaSyncBanner
import com.duoc.huertoescolar.ui.components.SemanticStatusBadge
import com.duoc.huertoescolar.ui.theme.SemanticErrorBg
import com.duoc.huertoescolar.ui.theme.SemanticErrorText
import com.duoc.huertoescolar.ui.theme.SemanticInfoBg
import com.duoc.huertoescolar.ui.theme.SemanticInfoText
import com.duoc.huertoescolar.ui.theme.SemanticWarningBg
import com.duoc.huertoescolar.ui.theme.SemanticWarningText
import com.duoc.huertoescolar.data.local.entity.SemanticBadgeStyle
import com.duoc.huertoescolar.ui.viewmodel.HuertoUiState
import com.duoc.huertoescolar.ui.viewmodel.HuertoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: HuertoUiState,
    viewModel: HuertoViewModel,
    onNavigateSectores: () -> Unit,
    onNavigateTareas: () -> Unit,
    onNavigateProblemas: () -> Unit,
    onNavigateHistorial: () -> Unit,
    onLogout: () -> Unit
) {
    val temperaturaTexto by viewModel.temperaturaTexto.collectAsState()
    val recomendacionRiego by viewModel.recomendacionRiego.collectAsState()
    val estadoBackend by viewModel.estadoBackend.collectAsState()
    val backendConectado by viewModel.backendConectado.collectAsState()
    val usuario = state.usuarioActual ?: return
    var mostrarSensoresDetalle by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(usuario.nombreFicticio, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${RolUsuario.valueOf(usuario.rol).etiqueta} • ${usuario.tallerGrupo}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Aviso prioritario de tareas atrasadas
            AnimatedVisibility(visible = state.tareasAtrasadas.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SemanticErrorBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SemanticErrorText)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Atención: Tienes ${state.tareasAtrasadas.size} tarea(s) atrasada(s) según fecha programada.",
                            color = SemanticErrorText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Panel Diferenciado por Rol
            if (state.esAdmin) {
                AdminResumenCard(state)
            } else {
                EstudianteResumenCard(state)
            }

            // Accesos Directos a Módulos
            Text("Módulos del Huerto", style = MaterialTheme.typography.titleLarge)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardActionCard(
                    title = "Sectores y Cultivos",
                    subtitle = "${state.sectores.size} sectores • ${state.cultivos.size} cultivos",
                    icon = Icons.Default.Grass,
                    onClick = onNavigateSectores,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionCard(
                    title = if (state.esAdmin) "Gestión de Tareas" else "Mis Tareas",
                    subtitle = "${state.tareasVisiblesPorRol.size} asignadas",
                    icon = Icons.Default.Checklist,
                    onClick = onNavigateTareas,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardActionCard(
                    title = "Reportar Problemas",
                    subtitle = "${state.reportes.size} registros",
                    icon = Icons.Default.ReportProblem,
                    onClick = onNavigateProblemas,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionCard(
                    title = "Historial y Alertas",
                    subtitle = "Cronología y avisos",
                    icon = Icons.Default.History,
                    onClick = onNavigateHistorial,
                    modifier = Modifier.weight(1f)
                )
            }

            // Integración Clima Open-Meteo & Microservicios
            ClimaSyncBanner(
                temperaturaTexto = temperaturaTexto,
                recomendacionRiego = recomendacionRiego,
                estadoBackend = estadoBackend,
                backendConectado = backendConectado,
                onSincronizarClick = { viewModel.sincronizarServiciosExternos() }
            )

            // Módulo de Sensores Simulados (Claramente diferenciado de datos manuales)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize() // Animación funcional #3: expansión fluida de tarjeta
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Monitoreo Ambiental Referencial", style = MaterialTheme.typography.titleMedium)
                            SemanticStatusBadge(
                                text = "SIMULACIÓN ACADÉMICA • DATO DE PRUEBA",
                                style = SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        TextButton(onClick = { mostrarSensoresDetalle = !mostrarSensoresDetalle }) {
                            Text(if (mostrarSensoresDetalle) "Ocultar" else "Ver")
                        }
                    }

                    if (mostrarSensoresDetalle) {
                        Spacer(modifier = Modifier.height(10.dp))
                        state.lecturasSensores.forEach { lectura ->
                            val sectorNombre = state.sectores.find { it.id == lectura.sectorId }?.nombre ?: "Sector"
                            val esBajo = lectura.estadoLectura.equals("Bajo", ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sectorNombre, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Suelo: ${lectura.humedadSueloPct}% | Temp: ${lectura.temperaturaC}°C | Amb: ${lectura.humedadAmbientalPct}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                SemanticStatusBadge(
                                    text = lectura.estadoLectura,
                                    style = if (esBajo) SemanticBadgeStyle(SemanticWarningBg, SemanticWarningText)
                                    else SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminResumenCard(state: HuertoUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Panel de Supervisión Docente", style = MaterialTheme.typography.titleMedium)
            Text("• Tareas pendientes de validación: ${state.tareasPendientesValidacion.size}")
            Text("• Próximas cosechas (15 días): ${state.proximasCosechas.size}")
            Text("• Problemas activos: ${state.reportes.count { it.estado != "CERRADO" }}")
        }
    }
}

@Composable
private fun EstudianteResumenCard(state: HuertoUiState) {
    val pendientes = state.tareasVisiblesPorRol.count { it.estado == "PENDIENTE" || it.estado == "EN_DESARROLLO" }
    val rechazadas = state.tareasVisiblesPorRol.count { it.estado == "RECHAZADA" }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Mi Resumen de Taller", style = MaterialTheme.typography.titleMedium)
            Text("• Mis tareas por ejecutar: $pendientes")
            Text("• Esperando revisión docente: ${state.tareasPendientesValidacion.size}")
            if (rechazadas > 0) {
                Text("• Tareas rechazadas para corregir: $rechazadas", color = SemanticErrorText)
            }
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}