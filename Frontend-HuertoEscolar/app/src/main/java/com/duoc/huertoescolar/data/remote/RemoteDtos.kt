package com.duoc.huertoescolar.data.remote

import com.google.gson.annotations.SerializedName

// --- DTOs Microservicio Catálogo (8081) ---
data class UsuarioDto(
    val id: Long? = null,
    val codigoFicticio: String = "",
    val nombreFicticio: String = "",
    val correoFicticio: String = "",
    val rol: String = "",
    val tallerGrupo: String = "",
    val activo: Boolean = true
)

data class SectorDto(
    val id: Long? = null,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "EN_USO",
    val responsableId: Long = 1L,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null
)

data class CultivoDto(
    val id: Long? = null,
    val nombreComun: String = "",
    val descripcion: String = "",
    val sectorId: Long = 1L,
    val fechaSiembra: String = "",
    val fechaTrasplante: String? = null,
    val fechaEstimadaCosecha: String = "",
    val frecuenciaRiegoDias: Int = 2,
    val estadoDesarrollo: String = "EN_CRECIMIENTO",
    val observaciones: String? = null
)

// --- DTOs Microservicio Operaciones (8082) ---
data class TareaDto(
    val id: Long? = null,
    val tipoTarea: String = "",
    val descripcion: String = "",
    val sectorId: Long = 1L,
    val cultivoId: Long? = null,
    val fechaCreacion: String = "",
    val fechaProgramada: String = "",
    val responsableId: Long = 2L,
    val creadorId: Long = 1L,
    val estado: String = "PENDIENTE",
    val fechaHoraRealizacion: String? = null,
    val comentarioEstudiante: String? = null
)

data class ValidacionDto(
    val id: Long? = null,
    val tareaId: Long,
    val validadorId: Long,
    val resultado: String,
    val comentario: String,
    val fechaHoraValidacion: String? = null
)

data class ReporteProblemaDto(
    val id: Long? = null,
    val sectorId: Long,
    val cultivoId: Long? = null,
    val tipoProblema: String,
    val descripcion: String,
    val fechaHoraReporte: String,
    val reportadoPorId: Long,
    val estado: String = "ABIERTO",
    val comentarioAdmin: String? = null,
    val fechaCierre: String? = null
)

// --- DTOs API Externa Open-Meteo (Clima San Bernardo) ---
data class OpenMeteoResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherDto? = null
)

data class CurrentWeatherDto(
    val temperature: Double = 0.0,
    val windspeed: Double = 0.0,
    val weathercode: Int = 0,
    val time: String = ""
)