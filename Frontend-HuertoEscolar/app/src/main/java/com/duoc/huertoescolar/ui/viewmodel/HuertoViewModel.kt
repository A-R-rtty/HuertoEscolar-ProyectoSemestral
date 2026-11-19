package com.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.duoc.huertoescolar.data.local.entity.*
import com.duoc.huertoescolar.data.repository.HuertoRepository
import com.duoc.huertoescolar.domain.validation.HuertoValidators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class HuertoUiState(
    val usuarioActual: UsuarioEntity? = null,
    val loginErrors: Map<String, String> = emptyMap(),
    val loginGeneralError: String? = null,
    val usuarios: List<UsuarioEntity> = emptyList(),
    val sectores: List<SectorEntity> = emptyList(),
    val cultivos: List<CultivoEntity> = emptyList(),
    val tareas: List<TareaEntity> = emptyList(),
    val evidencias: List<EvidenciaEntity> = emptyList(),
    val validaciones: List<ValidacionEntity> = emptyList(),
    val reportes: List<ReporteProblemaEntity> = emptyList(),
    val lecturasSensores: List<LecturaSensorEntity> = emptyList(),
    val mensajeFeedback: String? = null,
) {
    val esAdmin: Boolean
        get() = usuarioActual?.rol == RolUsuario.ADMINISTRADOR.name

    val tareasVisiblesPorRol: List<TareaEntity>
        get() = when {
            usuarioActual == null -> emptyList()
            esAdmin -> tareas
            else -> tareas.filter { it.responsableId == usuarioActual.id }
        }

    val tareasAtrasadas: List<TareaEntity>
        get() = tareasVisiblesPorRol.filter { it.estaAtrasada() }

    val tareasPendientesValidacion: List<TareaEntity>
        get() = tareasVisiblesPorRol.filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION.name }

    val proximasCosechas: List<CultivoEntity>
        get() {
            val hoy = LocalDate.now()
            return cultivos.filter { cultivo ->
                runCatching {
                    val cosecha = LocalDate.parse(cultivo.fechaEstimadaCosecha)
                    !cosecha.isBefore(hoy) && cosecha.isBefore(hoy.plusDays(16))
                }.getOrDefault(false)
            }
        }
}

class HuertoViewModel(private val repository: HuertoRepository) : ViewModel() {

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    private val _loginErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _loginGeneralError = MutableStateFlow<String?>(null)
    private val _mensajeFeedback = MutableStateFlow<String?>(null)

    private val dataFlow = combine(
        repository.observarUsuarios(),
        repository.observarSectores(),
        repository.observarCultivos(),
        repository.observarTareas(),
    ) { usuarios, sectores, cultivos, tareas ->
        QuadData(usuarios, sectores, cultivos, tareas)
    }

    private val extraFlow = combine(
        repository.observarEvidencias(),
        repository.observarValidaciones(),
        repository.observarReportes(),
        repository.observarLecturasSensores()
    ) { evidencias, validaciones, reportes, lecturas ->
        ExtraData(evidencias, validaciones, reportes, lecturas)
    }
    private val _temperaturaTexto = MutableStateFlow("Cargando...")
    val temperaturaTexto: StateFlow<String> = _temperaturaTexto.asStateFlow()

    private val _recomendacionRiego = MutableStateFlow("Consultando Open-Meteo...")
    val recomendacionRiego: StateFlow<String> = _recomendacionRiego.asStateFlow()

    private val _estadoBackend = MutableStateFlow("Verificando microservicios...")
    val estadoBackend: StateFlow<String> = _estadoBackend.asStateFlow()

    private val _backendConectado = MutableStateFlow(false)
    val backendConectado: StateFlow<Boolean> = _backendConectado.asStateFlow()

