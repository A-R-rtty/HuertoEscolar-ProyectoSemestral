package com.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.duoc.huertoescolar.data.local.entity.*
import com.duoc.huertoescolar.ui.components.*
import com.duoc.huertoescolar.data.local.entity.SemanticBadgeStyle
import com.duoc.huertoescolar.ui.theme.SemanticErrorBg
import com.duoc.huertoescolar.ui.theme.SemanticErrorText
import com.duoc.huertoescolar.ui.viewmodel.HuertoUiState
import java.time.LocalDate

@Composable
fun TareasListScreen(
    state: HuertoUiState,
    onBack: () -> Unit,
    onSelectTarea: (Long) -> Unit,
    onNuevaTarea: () -> Unit
) {
    var filtroEstado by remember { mutableStateOf<String>("TODAS") }
    var filtroSoloAtrasadas by remember { mutableStateOf(false) }
    var filtroEstudianteId by remember { mutableStateOf<Long?>(null) }

    val estudiantes = state.usuarios.filter { it.rol == RolUsuario.COLABORADOR.name }

    val tareasFiltradas = state.tareasVisiblesPorRol.filter { tarea ->
        val cumpleEstado = filtroEstado == "TODAS" || tarea.estado == filtroEstado
        val cumpleAtraso = !filtroSoloAtrasadas || tarea.estaAtrasada()
        val cumpleResponsable = filtroEstudianteId == null || tarea.responsableId == filtroEstudianteId
        cumpleEstado && cumpleAtraso && cumpleResponsable
    }

    Scaffold(
        topBar = { HuertoTopBar(title = if (state.esAdmin) "Todas las Tareas" else "Mis Tareas Asignadas", onBack = onBack) },
        floatingActionButton = {
            if (state.esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaTarea,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Asignar Tarea") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtros por Estado y Fecha (Atrasadas)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroEstado == "TODAS" && !filtroSoloAtrasadas,
                    onClick = { filtroEstado = "TODAS"; filtroSoloAtrasadas = false },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = filtroSoloAtrasadas,
                    onClick = { filtroSoloAtrasadas = !filtroSoloAtrasadas },
                    label = { Text("Atrasadas (${state.tareasAtrasadas.size})") }
                )
                EstadoTarea.entries.forEach { est ->
                    FilterChip(
                        selected = filtroEstado == est.name,
                        onClick = { filtroEstado = if (filtroEstado == est.name) "TODAS" else est.name },
                        label = { Text(est.etiqueta) }
                    )
                }
            }

            // Filtro por Responsable (visible para Administrador)
            if (state.esAdmin && estudiantes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroEstudianteId == null,
                        onClick = { filtroEstudianteId = null },
                        label = { Text("Todos los estudiantes") }
                    )
                    estudiantes.forEach { est ->
                        FilterChip(
                            selected = filtroEstudianteId == est.id,
                            onClick = { filtroEstudianteId = if (filtroEstudianteId == est.id) null else est.id },
                            label = { Text(est.nombreFicticio.substringBefore(" (")) }
                        )
                    }
                }
            }

            if (tareasFiltradas.isEmpty()) {
                EmptyStateMessage("No se encontraron tareas con los filtros seleccionados.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tareasFiltradas, key = { it.id }) { tarea ->
                        val tipo = runCatching { TipoTarea.valueOf(tarea.tipoTarea).etiqueta }.getOrDefault(tarea.tipoTarea)
                        val estado = runCatching { EstadoTarea.valueOf(tarea.estado) }.getOrDefault(EstadoTarea.PENDIENTE)
                        val sector = state.sectores.find { it.id == tarea.sectorId }?.nombre ?: "Sector"
                        val responsable = state.usuarios.find { it.id == tarea.responsableId }?.nombreFicticio ?: "Estudiante"

                        ElevatedCard(onClick = { onSelectTarea(tarea.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tipo, style = MaterialTheme.typography.titleMedium)
                                    SemanticStatusBadge(estado.etiqueta, estado.style())
                                }
                                Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                Text("Sector: $sector • Resp.: $responsable", style = MaterialTheme.typography.labelMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Programada: ${tarea.fechaProgramada}", style = MaterialTheme.typography.labelMedium)
                                    if (tarea.estaAtrasada()) {
                                        Spacer(Modifier.width(8.dp))
                                        SemanticStatusBadge("ATRASADA", SemanticBadgeStyle(SemanticErrorBg, SemanticErrorText))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaDetailScreen(
    tareaId: Long,
    state: HuertoUiState,
    onBack: () -> Unit,
    onEditarTarea: (Long) -> Unit,
    onIniciarTarea: (TareaEntity) -> Unit,
    onEntregarTarea: (TareaEntity, String, String?) -> Map<String, String>,
    onRevisarTarea: (TareaEntity, Boolean, String) -> Map<String, String>
) {
    val tarea = state.tareas.find { it.id == tareaId } ?: return
    val usuarioActual = state.usuarioActual ?: return

    // Control de acceso: un estudiante no puede abrir tareas de otros estudiantes
    if (!state.esAdmin && tarea.responsableId != usuarioActual.id) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val tipo = runCatching { TipoTarea.valueOf(tarea.tipoTarea).etiqueta }.getOrDefault(tarea.tipoTarea)
    val estado = runCatching { EstadoTarea.valueOf(tarea.estado) }.getOrDefault(EstadoTarea.PENDIENTE)
    val sector = state.sectores.find { it.id == tarea.sectorId }?.nombre ?: "Sector"
    val cultivo = state.cultivos.find { it.id == tarea.cultivoId }?.nombreComun ?: "General del sector"
    val responsable = state.usuarios.find { it.id == tarea.responsableId }?.nombreFicticio ?: "Estudiante"
    val evidenciasTarea = state.evidencias.filter { it.tareaId == tarea.id }
    val validacionesTarea = state.validaciones.filter { it.tareaId == tarea.id }

    var comentarioEntrega by remember { mutableStateOf(tarea.comentarioEstudiante ?: "") }
    var fotoEntregaUri by remember { mutableStateOf<String?>(null) }
    var errorsEntrega by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var comentarioAdmin by remember { mutableStateOf("") }
    var errorsAdmin by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Detalle de Tarea",
                onBack = onBack,
                actions = {
                    if (state.esAdmin && tarea.estado != EstadoTarea.VALIDADA.name) {
                        IconButton(onClick = { onEditarTarea(tarea.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar tarea")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tipo, style = MaterialTheme.typography.titleLarge)
                        SemanticStatusBadge(estado.etiqueta, estado.style())
                    }
                    Text("Instrucciones: ${tarea.descripcion}", style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider()
                    Text("Sector: $sector", style = MaterialTheme.typography.bodyMedium)
                    Text("Cultivo: $cultivo", style = MaterialTheme.typography.bodyMedium)
                    Text("Responsable asignado: $responsable", style = MaterialTheme.typography.bodyMedium)
                    Text("Fecha programada: ${tarea.fechaProgramada}", style = MaterialTheme.typography.bodyMedium)
                    tarea.fechaHoraRealizacion?.let {
                        Text("Entregada el: $it", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Historial de Revisiones Docentes previas (ej. motivo de rechazo)
            if (validacionesTarea.isNotEmpty()) {
                Text("Historial de Validaciones", style = MaterialTheme.typography.titleMedium)
                validacionesTarea.forEach { valItem ->
                    val validadorNombre = state.usuarios.find { it.id == valItem.validadorId }?.nombreFicticio ?: "Docente"
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${valItem.resultado} por $validadorNombre (${valItem.fechaHoraValidacion})", style = MaterialTheme.typography.labelMedium)
                            Text("Observación: ${valItem.comentario}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Evidencias Fotográficas ya adjuntadas
            if (evidenciasTarea.isNotEmpty()) {
                Text("Evidencias Adjuntas (${evidenciasTarea.size})", style = MaterialTheme.typography.titleMedium)
                evidenciasTarea.forEach { ev ->
                    AsyncImage(
                        model = ev.uriFoto,
                        contentDescription = ev.descripcion,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(10.dp))
                    )
                }
            }

            // --- FLUJO ESTUDIANTE: Iniciar y Entregar (incluye reenvío si fue RECHAZADA) ---
            val esResponsable = tarea.responsableId == usuarioActual.id
            if (esResponsable && (estado == EstadoTarea.PENDIENTE || estado == EstadoTarea.EN_DESARROLLO || estado == EstadoTarea.RECHAZADA)) {
                if (estado == EstadoTarea.PENDIENTE) {
                    Button(
                        onClick = { onIniciarTarea(tarea) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar Tarea (En desarrollo)")
                    }
                }

                if (estado == EstadoTarea.EN_DESARROLLO || estado == EstadoTarea.RECHAZADA) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (estado == EstadoTarea.RECHAZADA) "Corregir y Reenviar Tarea" else "Entregar Actividad Realizada",
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedTextField(
                                value = comentarioEntrega,
                                onValueChange = { comentarioEntrega = it },
                                label = { Text("Comentario del trabajo realizado *") },
                                isError = errorsEntrega.containsKey("comentario"),
                                supportingText = { errorsEntrega["comentario"]?.let { Text(it) } },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            EvidenciaFotograficaSelector(
                                uriActual = fotoEntregaUri,
                                onUriSeleccionada = { fotoEntregaUri = it }
                            )
                            Button(
                                onClick = {
                                    val res = onEntregarTarea(tarea, comentarioEntrega, fotoEntregaUri)
                                    if (res.isEmpty()) onBack() else errorsEntrega = res
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Enviar a Validación Docente")
                            }
                        }
                    }
                }
            }

            // --- FLUJO ADMINISTRADOR: Aprobar o Rechazar ---
            if (state.esAdmin && estado == EstadoTarea.PENDIENTE_VALIDACION) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Revisión y Validación Docente", style = MaterialTheme.typography.titleMedium)
                        Text("Comentario estudiante: \"${tarea.comentarioEstudiante.orEmpty()}\"", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = comentarioAdmin,
                            onValueChange = { comentarioAdmin = it },
                            label = { Text("Observación docente (obligatoria si rechaza)") },
                            isError = errorsAdmin.containsKey("comentarioAdmin") || errorsAdmin.containsKey("permiso"),
                            supportingText = {
                                (errorsAdmin["comentarioAdmin"] ?: errorsAdmin["permiso"])?.let { Text(it) }
                            },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    val res = onRevisarTarea(tarea, false, comentarioAdmin)
                                    if (res.isEmpty()) onBack() else errorsAdmin = res
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Rechazar")
                            }
                            Button(
                                onClick = {
                                    val res = onRevisarTarea(tarea, true, comentarioAdmin)
                                    if (res.isEmpty()) onBack() else errorsAdmin = res
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Aprobar Tarea")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaFormScreen(
    tareaId: Long?,
    state: HuertoUiState,
    onBack: () -> Unit,
    onGuardar: (Long, TipoTarea, String, Long?, Long?, String, Long?) -> Map<String, String>
) {
    if (!state.esAdmin) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val existente = state.tareas.find { it.id == tareaId }
    val estudiantes = state.usuarios.filter { it.rol == RolUsuario.COLABORADOR.name }

    var tipo by remember {
        mutableStateOf(existente?.let { runCatching { TipoTarea.valueOf(it.tipoTarea) }.getOrNull() } ?: TipoTarea.RIEGO)
    }
    var descripcion by remember { mutableStateOf(existente?.descripcion ?: "") }
    var sectorSel by remember { mutableStateOf(state.sectores.find { it.id == existente?.sectorId } ?: state.sectores.firstOrNull()) }
    var cultivoSel by remember { mutableStateOf(state.cultivos.find { it.id == existente?.cultivoId }) }
    var fechaProgramada by remember { mutableStateOf(existente?.fechaProgramada ?: LocalDate.now().plusDays(1).toString()) }
    var responsableSel by remember { mutableStateOf(estudiantes.find { it.id == existente?.responsableId } ?: estudiantes.firstOrNull()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val cultivosDelSector = state.cultivos.filter { it.sectorId == sectorSel?.id }

    Scaffold(
        topBar = { HuertoTopBar(title = if (existente == null) "Nueva Tarea" else "Editar Tarea", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnumOrItemDropdown(
                label = "Tipo de tarea *",
                items = TipoTarea.entries,
                selectedItem = tipo,
                itemLabel = { it.etiqueta },
                onItemSelected = { tipo = it }
            )
            EnumOrItemDropdown(
                label = "Sector relacionado *",
                items = state.sectores,
                selectedItem = sectorSel,
                itemLabel = { "${it.codigo} - ${it.nombre}" },
                onItemSelected = { sectorSel = it; cultivoSel = null },
                errorText = errors["sectorId"]
            )
            if (cultivosDelSector.isNotEmpty()) {
                EnumOrItemDropdown(
                    label = "Cultivo relacionado (opcional)",
                    items = cultivosDelSector,
                    selectedItem = cultivoSel,
                    itemLabel = { it.nombreComun },
                    onItemSelected = { cultivoSel = it }
                )
            }
            EnumOrItemDropdown(
                label = "Estudiante responsable *",
                items = estudiantes,
                selectedItem = responsableSel,
                itemLabel = { "${it.nombreFicticio} (${it.tallerGrupo})" },
                onItemSelected = { responsableSel = it },
                errorText = errors["responsableId"]
            )
            OutlinedTextField(
                value = fechaProgramada,
                onValueChange = { fechaProgramada = it },
                label = { Text("Fecha programada (AAAA-MM-DD) *") },
                isError = errors.containsKey("fechaProgramada"),
                supportingText = { errors["fechaProgramada"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Instrucciones detalladas *") },
                isError = errors.containsKey("descripcion"),
                supportingText = { errors["descripcion"]?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val res = onGuardar(
                        existente?.id ?: 0L, tipo, descripcion, sectorSel?.id,
                        cultivoSel?.id, fechaProgramada, responsableSel?.id
                    )
                    if (res.isEmpty()) onBack() else errors = res
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Asignar Tarea")
            }
        }
    }
}