package com.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.duoc.huertoescolar.data.local.entity.*
import com.duoc.huertoescolar.ui.components.*
import com.duoc.huertoescolar.ui.viewmodel.HuertoUiState

@Composable
fun ProblemasListScreen(
    state: HuertoUiState,
    onBack: () -> Unit,
    onNuevoReporte: () -> Unit,
    onActualizarEstado: (ReporteProblemaEntity, EstadoReporte, String) -> Unit
) {
    var reporteAEditar by remember { mutableStateOf<ReporteProblemaEntity?>(null) }

    Scaffold(
        topBar = { HuertoTopBar(title = "Problemas Reportados", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoReporte,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Reportar Problema") }
            )
        }
    ) { padding ->
        if (state.reportes.isEmpty()) {
            EmptyStateMessage("No hay problemas reportados en el huerto.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.reportes, key = { it.id }) { rep ->
                    val tipo = runCatching { TipoProblema.valueOf(rep.tipoProblema).etiqueta }.getOrDefault(rep.tipoProblema)
                    val estado = runCatching { EstadoReporte.valueOf(rep.estado) }.getOrDefault(EstadoReporte.INFORMADO)
                    val sector = state.sectores.find { it.id == rep.sectorId }?.nombre ?: "Sector"
                    val autor = state.usuarios.find { it.id == rep.reportadoPorId }?.nombreFicticio ?: "Usuario"
                    val evidencia = state.evidencias.find { it.reporteId == rep.id }

                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tipo, style = MaterialTheme.typography.titleMedium)
                                SemanticStatusBadge(estado.etiqueta, estado.style())
                            }
                            Text("Sector: $sector • Reportado por: $autor", style = MaterialTheme.typography.labelMedium)
                            Text(rep.descripcion, style = MaterialTheme.typography.bodyMedium)
                            evidencia?.let {
                                AsyncImage(
                                    model = it.uriFoto,
                                    contentDescription = "Foto problema",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            rep.comentarioAdmin?.let {
                                Text("Seguimiento docente: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            if (state.esAdmin) {
                                OutlinedButton(
                                    onClick = { reporteAEditar = rep },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gestionar Atención (Admin)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo exclusivo Administrador para cambiar estado de atención del reporte
    reporteAEditar?.let { rep ->
        var estadoSel by remember {
            mutableStateOf(runCatching { EstadoReporte.valueOf(rep.estado) }.getOrDefault(EstadoReporte.EN_REVISION))
        }
        var comentario by remember { mutableStateOf(rep.comentarioAdmin ?: "") }

        AlertDialog(
            onDismissRequest = { reporteAEditar = null },
            title = { Text("Actualizar Estado del Problema") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EnumOrItemDropdown(
                        label = "Estado de atención",
                        items = EstadoReporte.entries,
                        selectedItem = estadoSel,
                        itemLabel = { it.etiqueta },
                        onItemSelected = { estadoSel = it }
                    )
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentario de seguimiento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onActualizarEstado(rep, estadoSel, comentario)
                    reporteAEditar = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { reporteAEditar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ReporteFormScreen(
    state: HuertoUiState,
    onBack: () -> Unit,
    onGuardarReporte: (Long?, Long?, TipoProblema, String, String?) -> Map<String, String>
) {
    var sectorSel by remember { mutableStateOf(state.sectores.firstOrNull()) }
    var cultivoSel by remember { mutableStateOf<CultivoEntity?>(null) }
    var tipoSel by remember { mutableStateOf(TipoProblema.FALTA_AGUA) }
    var descripcion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val cultivosSector = state.cultivos.filter { it.sectorId == sectorSel?.id }

    Scaffold(topBar = { HuertoTopBar(title = "Nuevo Reporte de Problema", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnumOrItemDropdown(
                label = "Sector afectado *",
                items = state.sectores,
                selectedItem = sectorSel,
                itemLabel = { "${it.codigo} - ${it.nombre}" },
                onItemSelected = { sectorSel = it; cultivoSel = null },
                errorText = errors["sectorId"]
            )
            if (cultivosSector.isNotEmpty()) {
                EnumOrItemDropdown(
                    label = "Cultivo afectado (opcional)",
                    items = cultivosSector,
                    selectedItem = cultivoSel,
                    itemLabel = { it.nombreComun },
                    onItemSelected = { cultivoSel = it }
                )
            }
            EnumOrItemDropdown(
                label = "Tipo de problema *",
                items = TipoProblema.entries,
                selectedItem = tipoSel,
                itemLabel = { it.etiqueta },
                onItemSelected = { tipoSel = it }
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del problema observado *") },
                isError = errors.containsKey("descripcion"),
                supportingText = { errors["descripcion"]?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            EvidenciaFotograficaSelector(
                uriActual = fotoUri,
                onUriSeleccionada = { fotoUri = it },
                titulo = "Fotografía del problema (opcional)"
            )
            Button(
                onClick = {
                    val res = onGuardarReporte(sectorSel?.id, cultivoSel?.id, tipoSel, descripcion, fotoUri)
                    if (res.isEmpty()) onBack() else errors = res
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrar Reporte")
            }
        }
    }
}

@Composable
fun HistorialAlertasScreen(
    state: HuertoUiState,
    onBack: () -> Unit
) {
    Scaffold(topBar = { HuertoTopBar(title = "Historial y Recordatorios", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Recordatorios de Riego y Cosecha", style = MaterialTheme.typography.titleLarge)
            }
            items(state.cultivos, key = { "cult_${it.id}" }) { cultivo ->
                val sector = state.sectores.find { it.id == cultivo.sectorId }?.nombre ?: "Sector"
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${cultivo.nombreComun} ($sector)", style = MaterialTheme.typography.titleMedium)
                        Text("Riego planificado: cada ${cultivo.frecuenciaRiegoDias} día(s)", style = MaterialTheme.typography.bodyMedium)
                        Text("Cosecha estimada: ${cultivo.fechaEstimadaCosecha}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Historial de Validaciones y Actividades", style = MaterialTheme.typography.titleLarge)
            }
            if (state.validaciones.isEmpty()) {
                item { EmptyStateMessage("Aún no existen validaciones docentes registradas.") }
            } else {
                items(state.validaciones, key = { "val_${it.id}" }) { valItem ->
                    val tarea = state.tareas.find { it.id == valItem.tareaId }
                    val validador = state.usuarios.find { it.id == valItem.validadorId }?.nombreFicticio ?: "Docente"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tarea #${valItem.tareaId}: ${tarea?.descripcion?.take(45).orEmpty()}...", style = MaterialTheme.typography.titleMedium)
                            Text("Resultado: ${valItem.resultado} • Por: $validador", style = MaterialTheme.typography.labelMedium)
                            Text("Fecha: ${valItem.fechaHoraValidacion} • Obs: ${valItem.comentario}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}