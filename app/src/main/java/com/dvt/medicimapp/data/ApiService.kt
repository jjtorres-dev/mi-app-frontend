package com.dvt.medicimapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// ── Modelos de datos ──────────────────────────────────────────────────────────

data class ReporteApi(
    val id:           Int,
    val nombre:       String,
    val distrito:     String,
    val barrio:       String?,
    val tipoCriadero: String,
    val descripcion:  String?,
    val fechaHora:    String,
    val esAnonimo:    Boolean,
    val estado:       String
)

data class ReporteZonaApi(
    val distrito:      String,
    val totalReportes: Int,
    val recipiente:    Int,
    val llanta:        Int,
    val acequia:       Int,
    val maleza:        Int,
    val basura:        Int
)

data class AlertaApi(
    val distrito:      String,
    val nivel:         String,
    val totalReportes: Int,
    val descripcion:   String
)

data class TotalesApi(
    val totalReportes:  Int,
    val totalDistritos: Int
)

// ── Cliente HTTP ──────────────────────────────────────────────────────────────

object ApiService {

    // 10.0.2.2 es localhost de tu PC visto desde el emulador
    private const val BASE_URL = "http://10.0.2.2:3000/api"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // ── GET /api/reportes ─────────────────────────────────────────────────────
    suspend fun obtenerReportes(): Result<List<ReporteApi>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/reportes")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body     = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
            val json     = JSONObject(body)

            if (!json.getBoolean("ok")) {
                return@withContext Result.failure(Exception(json.getString("mensaje")))
            }

            val array   = json.getJSONArray("data")
            val reportes = mutableListOf<ReporteApi>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                reportes.add(
                    ReporteApi(
                        id           = obj.getInt("id"),
                        nombre       = obj.optString("nombre", "Anónimo"),
                        distrito     = obj.getString("distrito"),
                        barrio       = obj.optString("barrio"),
                        tipoCriadero = obj.getString("tipo_criadero"),
                        descripcion  = obj.optString("descripcion"),
                        fechaHora    = obj.getString("fecha_hora"),
                        esAnonimo    = obj.getInt("es_anonimo") == 1,
                        estado       = obj.getString("estado")
                    )
                )
            }
            Result.success(reportes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── GET /api/reportes/por-distrito ────────────────────────────────────────
    suspend fun obtenerReportesPorDistrito(): Result<List<ReporteZonaApi>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/reportes/por-distrito")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body     = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
            val json     = JSONObject(body)

            if (!json.getBoolean("ok")) {
                return@withContext Result.failure(Exception(json.getString("mensaje")))
            }

            val array = json.getJSONArray("data")
            val zonas = mutableListOf<ReporteZonaApi>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                zonas.add(
                    ReporteZonaApi(
                        distrito      = obj.getString("distrito"),
                        totalReportes = obj.getInt("total_reportes"),
                        recipiente    = obj.optInt("recipiente", 0),
                        llanta        = obj.optInt("llanta", 0),
                        acequia       = obj.optInt("acequia", 0),
                        maleza        = obj.optInt("maleza", 0),
                        basura        = obj.optInt("basura", 0)
                    )
                )
            }
            Result.success(zonas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── GET /api/reportes/total ───────────────────────────────────────────────
    suspend fun obtenerTotales(): Result<TotalesApi> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/reportes/total")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body     = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
            val json     = JSONObject(body)
            val data     = json.getJSONObject("data")

            Result.success(
                TotalesApi(
                    totalReportes  = data.getInt("total_reportes"),
                    totalDistritos = data.getInt("total_distritos")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── GET /api/alertas/hoy ──────────────────────────────────────────────────
    suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/alertas/hoy?distrito=$distrito")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body     = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
            val json     = JSONObject(body)
            val data     = json.getJSONObject("data")

            Result.success(
                AlertaApi(
                    distrito      = data.optString("distrito", distrito),
                    nivel         = data.optString("nivel", "bajo"),
                    totalReportes = data.optInt("total_reportes", 0),
                    descripcion   = data.optString("descripcion", "Sin reportes recientes")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── POST /api/reportes ────────────────────────────────────────────────────
    suspend fun enviarReporte(
        nombre:       String?,
        distrito:     String,
        barrio:       String?,
        tipoCriadero: String,
        descripcion:  String?,
        esAnonimo:    Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("nombre",        if (esAnonimo) "Anónimo" else (nombre ?: "Anónimo"))
                put("distrito",      distrito)
                put("barrio",        barrio ?: "")
                put("tipo_criadero", tipoCriadero)
                put("descripcion",   descripcion ?: "")
                put("es_anonimo",    esAnonimo)
            }

            val body = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/reportes")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
            val json     = JSONObject(respBody)

            if (!json.getBoolean("ok")) {
                return@withContext Result.failure(Exception(json.getString("mensaje")))
            }

            Result.success(json.getInt("id"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}