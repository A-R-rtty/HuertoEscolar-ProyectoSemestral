package com.duoc.huertoescolar.data.local.entity

import androidx.compose.ui.graphics.Color
import com.duoc.huertoescolar.ui.theme.*

data class SemanticBadgeStyle(val containerColor: Color, val contentColor: Color)

enum class RolUsuario(val etiqueta: String) {
    ADMINISTRADOR("Docente / Tallerista"),
    COLABORADOR("Estudiante")
}

enum class EstadoSector(val etiqueta: String) {
    DISPONIBLE("Disponible"),
    EN_USO("En uso"),
    EN_MANTENIMIENTO("En mantenimiento"),
    FUERA_DE_USO("Fuera de uso");

    fun style(): SemanticBadgeStyle = when (this) {
        DISPONIBLE -> SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText)
        EN_USO -> SemanticBadgeStyle(SemanticSuccessBg, SemanticSuccessText)
        EN_MANTENIMIENTO -> SemanticBadgeStyle(SemanticWarningBg, SemanticWarningText)
        FUERA_DE_USO -> SemanticBadgeStyle(SemanticErrorBg, SemanticErrorText)
    }
}

enum class EstadoCultivo(val etiqueta: String) {
    PLANIFICADO("Planificado"),
    SEMBRADO("Sembrado"),
    EN_CRECIMIENTO("En crecimiento"),
    LISTO_PARA_COSECHAR("Listo para cosechar"),
    COSECHADO("Cosechado"),
    CON_PROBLEMAS("Con problemas"),
    FINALIZADO("Finalizado");

    fun style(): SemanticBadgeStyle = when (this) {
        PLANIFICADO, FINALIZADO -> SemanticBadgeStyle(SemanticNeutralBg, SemanticNeutralText)
        SEMBRADO, EN_CRECIMIENTO -> SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText)
        LISTO_PARA_COSECHAR, COSECHADO -> SemanticBadgeStyle(SemanticSuccessBg, SemanticSuccessText)
        CON_PROBLEMAS -> SemanticBadgeStyle(SemanticErrorBg, SemanticErrorText)
    }
}

enum class TipoTarea(val etiqueta: String) {
    PREPARACION_SECTOR("Preparación del sector"),
    SIEMBRA("Siembra"),
    TRASPLANTE("Trasplante"),
    RIEGO("Riego"),
    LIMPIEZA("Limpieza"),
    RETIRO_MALEZAS("Retiro de malezas"),
    OBSERVACION_CULTIVO("Observación del cultivo"),
    MANTENIMIENTO("Mantenimiento"),
    REVISION_SUELO("Revisión del suelo"),
    REGISTRO_CRECIMIENTO("Registro del crecimiento"),
    COSECHA("Cosecha"),
    OTRA("Otra actividad autorizada")
}

enum class EstadoTarea(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_DESARROLLO("En desarrollo"),
    PENDIENTE_VALIDACION("Pendiente de validación"),
    VALIDADA("Validada"),
    RECHAZADA("Rechazada");

    fun style(): SemanticBadgeStyle = when (this) {
        PENDIENTE -> SemanticBadgeStyle(SemanticNeutralBg, SemanticNeutralText)
        EN_DESARROLLO -> SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText)
        PENDIENTE_VALIDACION -> SemanticBadgeStyle(SemanticWarningBg, SemanticWarningText)
        VALIDADA -> SemanticBadgeStyle(SemanticSuccessBg, SemanticSuccessText)
        RECHAZADA -> SemanticBadgeStyle(SemanticErrorBg, SemanticErrorText)
    }
}

enum class TipoProblema(val etiqueta: String) {
    FALTA_AGUA("Falta de agua"),
    DANO_PLANTA("Daño en la planta"),
    PLAGA_SIMULADA("Plaga simulada"),
    DETERIORO_SECTOR("Deterioro del sector"),
    PROBLEMA_MANTENIMIENTO("Problema de mantenimiento"),
    OTRO("Otro problema observado")
}

enum class EstadoReporte(val etiqueta: String) {
    INFORMADO("Informado"),
    EN_REVISION("En revisión"),
    ATENDIDO("Atendido"),
    CERRADO("Cerrado");

    fun style(): SemanticBadgeStyle = when (this) {
        INFORMADO -> SemanticBadgeStyle(SemanticErrorBg, SemanticErrorText)
        EN_REVISION -> SemanticBadgeStyle(SemanticWarningBg, SemanticWarningText)
        ATENDIDO -> SemanticBadgeStyle(SemanticInfoBg, SemanticInfoText)
        CERRADO -> SemanticBadgeStyle(SemanticSuccessBg, SemanticSuccessText)
    }
}