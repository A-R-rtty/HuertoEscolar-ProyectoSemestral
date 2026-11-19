package com.duoc.huertoescolar.data.repository

import com.duoc.huertoescolar.data.local.HuertoDatabase
import com.duoc.huertoescolar.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class HuertoRepository(private val db: HuertoDatabase) {

    fun observarUsuarios(): Flow<List<UsuarioEntity>> = db.usuarioDao().observarUsuarios()
    fun observarSectores(): Flow<List<SectorEntity>> = db.sectorDao().observarSectores()
    fun observarCultivos(): Flow<List<CultivoEntity>> = db.cultivoDao().observarCultivos()
    fun observarTareas(): Flow<List<TareaEntity>> = db.tareaDao().observarTareas()
    fun observarEvidencias(): Flow<List<EvidenciaEntity>> = db.evidenciaDao().observarEvidencias()
    fun observarValidaciones(): Flow<List<ValidacionEntity>> = db.validacionDao().observarValidaciones()
    fun observarReportes(): Flow<List<ReporteProblemaEntity>> = db.reporteProblemaDao().observarReportes()
    fun observarLecturasSensores(): Flow<List<LecturaSensorEntity>> = db.lecturaSensorDao().observarLecturas()

    suspend fun autenticar(correo: String, password: String): UsuarioEntity? =
        db.usuarioDao().login(correo.trim(), password)

    suspend fun guardarSector(sector: SectorEntity) {
        if (sector.id == 0L) db.sectorDao().insertar(sector) else db.sectorDao().actualizar(sector)
    }

    suspend fun guardarCultivo(cultivo: CultivoEntity) {
        if (cultivo.id == 0L) db.cultivoDao().insertar(cultivo) else db.cultivoDao().actualizar(cultivo)
    }

    suspend fun guardarTarea(tarea: TareaEntity) {
        if (tarea.id == 0L) db.tareaDao().insertar(tarea) else db.tareaDao().actualizar(tarea)
    }

    suspend fun actualizarTarea(tarea: TareaEntity) = db.tareaDao().actualizar(tarea)

    suspend fun registrarEvidencia(evidencia: EvidenciaEntity) = db.evidenciaDao().insertar(evidencia)

    suspend fun registrarValidacion(validacion: ValidacionEntity, tareaActualizada: TareaEntity) {
        db.validacionDao().insertar(validacion)
        db.tareaDao().actualizar(tareaActualizada)
    }

    suspend fun crearReporte(reporte: ReporteProblemaEntity): Long = db.reporteProblemaDao().insertar(reporte)

    suspend fun actualizarReporte(reporte: ReporteProblemaEntity) = db.reporteProblemaDao().actualizar(reporte)

    // --- INTEGRACIÓN REMOTA (MICROSERVICIOS + API CLIMA) ---

    suspend fun obtenerClimaSanBernardo(): Result<com.duoc.huertoescolar.data.remote.CurrentWeatherDto> {
        return try {
            val response = com.duoc.huertoescolar.data.remote.RetrofitClient.climaApi.getClimaActual()
            val clima = response.currentWeather
            if (clima != null) Result.success(clima)
            else Result.failure(Exception("Sin datos meteorológicos"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun probarConexionMicroservicios(): Result<String> {
        return try {
            val sectoresRemotos = com.duoc.huertoescolar.data.remote.RetrofitClient.catalogoApi.getSectores()
            val tareasRemotas = com.duoc.huertoescolar.data.remote.RetrofitClient.operacionesApi.getTareas()
            Result.success("Conectado: ${sectoresRemotos.size} sectores (8081) y ${tareasRemotas.size} tareas (8082)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarSectorRemoto(
        codigo: String,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        estado: String,
        responsableId: Long
    ) {
        try {
            com.duoc.huertoescolar.data.remote.RetrofitClient.catalogoApi.crearSector(
                com.duoc.huertoescolar.data.remote.SectorDto(
                    codigo = codigo,
                    nombre = nombre,
                    descripcion = descripcion,
                    ubicacion = ubicacion,
                    estado = estado,
                    responsableId = responsableId
                )
            )
        } catch (_: Exception) { /* Mantiene respaldo local Room si no hay red */ }
    }

    suspend fun enviarTareaRemota(
        tipoTarea: String,
        descripcion: String,
        sectorId: Long,
        cultivoId: Long?,
        fechaCreacion: String,
        fechaProgramada: String,
        responsableId: Long,
        creadorId: Long
    ) {
        try {
            com.duoc.huertoescolar.data.remote.RetrofitClient.operacionesApi.crearTarea(
                com.duoc.huertoescolar.data.remote.TareaDto(
                    tipoTarea = tipoTarea,
                    descripcion = descripcion,
                    sectorId = sectorId,
                    cultivoId = cultivoId,
                    fechaCreacion = fechaCreacion,
                    fechaProgramada = fechaProgramada,
                    responsableId = responsableId,
                    creadorId = creadorId
                )
            )
        } catch (_: Exception) { }
    }

    suspend fun enviarReporteRemoto(
        sectorId: Long,
        cultivoId: Long?,
        tipoProblema: String,
        descripcion: String,
        fechaHoraReporte: String,
        reportadoPorId: Long
    ) {
        try {
            com.duoc.huertoescolar.data.remote.RetrofitClient.operacionesApi.crearReporte(
                com.duoc.huertoescolar.data.remote.ReporteProblemaDto(
                    sectorId = sectorId,
                    cultivoId = cultivoId,
                    tipoProblema = tipoProblema,
                    descripcion = descripcion,
                    fechaHoraReporte = fechaHoraReporte,
                    reportadoPorId = reportadoPorId
                )
            )
        } catch (_: Exception) { }
    }
}
