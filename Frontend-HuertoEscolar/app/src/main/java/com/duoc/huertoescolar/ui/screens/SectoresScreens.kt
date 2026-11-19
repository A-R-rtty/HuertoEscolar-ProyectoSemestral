package com.duoc.huertoescolar.ui.screens

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
import com.duoc.huertoescolar.ui.viewmodel.HuertoUiState
import java.time.LocalDate

@Composable
fun SectoresListScreen(
    state: HuertoUiState,
    onBack: () -> Unit,
    onSelectSector: (Long) -> Unit,
    onNuevoSector: () -> Unit
) {
    Scaffold(
        topBar = { HuertoTopBar(title = "Sectores del Huerto", onBack = onBack) },
        floatingActionButton = {
            if (state.esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onNuevoSector,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nuevo Sector") }
                )
            }
        }
    ) { padding ->
        if (state.sectores.isEmpty()) {
            EmptyStateMessage("No hay sectores registrados aún.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.sectores, key = { it.id }) { sector ->
                    val estadoEnum = runCatching { EstadoSector.valueOf(sector.estado) }.getOrDefault(EstadoSector.DISPONIBLE)
                    val cultivosSector = state.cultivos.count { it.sectorId == sector.id }

                    ElevatedCard(
                        onClick = { onSelectSector(sector.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${sector.codigo} • ${sector.nombre}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                SemanticStatusBadge(text = estadoEnum.etiqueta, style = estadoEnum.style())
                            }
                            Text("Ubicación: ${sector.ubicacion}", style = MaterialTheme.typography.bodyMedium)
                            Text(sector.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Cultivos asociados: $cultivosSector", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectorDetailScreen(
    sectorId: Long,
    state: HuertoUiState,
    onBack: () -> Unit,
    onEditarSector: (Long) -> Unit,
    onNuevoCultivo: (Long) -> Unit,
    onEditarCultivo: (Long, Long) -> Unit
) {
    val sector = state.sectores.find { it.id == sectorId } ?: return
    val cultivos = state.cultivos.filter { it.sectorId == sectorId }
    val estadoSector = runCatching { EstadoSector.valueOf(sector.estado) }.getOrDefault(EstadoSector.DISPONIBLE)

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = sector.nombre,
                onBack = onBack,
                actions = {
                    if (state.esAdmin) {
                        IconButton(onClick = { onEditarSector(sector.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar sector")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { onNuevoCultivo(sector.id) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Registrar Cultivo") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Código: ${sector.codigo}", style = MaterialTheme.typography.labelMedium)
                            SemanticStatusBadge(estadoSector.etiqueta, estadoSector.style())
                        }
                        Text("Ubicación: ${sector.ubicacion}", style = MaterialTheme.typography.bodyLarge)
                        Text(sector.descripcion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text("Cultivos en este sector (${cultivos.size})", style = MaterialTheme.typography.titleLarge)
            }

            if (cultivos.isEmpty()) {
                item { EmptyStateMessage("Este sector aún no tiene cultivos asociados.") }
            } else {
                items(cultivos, key = { it.id }) { cultivo ->
                    val estadoCultivo = runCatching { EstadoCultivo.valueOf(cultivo.estadoDesarrollo) }
                        .getOrDefault(EstadoCultivo.PLANIFICADO)

                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cultivo.nombreComun, style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SemanticStatusBadge(estadoCultivo.etiqueta, estadoCultivo.style())
                                    if (state.esAdmin) {
                                        IconButton(onClick = { onEditarCultivo(sector.id, cultivo.id) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar cultivo")
                                        }
                                    }
                                }
                            }
                            Text(cultivo.descripcion, style = MaterialTheme.typography.bodyMedium)
                            Text("Siembra: ${cultivo.fechaSiembra} | Cosecha est.: ${cultivo.fechaEstimadaCosecha}", style = MaterialTheme.typography.labelMedium)
                            Text("Frecuencia de riego: cada ${cultivo.frecuenciaRiegoDias} día(s)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            if (!cultivo.fotoReferencialUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = cultivo.fotoReferencialUri,
                                    contentDescription = "Foto cultivo ${cultivo.nombreComun}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp))
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
fun SectorFormScreen(
    sectorId: Long?,
    state: HuertoUiState,
    onBack: () -> Unit,
    onGuardar: (Long, String, String, String, String, EstadoSector) -> Map<String, String>
) {
    if (!state.esAdmin) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val existente = state.sectores.find { it.id == sectorId }
    var codigo by remember { mutableStateOf(existente?.codigo ?: "") }
    var nombre by remember { mutableStateOf(existente?.nombre ?: "") }
    var ubicacion by remember { mutableStateOf(existente?.ubicacion ?: "") }
    var descripcion by remember { mutableStateOf(existente?.descripcion ?: "") }
    var estado by remember {
        mutableStateOf(existente?.let { runCatching { EstadoSector.valueOf(it.estado) }.getOrNull() } ?: EstadoSector.DISPONIBLE)
    }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Scaffold(
        topBar = { HuertoTopBar(title = if (existente == null) "Nuevo Sector" else "Editar Sector", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código (ej. SEC-ESTE) *") },
                isError = errors.containsKey("codigo"),
                supportingText = { errors["codigo"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del sector *") },
                isError = errors.containsKey("nombre"),
                supportingText = { errors["nombre"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación dentro de la escuela *") },
                isError = errors.containsKey("ubicacion"),
                supportingText = { errors["ubicacion"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            EnumOrItemDropdown(
                label = "Estado del sector",
                items = EstadoSector.entries,
                selectedItem = estado,
                itemLabel = { it.etiqueta },
                onItemSelected = { estado = it }
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción general *") },
                isError = errors.containsKey("descripcion"),
                supportingText = { errors["descripcion"]?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val result = onGuardar(existente?.id ?: 0L, codigo, nombre, ubicacion, descripcion, estado)
                    if (result.isEmpty()) onBack() else errors = result
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar Sector")
            }
        }
    }
}

@Composable
fun CultivoFormScreen(
    sectorIdInicial: Long,
    cultivoId: Long?,
    state: HuertoUiState,
    onBack: () -> Unit,
    onGuardar: (Long, String, String, Long?, String, String?, String, String, EstadoCultivo, String, String?) -> Map<String, String>
) {
    if (!state.esAdmin) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val existente = state.cultivos.find { it.id == cultivoId }
    val hoy = LocalDate.now().toString()

    var nombreComun by remember { mutableStateOf(existente?.nombreComun ?: "") }
    var descripcion by remember { mutableStateOf(existente?.descripcion ?: "") }
    var sectorSeleccionado by remember {
        mutableStateOf(state.sectores.find { it.id == (existente?.sectorId ?: sectorIdInicial) })
    }
    var fechaSiembra by remember { mutableStateOf(existente?.fechaSiembra ?: hoy) }
    var fechaTrasplante by remember { mutableStateOf(existente?.fechaTrasplante ?: "") }
    var fechaCosecha by remember { mutableStateOf(existente?.fechaEstimadaCosecha ?: LocalDate.now().plusDays(45).toString()) }
    var frecuenciaRiego by remember { mutableStateOf(existente?.frecuenciaRiegoDias?.toString() ?: "2") }
    var estado by remember {
        mutableStateOf(existente?.let { runCatching { EstadoCultivo.valueOf(it.estadoDesarrollo) }.getOrNull() } ?: EstadoCultivo.SEMBRADO)
    }
    var observaciones by remember { mutableStateOf(existente?.observaciones ?: "") }
    var fotoUri by remember { mutableStateOf(existente?.fotoReferencialUri) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Scaffold(
        topBar = { HuertoTopBar(title = if (existente == null) "Registrar Cultivo" else "Editar Cultivo", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombreComun,
                onValueChange = { nombreComun = it },
                label = { Text("Nombre común del cultivo *") },
                isError = errors.containsKey("nombreComun"),
                supportingText = { errors["nombreComun"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            EnumOrItemDropdown(
                label = "Sector asignado *",
                items = state.sectores,
                selectedItem = sectorSeleccionado,
                itemLabel = { "${it.codigo} - ${it.nombre}" },
                onItemSelected = { sectorSeleccionado = it },
                errorText = errors["sectorId"]
            )
            OutlinedTextField(
                value = fechaSiembra,
                onValueChange = { fechaSiembra = it },
                label = { Text("Fecha de siembra (AAAA-MM-DD) *") },
                isError = errors.containsKey("fechaSiembra"),
                supportingText = { errors["fechaSiembra"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaTrasplante,
                onValueChange = { fechaTrasplante = it },
                label = { Text("Fecha de trasplante (opcional, AAAA-MM-DD)") },
                isError = errors.containsKey("fechaTrasplante"),
                supportingText = { errors["fechaTrasplante"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaCosecha,
                onValueChange = { fechaCosecha = it },
                label = { Text("Fecha estimada de cosecha (AAAA-MM-DD) *") },
                isError = errors.containsKey("fechaEstimadaCosecha"),
                supportingText = { errors["fechaEstimadaCosecha"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = frecuenciaRiego,
                onValueChange = { frecuenciaRiego = it },
                label = { Text("Frecuencia de riego (cada N días) *") },
                isError = errors.containsKey("frecuenciaRiego"),
                supportingText = { errors["frecuenciaRiego"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            EnumOrItemDropdown(
                label = "Estado de desarrollo",
                items = EstadoCultivo.entries,
                selectedItem = estado,
                itemLabel = { it.etiqueta },
                onItemSelected = { estado = it }
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del cultivo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones generales") },
                modifier = Modifier.fillMaxWidth()
            )

            EvidenciaFotograficaSelector(
                uriActual = fotoUri,
                onUriSeleccionada = { fotoUri = it },
                titulo = "Fotografía referencial del cultivo"
            )

            Button(
                onClick = {
                    val res = onGuardar(
                        existente?.id ?: 0L, nombreComun, descripcion, sectorSeleccionado?.id,
                        fechaSiembra, fechaTrasplante, fechaCosecha, frecuenciaRiego,
                        estado, observaciones, fotoUri
                    )
                    if (res.isEmpty()) onBack() else errors = res
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar Cultivo")
            }
        }
    }
}