    val uiState: StateFlow<HuertoUiState> = combine(
        _usuarioActual,
        _loginErrors,
        _loginGeneralError,
        _mensajeFeedback,
        combine(dataFlow, extraFlow) { d, e -> d to e }
    ) { usuario, loginErr, generalErr, feedback, combinedData ->
        val (d, e) = combinedData
        HuertoUiState(
            usuarioActual = usuario,
            loginErrors = loginErr,
            loginGeneralError = generalErr,
            usuarios = d.usuarios,
            sectores = d.sectores,
            cultivos = d.cultivos,
            tareas = d.tareas,
            evidencias = e.evidencias,
            validaciones = e.validaciones,
            reportes = e.reportes,
            lecturasSensores = e.lecturas,
            mensajeFeedback = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HuertoUiState()
    )

    init {
        sincronizarServiciosExternos()
    }

    fun iniciarSesion(correo: String, password: String) {
        val validacion = HuertoValidators.validarLogin(correo, password)
        if (!validacion.isValid) {
            _loginErrors.value = validacion.errors
            _loginGeneralError.value = null
            return
        }
        _loginErrors.value = emptyMap()
        viewModelScope.launch {
            val user = repository.autenticar(correo, password)
            if (user != null) {
                _usuarioActual.value = user
                _loginGeneralError.value = null
            } else {
                _loginGeneralError.value = "Credenciales incorrectas o cuenta inactiva."
            }
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _loginErrors.value = emptyMap()
        _loginGeneralError.value = null
    }

    fun limpiarFeedback() {
        _mensajeFeedback.value = null
    }

    // --- Operaciones de Tarea (Flujo de estados y reglas de negocio) ---
    fun iniciarTarea(tarea: TareaEntity) {
        val user = _usuarioActual.value ?: return
        if ((tarea.responsableId != user.id) && !uiState.value.esAdmin) return
        viewModelScope.launch {
            repository.actualizarTarea(tarea.copy(estado = EstadoTarea.EN_DESARROLLO.name))
            _mensajeFeedback.value = "Tarea iniciada y marcada en desarrollo."
        }
    }

    fun entregarTarea(
        tarea: TareaEntity,
        comentario: String,
        uriFoto: String?
    ): Map<String, String> {
        val user = _usuarioActual.value ?: return mapOf("auth" to "Sesión expirada")
        val valResult = HuertoValidators.validarEntregaTarea(comentario)
        if (!valResult.isValid) return valResult.errors

        val ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        viewModelScope.launch {
            repository.actualizarTarea(
                tarea.copy(
                    estado = EstadoTarea.PENDIENTE_VALIDACION.name,
                    fechaHoraRealizacion = ahora,
                    comentarioEstudiante = comentario.trim()
                )
            )
            if (!uriFoto.isNullOrBlank()) {
                repository.registrarEvidencia(
                    EvidenciaEntity(
                        tareaId = tarea.id,
                        uriFoto = uriFoto,
                        descripcion = "Evidencia de entrega: ${comentario.take(50)}",
                        fechaCaptura = ahora,
                        usuarioId = user.id
                    )
                )
            }
            _mensajeFeedback.value = "Tarea entregada. Queda pendiente de validación docente."
        }
        return emptyMap()
    }

    fun validarORechazarTarea(
        tarea: TareaEntity,
        aprobar: Boolean,
        comentarioAdmin: String
    ): Map<String, String> {
        val admin = _usuarioActual.value ?: return mapOf("auth" to "Sesión expirada")
        val check = HuertoValidators.validarRevisionTarea(
            aprobar,
            comentarioAdmin,
            admin,
            tarea.responsableId
        )
        if (!check.isValid) return check.errors

        val ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val nuevoEstado = if (aprobar) EstadoTarea.VALIDADA.name else EstadoTarea.RECHAZADA.name
        viewModelScope.launch {
            repository.registrarValidacion(
                validacion = ValidacionEntity(
                    tareaId = tarea.id,
                    validadorId = admin.id,
                    resultado = if (aprobar) "APROBADA" else "RECHAZADA",
                    comentario = comentarioAdmin.trim().ifBlank { "Actividad aprobada conforme." },
                    fechaHoraValidacion = ahora
                ),
                tareaActualizada = tarea.copy(estado = nuevoEstado)
            )
            _mensajeFeedback.value =
                if (aprobar) "Tarea aprobada correctamente." else "Tarea rechazada para corrección."
        }
        return emptyMap()
    }

    // --- Guardado de Sectores, Cultivos, Tareas y Reportes (Listos para Fase 3) ---
    fun guardarSector(
        id: Long,
        codigo: String,
        nombre: String,
        ubicacion: String,
        descripcion: String,
        estado: EstadoSector
    ): Map<String, String> {
        val admin = _usuarioActual.value ?: return mapOf("permiso" to "Sin sesión")
        if (!uiState.value.esAdmin) return mapOf("permiso" to "Acción exclusiva de administrador")
        val res = HuertoValidators.validarSector(codigo, nombre, ubicacion, descripcion)
        if (!res.isValid) return res.errors

        val hoy = LocalDate.now().toString()
        viewModelScope.launch {
            repository.guardarSector(
                SectorEntity(
                    id = id,
                    codigo = codigo.trim().uppercase(),
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    ubicacion = ubicacion.trim(),
                    estado = estado.name,
                    responsableId = admin.id,
                    fechaCreacion = hoy,
                    fechaActualizacion = hoy
                )
            )
            repository.enviarSectorRemoto(
                codigo = codigo.trim().uppercase(),
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                ubicacion = ubicacion.trim(),
                estado = estado.name,
                responsableId = admin.id
            )
            _mensajeFeedback.value = "Sector guardado exitosamente."
        }
        return emptyMap()
    }

    fun guardarCultivo(
        id: Long,
        nombreComun: String,
        descripcion: String,
        sectorId: Long?,
        fechaSiembra: String,
        fechaTrasplante: String?,
        fechaEstimadaCosecha: String,
        frecuenciaRiegoText: String,
        estado: EstadoCultivo,
        observaciones: String,
        fotoUri: String?
    ): Map<String, String> {
        if (!uiState.value.esAdmin) return mapOf("permiso" to "Acción exclusiva de administrador")
        val res = HuertoValidators.validarCultivo(
            nombreComun,
            sectorId,
            fechaSiembra,
            fechaTrasplante,
            fechaEstimadaCosecha,
            frecuenciaRiegoText
        )
        if (!res.isValid) return res.errors

        viewModelScope.launch {
            repository.guardarCultivo(
                CultivoEntity(
                    id = id,
                    nombreComun = nombreComun.trim(),
                    descripcion = descripcion.trim(),
                    sectorId = sectorId!!,
                    fechaSiembra = fechaSiembra.trim(),
                    fechaTrasplante = fechaTrasplante?.trim()?.takeIf { it.isNotEmpty() },
                    fechaEstimadaCosecha = fechaEstimadaCosecha.trim(),
                    frecuenciaRiegoDias = frecuenciaRiegoText.toInt(),
                    estadoDesarrollo = estado.name,
                    observaciones = observaciones.trim(),
                    fotoReferencialUri = fotoUri
                )
            )
            _mensajeFeedback.value = "Cultivo registrado correctamente."
        }
        return emptyMap()
    }

    fun guardarTarea(
        id: Long,
        tipo: TipoTarea,
        descripcion: String,
        sectorId: Long?,
        cultivoId: Long?,
        fechaProgramada: String,
        responsableId: Long?
    ): Map<String, String> {
        val admin = _usuarioActual.value ?: return mapOf("permiso" to "Sin sesión")
        if (!uiState.value.esAdmin) return mapOf("permiso" to "Acción exclusiva de administrador")
        val responsable = uiState.value.usuarios.find { it.id == responsableId }
        val res = HuertoValidators.validarTarea(descripcion, sectorId, fechaProgramada, responsable)
        if (!res.isValid) return res.errors

        viewModelScope.launch {
            val hoy = LocalDate.now().toString()
            repository.guardarTarea(
                TareaEntity(
                    id = id,
                    tipoTarea = tipo.name,
                    descripcion = descripcion.trim(),
                    sectorId = sectorId!!,
                    cultivoId = cultivoId,
                    fechaCreacion = hoy,
                    fechaProgramada = fechaProgramada.trim(),
                    responsableId = responsable!!.id,
                    creadorId = admin.id,
                    estado = EstadoTarea.PENDIENTE.name
                )
            )
            repository.enviarTareaRemota(
                tipoTarea = tipo.name,
                descripcion = descripcion.trim(),
                sectorId = sectorId,
                cultivoId = cultivoId,
                fechaCreacion = hoy,
                fechaProgramada = fechaProgramada.trim(),
                responsableId = responsable.id,
                creadorId = admin.id
            )
            _mensajeFeedback.value = "Tarea asignada a ${responsable.nombreFicticio}."
        }
        return emptyMap()
    }

    fun reportarProblema(
        sectorId: Long?,
        cultivoId: Long?,
        tipo: TipoProblema,
        descripcion: String,
        uriFoto: String?
    ): Map<String, String> {
        val user = _usuarioActual.value ?: return mapOf("permiso" to "Sin sesión")
        val res = HuertoValidators.validarReporteProblema(sectorId, descripcion)
        if (!res.isValid) return res.errors

        val ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        viewModelScope.launch {
            val reporteId = repository.crearReporte(
                ReporteProblemaEntity(
                    sectorId = sectorId!!,
                    cultivoId = cultivoId,
                    tipoProblema = tipo.name,
                    descripcion = descripcion.trim(),
                    fechaHoraReporte = ahora,
                    reportadoPorId = user.id,
                    estado = EstadoReporte.INFORMADO.name
                )
            )
            if (!uriFoto.isNullOrBlank()) {
                repository.registrarEvidencia(
                    EvidenciaEntity(
                        reporteId = reporteId,
                        uriFoto = uriFoto,
                        descripcion = "Evidencia de problema: ${tipo.etiqueta}",
                        fechaCaptura = ahora,
                        usuarioId = user.id
                    )
                )
            }
            repository.enviarReporteRemoto(
                sectorId = sectorId,
                cultivoId = cultivoId,
                tipoProblema = tipo.name,
                descripcion = descripcion.trim(),
                fechaHoraReporte = ahora,
                reportadoPorId = user.id
            )
            _mensajeFeedback.value = "Problema reportado al equipo docente."
        }
        return emptyMap()
    }

    fun actualizarEstadoProblema(
        reporte: ReporteProblemaEntity,
        nuevoEstado: EstadoReporte,
        comentarioAdmin: String
    ) {
        if (!uiState.value.esAdmin) return
        val ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        viewModelScope.launch {
            repository.actualizarReporte(
                reporte.copy(
                    estado = nuevoEstado.name,
                    comentarioAdmin = comentarioAdmin.trim().ifBlank { reporte.comentarioAdmin },
                    fechaCierre = if (nuevoEstado == EstadoReporte.CERRADO) ahora else null
                )
            )
            _mensajeFeedback.value = "Estado del reporte actualizado a ${nuevoEstado.etiqueta}."
        }
    }

    private data class QuadData(
        val usuarios: List<UsuarioEntity>,
        val sectores: List<SectorEntity>,
        val cultivos: List<CultivoEntity>,
        val tareas: List<TareaEntity>
    )

    private data class ExtraData(
        val evidencias: List<EvidenciaEntity>,
        val validaciones: List<ValidacionEntity>,
        val reportes: List<ReporteProblemaEntity>,
        val lecturas: List<LecturaSensorEntity>
    )

    class Factory(private val repository: HuertoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HuertoViewModel(repository) as T
        }
    }
    fun sincronizarServiciosExternos() {
        viewModelScope.launch {
            // 1. Sincronizar Clima
            try {
                val climaResult = repository.obtenerClimaSanBernardo()
                val clima = climaResult.getOrNull()
                if ((climaResult.isSuccess) && (clima != null)) {
                    _temperaturaTexto.value = "${clima.temperature}°C (Viento ${clima.windspeed} km/h)"
                    _recomendacionRiego.value = when {
                        clima.temperature >= 26.0 -> "Calor alto: priorizar riego temprano o al atardecer."
                        clima.weathercode in 51..99 -> "Lluvia detectada: suspender riego programado."
                        else -> "Condiciones óptimas para labores en el huerto."
                    }
                } else {
                    _temperaturaTexto.value = "Sin conexión externa"
                    _recomendacionRiego.value = "Modo local activo (revisar Internet)."
                }
            } catch (_: Exception) {
                _temperaturaTexto.value = "Sin conexión externa"
                _recomendacionRiego.value = "Modo local activo (revisar Internet)."
            }

            // 2. Sincronizar Microservicios
            try {
                val conexionResult = repository.probarConexionMicroservicios()
                if (conexionResult.isSuccess) {
                    _backendConectado.value = true
                    _estadoBackend.value = conexionResult.getOrNull() ?: "Conectado"
                } else {
                    _backendConectado.value = false
                    _estadoBackend.value = "Backend offline (usando base local Room)"
                }
            } catch (_: Exception) {
                _backendConectado.value = false
                _estadoBackend.value = "Backend offline (usando base local Room)"
            }
        }
    }
}
