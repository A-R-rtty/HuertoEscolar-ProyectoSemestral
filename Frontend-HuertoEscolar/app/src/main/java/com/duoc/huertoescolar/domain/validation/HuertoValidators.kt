package com.duoc.huertoescolar.domain.validation

import com.duoc.huertoescolar.data.local.entity.RolUsuario
import com.duoc.huertoescolar.data.local.entity.UsuarioEntity
import java.time.LocalDate

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)

object HuertoValidators {

    fun validarLogin(correo: String, password: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (correo.isBlank()) {
            errors["correo"] = "El correo ficticio es obligatorio."
        } else if (!correo.contains("@") || !correo.contains(".")) {
            errors["correo"] = "Ingresa un formato de correo válido (ej. usuario@demo.cl)."
        }
        if (password.isBlank()) {
            errors["password"] = "La contraseña es obligatoria."
        } else if (password.length < 6) {
            errors["password"] = "La contraseña debe tener al menos 6 caracteres."
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarSector(codigo: String, nombre: String, ubicacion: String, descripcion: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (codigo.isBlank() || codigo.trim().length < 3) {
            errors["codigo"] = "El código debe tener al menos 3 caracteres (ej. SEC-01)."
        }
        if (nombre.isBlank() || nombre.trim().length < 4) {
            errors["nombre"] = "El nombre del sector debe tener al menos 4 caracteres."
        }
        if (ubicacion.isBlank()) {
            errors["ubicacion"] = "Indica la ubicación dentro de la escuela."
        }
        if (descripcion.isBlank()) {
            errors["descripcion"] = "Agrega una breve descripción del sector."
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarCultivo(
        nombreComun: String,
        sectorId: Long?,
        fechaSiembra: String,
        fechaTrasplante: String?,
        fechaEstimadaCosecha: String,
        frecuenciaRiegoText: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (nombreComun.isBlank() || nombreComun.trim().length < 3) {
            errors["nombreComun"] = "Ingresa el nombre común del cultivo (mín. 3 caracteres)."
        }
        if (sectorId == null || sectorId <= 0) {
            errors["sectorId"] = "Debes seleccionar un sector válido."
        }

        val siembraParsed = runCatching { LocalDate.parse(fechaSiembra) }.getOrNull()
        val cosechaParsed = runCatching { LocalDate.parse(fechaEstimadaCosecha) }.getOrNull()

        if (siembraParsed == null) {
            errors["fechaSiembra"] = "Formato de fecha inválido (usa AAAA-MM-DD)."
        }
        if (cosechaParsed == null) {
            errors["fechaEstimadaCosecha"] = "Formato de fecha inválido (usa AAAA-MM-DD)."
        }
        if (siembraParsed != null && cosechaParsed != null && cosechaParsed.isBefore(siembraParsed)) {
            errors["fechaEstimadaCosecha"] = "La cosecha estimada no puede ser anterior a la siembra."
        }
        if (!fechaTrasplante.isNullOrBlank()) {
            val trasplanteParsed = runCatching { LocalDate.parse(fechaTrasplante) }.getOrNull()
            if (trasplanteParsed == null) {
                errors["fechaTrasplante"] = "Formato de trasplante inválido (usa AAAA-MM-DD)."
            } else if (siembraParsed != null && trasplanteParsed.isBefore(siembraParsed)) {
                errors["fechaTrasplante"] = "El trasplante no puede ser anterior a la siembra."
            }
        }

        val riegoDias = frecuenciaRiegoText.toIntOrNull()
        if (riegoDias == null || riegoDias <= 0) {
            errors["frecuenciaRiego"] = "La frecuencia de riego debe ser un número entero mayor a 0."
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarTarea(
        descripcion: String,
        sectorId: Long?,
        fechaProgramada: String,
        responsable: UsuarioEntity?
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (descripcion.isBlank() || descripcion.trim().length < 8) {
            errors["descripcion"] = "Describe las instrucciones de la tarea (mín. 8 caracteres)."
        }
        if (sectorId == null || sectorId <= 0) {
            errors["sectorId"] = "Selecciona un sector asociado."
        }
        if (runCatching { LocalDate.parse(fechaProgramada) }.isFailure) {
            errors["fechaProgramada"] = "Ingresa una fecha válida (AAAA-MM-DD)."
        }
        if (responsable == null) {
            errors["responsableId"] = "Debes asignar un estudiante responsable."
        } else if (responsable.rol != RolUsuario.COLABORADOR.name || !responsable.activo) {
            errors["responsableId"] = "Solo se pueden asignar tareas a estudiantes colaboradores activos."
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarEntregaTarea(comentarioEstudiante: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (comentarioEstudiante.isBlank() || comentarioEstudiante.trim().length < 5) {
            errors["comentario"] = "Describe brevemente el trabajo realizado antes de entregar."
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarRevisionTarea(
        esAprobada: Boolean,
        comentarioAdmin: String,
        validador: UsuarioEntity,
        responsableTareaId: Long
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (validador.rol != RolUsuario.ADMINISTRADOR.name) {
            errors["permiso"] = "Solo un docente o tallerista autorizado puede validar tareas."
        }
        if (validador.id == responsableTareaId) {
            errors["permiso"] = "Un usuario no puede validar sus propias actividades."
        }
        if (!esAprobada && comentarioAdmin.isBlank()) {
            errors["comentarioAdmin"] = "Debes ingresar un motivo obligatorio al rechazar la tarea."
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validarReporteProblema(sectorId: Long?, descripcion: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (sectorId == null || sectorId <= 0) {
            errors["sectorId"] = "Selecciona el sector donde ocurre el problema."
        }
        if (descripcion.isBlank() || descripcion.trim().length < 8) {
            errors["descripcion"] = "Describe el problema observado (mín. 8 caracteres)."
        }
        return ValidationResult(errors.isEmpty(), errors)
    }
}