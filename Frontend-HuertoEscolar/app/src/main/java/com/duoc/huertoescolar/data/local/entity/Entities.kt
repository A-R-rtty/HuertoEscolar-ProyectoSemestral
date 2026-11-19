package com.duoc.huertoescolar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoFicticio: String,
    val nombreFicticio: String,
    val correoFicticio: String,
    val password: String,
    val rol: String, // RolUsuario.name
    val tallerGrupo: String,
    val activo: Boolean = true
)

@Entity(
    tableName = "sectores",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("responsableId")]
)
data class SectorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String, // EstadoSector.name
    val responsableId: Long,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

@Entity(
    tableName = "cultivos",
    foreignKeys = [
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectorId")]
)
data class CultivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreComun: String,
    val descripcion: String,
    val sectorId: Long,
    val fechaSiembra: String, // ISO yyyy-MM-dd
    val fechaTrasplante: String? = null,
    val fechaEstimadaCosecha: String,
    val frecuenciaRiegoDias: Int,
    val estadoDesarrollo: String, // EstadoCultivo.name
    val observaciones: String = "",
    val fotoReferencialUri: String? = null
)

@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(entity = SectorEntity::class, parentColumns = ["id"], childColumns = ["sectorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CultivoEntity::class, parentColumns = ["id"], childColumns = ["cultivoId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["responsableId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["creadorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("sectorId"), Index("cultivoId"), Index("responsableId"), Index("creadorId")]
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoTarea: String, // TipoTarea.name
    val descripcion: String,
    val sectorId: Long,
    val cultivoId: Long? = null,
    val fechaCreacion: String,
    val fechaProgramada: String, // ISO yyyy-MM-dd
    val responsableId: Long,
    val creadorId: Long,
    val estado: String, // EstadoTarea.name
    val fechaHoraRealizacion: String? = null,
    val comentarioEstudiante: String? = null
) {
    fun estaAtrasada(hoy: LocalDate = LocalDate.now()): Boolean {
        if (estado == EstadoTarea.VALIDADA.name) return false
        return runCatching { LocalDate.parse(fechaProgramada).isBefore(hoy) }.getOrDefault(false)
    }
}

@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(entity = TareaEntity::class, parentColumns = ["id"], childColumns = ["tareaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CultivoEntity::class, parentColumns = ["id"], childColumns = ["cultivoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ReporteProblemaEntity::class, parentColumns = ["id"], childColumns = ["reporteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("tareaId"), Index("cultivoId"), Index("reporteId"), Index("usuarioId")]
)
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tareaId: Long? = null,
    val cultivoId: Long? = null,
    val reporteId: Long? = null,
    val uriFoto: String,
    val descripcion: String,
    val fechaCaptura: String,
    val usuarioId: Long
)

@Entity(
    tableName = "validaciones",
    foreignKeys = [
        ForeignKey(entity = TareaEntity::class, parentColumns = ["id"], childColumns = ["tareaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["validadorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("tareaId"), Index("validadorId")]
)
data class ValidacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tareaId: Long,
    val validadorId: Long,
    val resultado: String, // "APROBADA" o "RECHAZADA"
    val comentario: String,
    val fechaHoraValidacion: String
)

@Entity(
    tableName = "reportes_problemas",
    foreignKeys = [
        ForeignKey(entity = SectorEntity::class, parentColumns = ["id"], childColumns = ["sectorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CultivoEntity::class, parentColumns = ["id"], childColumns = ["cultivoId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["reportadoPorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("sectorId"), Index("cultivoId"), Index("reportadoPorId")]
)
data class ReporteProblemaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val cultivoId: Long? = null,
    val tipoProblema: String, // TipoProblema.name
    val descripcion: String,
    val fechaHoraReporte: String,
    val reportadoPorId: Long,
    val estado: String, // EstadoReporte.name
    val comentarioAdmin: String? = null,
    val fechaCierre: String? = null
)

@Entity(
    tableName = "lecturas_sensores_simuladas",
    foreignKeys = [
        ForeignKey(entity = SectorEntity::class, parentColumns = ["id"], childColumns = ["sectorId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("sectorId")]
)
data class LecturaSensorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val fechaHoraLectura: String,
    val humedadSueloPct: Int,
    val temperaturaC: Double,
    val humedadAmbientalPct: Int,
    val origen: String = "Simulación académica (dato de prueba)",
    val estadoLectura: String // "Normal", "Bajo", "Alto", "Pendiente de revisión"
)