package com.duoc.huertoescolar.data.remote

import retrofit2.Response
import retrofit2.http.*

interface CatalogoApiService {
    @GET("api/catalogo/usuarios")
    suspend fun getUsuarios(): List<UsuarioDto>

    @GET("api/catalogo/sectores")
    suspend fun getSectores(): List<SectorDto>

    @POST("api/catalogo/sectores")
    suspend fun crearSector(@Body sector: SectorDto): SectorDto

    @PUT("api/catalogo/sectores/{id}")
    suspend fun actualizarSector(@Path("id") id: Long, @Body sector: SectorDto): SectorDto

    @DELETE("api/catalogo/sectores/{id}")
    suspend fun eliminarSector(@Path("id") id: Long): Response<Unit>

    @GET("api/catalogo/cultivos")
    suspend fun getCultivos(): List<CultivoDto>

    @POST("api/catalogo/cultivos")
    suspend fun crearCultivo(@Body cultivo: CultivoDto): CultivoDto

    @PUT("api/catalogo/cultivos/{id}")
    suspend fun actualizarCultivo(@Path("id") id: Long, @Body cultivo: CultivoDto): CultivoDto
}

interface OperacionesApiService {
    @GET("api/operaciones/tareas")
    suspend fun getTareas(): List<TareaDto>

    @POST("api/operaciones/tareas")
    suspend fun crearTarea(@Body tarea: TareaDto): TareaDto

    @PUT("api/operaciones/tareas/{id}")
    suspend fun actualizarTarea(@Path("id") id: Long, @Body tarea: TareaDto): TareaDto

    @GET("api/operaciones/validaciones")
    suspend fun getValidaciones(): List<ValidacionDto>

    @POST("api/operaciones/validaciones")
    suspend fun registrarValidacion(@Body validacion: ValidacionDto): Response<ValidacionDto>

    @GET("api/operaciones/reportes")
    suspend fun getReportes(): List<ReporteProblemaDto>

    @POST("api/operaciones/reportes")
    suspend fun crearReporte(@Body reporte: ReporteProblemaDto): ReporteProblemaDto

    @PUT("api/operaciones/reportes/{id}")
    suspend fun actualizarReporte(@Path("id") id: Long, @Body reporte: ReporteProblemaDto): ReporteProblemaDto
}

interface ClimaApiService {
    // Coordenadas de San Bernardo, Región Metropolitana: -33.5922, -70.6996
    @GET("v1/forecast")
    suspend fun getClimaActual(
        @Query("latitude") lat: Double = -33.5922,
        @Query("longitude") lon: Double = -70.6996,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}