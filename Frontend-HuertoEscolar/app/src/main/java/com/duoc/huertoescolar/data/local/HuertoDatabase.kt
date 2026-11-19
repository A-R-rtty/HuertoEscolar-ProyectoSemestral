// data/local/HuertoDatabase.kt
package com.duoc.huertoescolar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duoc.huertoescolar.data.local.dao.*
import com.duoc.huertoescolar.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        UsuarioEntity::class,
        SectorEntity::class,
        CultivoEntity::class,
        TareaEntity::class,
        EvidenciaEntity::class,
        ValidacionEntity::class,
        ReporteProblemaEntity::class,
        LecturaSensorEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HuertoDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun validacionDao(): ValidacionDao
    abstract fun reporteProblemaDao(): ReporteProblemaDao
    abstract fun lecturaSensorDao(): LecturaSensorDao

    companion object {
        @Volatile
        private var INSTANCE: HuertoDatabase? = null

        fun getDatabase(context: Context): HuertoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HuertoDatabase::class.java,
                    "huerto_escolar_san_bernardo.db"
                )
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    poblarDatosFicticios(database)
                }
            }
        }

        private suspend fun poblarDatosFicticios(db: HuertoDatabase) {
            val hoy = LocalDate.now()

            // 1. 4 Cuentas Ficticias (1 Administrador + 3 Estudiantes)
            val usuarios = listOf(
                UsuarioEntity(
                    id = 1,
                    codigoFicticio = "ADM-01",
                    nombreFicticio = "Prof. Elena Rojas (Ficticia)",
                    correoFicticio = "admin.huerto@demo.cl",
                    password = "Admin1234",
                    rol = RolUsuario.ADMINISTRADOR.name,
                    tallerGrupo = "Coordinación Huerto Escolar"
                ),
                UsuarioEntity(
                    id = 2,
                    codigoFicticio = "EST-01",
                    nombreFicticio = "Mateo Vega (Ficticio)",
                    correoFicticio = "mateo.est@demo.cl",
                    password = "Est1234",
                    rol = RolUsuario.COLABORADOR.name,
                    tallerGrupo = "Taller 5° Básico A"
                ),
                UsuarioEntity(
                    id = 3,
                    codigoFicticio = "EST-02",
                    nombreFicticio = "Sofía Mora (Ficticia)",
                    correoFicticio = "sofia.est@demo.cl",
                    password = "Est1234",
                    rol = RolUsuario.COLABORADOR.name,
                    tallerGrupo = "Taller 6° Básico B"
                ),
                UsuarioEntity(
                    id = 4,
                    codigoFicticio = "EST-03",
                    nombreFicticio = "Lucas Soto (Ficticio)",
                    correoFicticio = "lucas.est@demo.cl",
                    password = "Est1234",
                    rol = RolUsuario.COLABORADOR.name,
                    tallerGrupo = "Taller 7° Básico A"
                )
            )
            db.usuarioDao().insertarTodos(usuarios)

            // 2. Sectores Ficticios
            val s1 = db.sectorDao().insertar(
                SectorEntity(
                    id = 1,
                    codigo = "SEC-NORTE",
                    nombre = "Bancal Norte - Hortalizas",
                    descripcion = "Bancal elevado con exposición solar directa de mañana.",
                    ubicacion = "Patio Interior Norte",
                    estado = EstadoSector.EN_USO.name,
                    responsableId = 1,
                    fechaCreacion = hoy.minusDays(30).toString(),
                    fechaActualizacion = hoy.minusDays(2).toString()
                )
            )
            val s2 = db.sectorDao().insertar(
                SectorEntity(
                    id = 2,
                    codigo = "SEC-INV",
                    nombre = "Invernadero Almácigos",
                    descripcion = "Espacio protegido para germinación y plantas aromáticas.",
                    ubicacion = "Costado Sala de Ciencias",
                    estado = EstadoSector.EN_USO.name,
                    responsableId = 1,
                    fechaCreacion = hoy.minusDays(45).toString(),
                    fechaActualizacion = hoy.minusDays(1).toString()
                )
            )
            db.sectorDao().insertar(
                SectorEntity(
                    id = 3,
                    codigo = "SEC-COMP",
                    nombre = "Zona Compostaje y Suelo",
                    descripcion = "Área de preparación de sustrato orgánico escolar.",
                    ubicacion = "Patio Sur",
                    estado = EstadoSector.EN_MANTENIMIENTO.name,
                    responsableId = 1,
                    fechaCreacion = hoy.minusDays(60).toString(),
                    fechaActualizacion = hoy.toString()
                )
            )

            // 3. Cultivos Ficticios
            val c1 = db.cultivoDao().insertar(
                CultivoEntity(
                    id = 1,
                    nombreComun = "Lechuga Costina",
                    descripcion = "Hortaliza de hoja verde sembrada por estudiantes de 5° Básico.",
                    sectorId = s1,
                    fechaSiembra = hoy.minusDays(25).toString(),
                    fechaTrasplante = hoy.minusDays(12).toString(),
                    fechaEstimadaCosecha = hoy.plusDays(15).toString(),
                    frecuenciaRiegoDias = 2,
                    estadoDesarrollo = EstadoCultivo.EN_CRECIMIENTO.name,
                    observaciones = "Buen vigor foliar, mantener humedad constante."
                )
            )
            val c2 = db.cultivoDao().insertar(
                CultivoEntity(
                    id = 2,
                    nombreComun = "Tomate Cherry",
                    descripcion = "Cultivo tutorado en bancal norte.",
                    sectorId = s1,
                    fechaSiembra = hoy.minusDays(40).toString(),
                    fechaTrasplante = hoy.minusDays(20).toString(),
                    fechaEstimadaCosecha = hoy.plusDays(5).toString(),
                    frecuenciaRiegoDias = 3,
                    estadoDesarrollo = EstadoCultivo.LISTO_PARA_COSECHAR.name,
                    observaciones = "Primeros racimos maduros listos para recolección."
                )
            )
            val c3 = db.cultivoDao().insertar(
                CultivoEntity(
                    id = 3,
                    nombreComun = "Albahaca y Menta",
                    descripcion = "Hierbas aromáticas en almácigos educativos.",
                    sectorId = s2,
                    fechaSiembra = hoy.minusDays(15).toString(),
                    fechaEstimadaCosecha = hoy.plusDays(30).toString(),
                    frecuenciaRiegoDias = 2,
                    estadoDesarrollo = EstadoCultivo.SEMBRADO.name,
                    observaciones = "Proteger de corrientes de aire frío."
                )
            )

            // 4. Tareas Ficticias (Pendiente, Atrasada, Pendiente de validación y Validada)
            db.tareaDao().insertar(
                TareaEntity(
                    id = 1,
                    tipoTarea = TipoTarea.RIEGO.name,
                    descripcion = "Regar suavemente el bancal de lechugas con regadera de lluvia fina (2 litros por metro).",
                    sectorId = s1,
                    cultivoId = c1,
                    fechaCreacion = hoy.minusDays(2).toString(),
                    fechaProgramada = hoy.toString(),
                    responsableId = 2, // Mateo
                    creadorId = 1,
                    estado = EstadoTarea.PENDIENTE.name
                )
            )
            db.tareaDao().insertar(
                TareaEntity(
                    id = 2,
                    tipoTarea = TipoTarea.RETIRO_MALEZAS.name,
                    descripcion = "Retirar malezas alrededor de los tutores de tomate cherry sin dañar raíces.",
                    sectorId = s1,
                    cultivoId = c2,
                    fechaCreacion = hoy.minusDays(5).toString(),
                    fechaProgramada = hoy.minusDays(2).toString(), // Atrasada
                    responsableId = 2, // Mateo
                    creadorId = 1,
                    estado = EstadoTarea.PENDIENTE.name
                )
            )
            val t3 = db.tareaDao().insertar(
                TareaEntity(
                    id = 3,
                    tipoTarea = TipoTarea.OBSERVACION_CULTIVO.name,
                    descripcion = "Revisar brotes de albahaca y registrar altura aproximada.",
                    sectorId = s2,
                    cultivoId = c3,
                    fechaCreacion = hoy.minusDays(3).toString(),
                    fechaProgramada = hoy.minusDays(1).toString(),
                    responsableId = 3, // Sofía
                    creadorId = 1,
                    estado = EstadoTarea.PENDIENTE_VALIDACION.name,
                    fechaHoraRealizacion = "${hoy.minusDays(1)} 11:30",
                    comentarioEstudiante = "Se observaron brotes sanos de 8 cm sin manchas en hojas."
                )
            )
            val t4 = db.tareaDao().insertar(
                TareaEntity(
                    id = 4,
                    tipoTarea = TipoTarea.LIMPIEZA.name,
                    descripcion = "Ordenar herramientas y limpiar pasillos del invernadero.",
                    sectorId = s2,
                    cultivoId = null,
                    fechaCreacion = hoy.minusDays(6).toString(),
                    fechaProgramada = hoy.minusDays(4).toString(),
                    responsableId = 4, // Lucas
                    creadorId = 1,
                    estado = EstadoTarea.VALIDADA.name,
                    fechaHoraRealizacion = "${hoy.minusDays(4)} 15:10",
                    comentarioEstudiante = "Pasillos barridos y regaderas guardadas."
                )
            )
            db.validacionDao().insertar(
                ValidacionEntity(
                    tareaId = t4,
                    validadorId = 1,
                    resultado = "APROBADA",
                    comentario = "Excelente orden y cuidado del espacio compartido.",
                    fechaHoraValidacion = "${hoy.minusDays(4)} 16:00"
                )
            )

            // 5. Reporte de Problema Ficticio
            db.reporteProblemaDao().insertar(
                ReporteProblemaEntity(
                    sectorId = s1,
                    cultivoId = c1,
                    tipoProblema = TipoProblema.FALTA_AGUA.name,
                    descripcion = "Tierra seca en el extremo derecho del bancal norte tras jornada soleada.",
                    fechaHoraReporte = "${hoy.minusDays(1)} 14:20",
                    reportadoPorId = 2,
                    estado = EstadoReporte.INFORMADO.name
                )
            )

            // 6. Lecturas Simuladas de Sensores (Claramente etiquetadas como simulación académica)
            db.lecturaSensorDao().insertarTodas(
                listOf(
                    LecturaSensorEntity(
                        sectorId = s1,
                        fechaHoraLectura = "$hoy 09:00",
                        humedadSueloPct = 34,
                        temperaturaC = 23.5,
                        humedadAmbientalPct = 48,
                        origen = "Simulación académica (dato de prueba)",
                        estadoLectura = "Bajo"
                    ),
                    LecturaSensorEntity(
                        sectorId = s2,
                        fechaHoraLectura = "$hoy 09:00",
                        humedadSueloPct = 62,
                        temperaturaC = 21.0,
                        humedadAmbientalPct = 65,
                        origen = "Simulación académica (dato de prueba)",
                        estadoLectura = "Normal"
                    )
                )
            )
        }
    }
}