package com.duoc.huertoescolar.data.local.dao

import androidx.room.*
import com.duoc.huertoescolar.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE LOWER(correoFicticio) = LOWER(:correo) AND password = :password AND activo = 1 LIMIT 1")
    suspend fun login(correo: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE activo = 1 ORDER BY rol ASC, nombreFicticio ASC")
    fun observarUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)
}

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectores ORDER BY nombre ASC")
    fun observarSectores(): Flow<List<SectorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sector: SectorEntity): Long

    @Update
    suspend fun actualizar(sector: SectorEntity)

    @Delete
    suspend fun eliminar(sector: SectorEntity)
}

@Dao
interface CultivoDao {
    @Query("SELECT * FROM cultivos ORDER BY fechaSiembra DESC")
    fun observarCultivos(): Flow<List<CultivoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cultivo: CultivoEntity): Long

    @Update
    suspend fun actualizar(cultivo: CultivoEntity)

    @Delete
    suspend fun eliminar(cultivo: CultivoEntity)
}

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas ORDER BY fechaProgramada ASC")
    fun observarTareas(): Flow<List<TareaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: TareaEntity): Long

    @Update
    suspend fun actualizar(tarea: TareaEntity)

    @Delete
    suspend fun eliminar(tarea: TareaEntity)
}

@Dao
interface EvidenciaDao {
    @Query("SELECT * FROM evidencias ORDER BY fechaCaptura DESC")
    fun observarEvidencias(): Flow<List<EvidenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evidencia: EvidenciaEntity): Long
}

@Dao
interface ValidacionDao {
    @Query("SELECT * FROM validaciones ORDER BY fechaHoraValidacion DESC")
    fun observarValidaciones(): Flow<List<ValidacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(validacion: ValidacionEntity): Long
}

@Dao
interface ReporteProblemaDao {
    @Query("SELECT * FROM reportes_problemas ORDER BY fechaHoraReporte DESC")
    fun observarReportes(): Flow<List<ReporteProblemaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(reporte: ReporteProblemaEntity): Long

    @Update
    suspend fun actualizar(reporte: ReporteProblemaEntity)
}

@Dao
interface LecturaSensorDao {
    @Query("SELECT * FROM lecturas_sensores_simuladas ORDER BY fechaHoraLectura DESC")
    fun observarLecturas(): Flow<List<LecturaSensorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lecturas: List<LecturaSensorEntity>)
}