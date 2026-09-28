package com.dvt.medicimapp.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import kotlin.coroutines.resumeWithException
import com.dvt.medicimapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


// ============================================================
// CLIENTE HTTP
// ============================================================

object ApiService {

    /**
     * Backend oficial MoskiCheck desplegado en Railway.
     *
     * La aplicación Android se comunica exclusivamente
     * mediante HTTPS con el Backend de producción.
     *
     * Android
     *      ↓ HTTPS
     * Backend MoskiCheck - Railway
     *      ↓
     * MoskiCheck IA
     *      ↓
     * MySQL
     */
    private const val BASE_URL =
        "https://mi-app-backend-production-01e4.up.railway.app/api"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()


    // ========================================================
    // GET /api/reportes
    // ========================================================

    suspend fun obtenerReportes(): Result<List<ReporteApi>> =
        withContext(Dispatchers.IO) {

            try {

                val request = Request.Builder()
                    .url("$BASE_URL/reportes")
                    .get()
                    .build()

                executeRequest(request).let { response ->

                    val body = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }

                    val array = json.getJSONArray("data")
                    val reportes = mutableListOf<ReporteApi>()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)

                        reportes.add(
                            ReporteApi(
                                id = obj.getInt("id"),
                                nombre = obj.optString(
                                    "nombre",
                                    "Anónimo"
                                ),
                                distrito = obj.getString("distrito"),
                                barrio = obj.nullableString("barrio"),
                                tipoCriadero = obj.getString(
                                    "tipo_criadero"
                                ),
                                descripcion = obj.nullableString("descripcion"),
                                fechaHora = obj.getString(
                                    "fecha_hora"
                                ),
                                esAnonimo = obj.optInt(
                                    "es_anonimo",
                                    0
                                ) == 1,
                                estado = obj.getString(
                                    "estado"
                                )
                            )
                        )
                    }

                    Result.success(reportes)
                }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }


    // ========================================================
    // GET /api/reportes/por-distrito
    // ========================================================

    suspend fun obtenerReportesPorDistrito():
            Result<List<ReporteZonaApi>> =
        withContext(Dispatchers.IO) {

            try {

                val request = Request.Builder()
                    .url("$BASE_URL/reportes/por-distrito")
                    .get()
                    .build()

                executeRequest(request).let { response ->

                    val body = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }

                    val array = json.getJSONArray("data")
                    val zonas = mutableListOf<ReporteZonaApi>()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)

                        zonas.add(
                            ReporteZonaApi(
                                distrito = obj.getString(
                                    "distrito"
                                ),
                                totalReportes = obj.getInt(
                                    "total_reportes"
                                ),
                                recipiente = obj.optInt(
                                    "recipiente",
                                    0
                                ),
                                llanta = obj.optInt(
                                    "llanta",
                                    0
                                ),
                                acequia = obj.optInt(
                                    "acequia",
                                    0
                                ),
                                maleza = obj.optInt(
                                    "maleza",
                                    0
                                ),
                                basura = obj.optInt(
                                    "basura",
                                    0
                                )
                            )
                        )
                    }

                    Result.success(zonas)
                }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }


    // ========================================================
    // GET /api/reportes/total
    // ========================================================

    suspend fun obtenerTotales(): Result<TotalesApi> =
        withContext(Dispatchers.IO) {

            try {

                val request = Request.Builder()
                    .url("$BASE_URL/reportes/total")
                    .get()
                    .build()

                executeRequest(request).let { response ->

                    val body = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }

                    val data = json.getJSONObject("data")

                    Result.success(
                        TotalesApi(
                            totalReportes = data.getInt(
                                "total_reportes"
                            ),
                            totalDistritos = data.getInt(
                                "total_distritos"
                            )
                        )
                    )
                }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }


    // ========================================================
    // GET /api/alertas/hoy
    // ========================================================

    suspend fun obtenerAlertaHoy(
        distrito: String
    ): Result<AlertaApi> =
        withContext(Dispatchers.IO) {

            try {

                val request = Request.Builder()
                    .url("$BASE_URL/alertas/hoy".toHttpUrl().newBuilder()
                        .addQueryParameter("distrito", distrito).build())
                    .get()
                    .build()

                executeRequest(request).let { response ->

                    val body = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }

                    val data = json.getJSONObject("data")
                    val nivel = data.getString("nivel")
                    if (nivel !in setOf("bajo", "moderado", "alto", "critico")) {
                        throw ApiError.InvalidResponse()
                    }

                    Result.success(
                        AlertaApi(
                            distrito = data.optString(
                                "distrito",
                                distrito
                            ),
                            nivel = nivel,
                            totalReportes = data.getInt("total_reportes"),
                            descripcion = data.optString(
                                "descripcion",
                                "Sin reportes recientes"
                            )
                        )
                    )
                }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }


    // ========================================================
    // POST /api/reportes
    // ========================================================

    suspend fun enviarReporte(
        nombre: String?,
        distrito: String,
        barrio: String?,
        tipoCriadero: String,
        descripcion: String?,
        esAnonimo: Boolean
    ): Result<Int> =
        withContext(Dispatchers.IO) {

            try {

                val jsonBody = JSONObject().apply {

                    put(
                        "nombre",
                        if (esAnonimo) {
                            "Anónimo"
                        } else {
                            nombre ?: "Anónimo"
                        }
                    )

                    put(
                        "distrito",
                        distrito
                    )

                    put(
                        "barrio",
                        barrio ?: ""
                    )

                    put(
                        "tipo_criadero",
                        tipoCriadero
                    )

                    put(
                        "descripcion",
                        descripcion ?: ""
                    )

                    put(
                        "es_anonimo",
                        esAnonimo
                    )
                }

                val body = jsonBody
                    .toString()
                    .toRequestBody(
                        "application/json".toMediaType()
                    )

                val request = Request.Builder()
                    .url("$BASE_URL/reportes")
                    .post(body)
                    .build()

                executeRequest(request).let { response ->

                    val respBody = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )

                    val json = JSONObject(respBody)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }

                    Result.success(
                        json.getInt("id")
                    )
                }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }


    // ========================================================
    // POST /api/diagnosticos/analizar
    // MOSKICHECK V3
    // ========================================================

    suspend fun analizarSintomas(
        evaluacion: EvaluacionMoskiCheckRequest
    ): Result<ResultadoMoskiCheckApi> =
        withContext(Dispatchers.IO) {

            try {

                // ------------------------------------------------
                // Construir contrato EXACTO del Backend V3
                // ------------------------------------------------

                val jsonBody = JSONObject().apply {

                    // 11 síntomas del modelo
                    put(
                        "sudden_fever",
                        evaluacion.suddenFever
                    )

                    put(
                        "headache",
                        evaluacion.headache
                    )

                    put(
                        "muscle_pain",
                        evaluacion.musclePain
                    )

                    put(
                        "joint_pain",
                        evaluacion.jointPain
                    )

                    put(
                        "vomiting",
                        evaluacion.vomiting
                    )

                    put(
                        "rash",
                        evaluacion.rash
                    )

                    put(
                        "nausea",
                        evaluacion.nausea
                    )

                    put(
                        "fatigue",
                        evaluacion.fatigue
                    )

                    put(
                        "orbital_pain",
                        evaluacion.orbitalPain
                    )

                    put(
                        "red_eyes",
                        evaluacion.redEyes
                    )

                    put(
                        "swelling",
                        evaluacion.swelling
                    )


                    // 3 señales de seguridad
                    put(
                        "dolor_abdominal_intenso",
                        evaluacion.dolorAbdominalIntenso
                    )

                    put(
                        "sangrado",
                        evaluacion.sangrado
                    )

                    put(
                        "vomitos_persistentes",
                        evaluacion.vomitosPersistentes
                    )
                }


                val requestBody = jsonBody
                    .toString()
                    .toRequestBody(
                        "application/json".toMediaType()
                    )


                val request = Request.Builder()
                    .url(
                        "$BASE_URL/diagnosticos/analizar"
                    )
                    .post(requestBody)
                    .build()


                executeRequest(request).let { response ->

                    val responseBody = response.body
                        ?: return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )


                    val json = JSONObject(responseBody)


                    // ------------------------------------------------
                    // Validar respuesta del Backend Node.js
                    // ------------------------------------------------

                    if (
                        !response.isSuccessful ||
                        !json.optBoolean("ok", false)
                    ) {

                        return@withContext Result.failure(
                            ApiError.Rejected(obtenerMensajeError(json))
                        )
                    }


                    if (
                        !json.has(
                            "resultado_inteligencia_artificial"
                        )
                    ) {

                        return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )
                    }


                    val resultadoJson =
                        json.getJSONObject(
                            "resultado_inteligencia_artificial"
                        )


                    // ------------------------------------------------
                    // Status
                    // ------------------------------------------------

                    val status =
                        resultadoJson.optString(
                            "status",
                            ""
                        )


                    if (
                        status != "orientation" &&
                        status != "inconclusive" &&
                        status != "alert"
                    ) {

                        return@withContext Result.failure(
                            ApiError.InvalidResponse()
                        )
                    }


                    // ------------------------------------------------
                    // Prediction nullable
                    // ------------------------------------------------

                    val prediction =
                        if (
                            resultadoJson.has("prediction") &&
                            !resultadoJson.isNull("prediction")
                        ) {

                            resultadoJson.getString(
                                "prediction"
                            )

                        } else {

                            null
                        }


                    // ------------------------------------------------
                    // Confidence nullable
                    // ------------------------------------------------

                    val confidence =
                        if (
                            resultadoJson.has("confidence") &&
                            !resultadoJson.isNull("confidence")
                        ) {

                            resultadoJson.getDouble(
                                "confidence"
                            )

                        } else {

                            null
                        }


                    // ------------------------------------------------
                    // Probabilities nullable
                    // ------------------------------------------------

                    val probabilities =
                        if (
                            resultadoJson.has("probabilities") &&
                            !resultadoJson.isNull("probabilities")
                        ) {

                            val probabilitiesJson =
                                resultadoJson.getJSONObject(
                                    "probabilities"
                                )

                            ProbabilidadesMoskiCheckApi(
                                chikungunya =
                                    probabilitiesJson.getDouble("chikungunya"),

                                dengue =
                                    probabilitiesJson.getDouble("dengue"),

                                zika =
                                    probabilitiesJson.getDouble("zika")
                            )

                        } else {

                            null
                        }


                    // ------------------------------------------------
                    // Alertas detectadas
                    // ------------------------------------------------

                    val alertsDetected =
                        mutableListOf<String>()


                    if (
                        resultadoJson.has("alerts_detected") &&
                        !resultadoJson.isNull("alerts_detected")
                    ) {

                        val alertsJson =
                            resultadoJson.getJSONArray(
                                "alerts_detected"
                            )


                        for (
                        i in 0 until alertsJson.length()
                        ) {

                            alertsDetected.add(
                                alertsJson.getString(i)
                            )
                        }
                    }


                    // ------------------------------------------------
                    // Resultado final para la aplicación
                    // ------------------------------------------------

                    val resultado =
                        ResultadoMoskiCheckApi(

                            status = status,

                            prediction = prediction,

                            confidence = confidence,

                            probabilities = probabilities,

                            symptomCount =
                                resultadoJson.optInt(
                                    "symptom_count",
                                    0
                                ),

                            alertsDetected =
                                alertsDetected,

                            modelVersion =
                                if (
                                    resultadoJson.has(
                                        "model_version"
                                    ) &&
                                    !resultadoJson.isNull(
                                        "model_version"
                                    )
                                ) {

                                    resultadoJson.getString(
                                        "model_version"
                                    )

                                } else {

                                    null
                                },

                            message =
                                resultadoJson.optString(
                                    "message",
                                    ""
                                )
                        )


                    Result.success(resultado)
                }


            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {

                Result.failure(ApiError.from(e))
            }
        }



    internal data class ResponseData(val code: Int, val body: String?) {
        val isSuccessful: Boolean get() = code in 200..299
    }

    /** Read and close the body before completing the continuation: cancellation cancels the call. */
    internal suspend fun executeRequest(
        request: Request,
        httpClient: OkHttpClient = client
    ): ResponseData = suspendCancellableCoroutine { continuation ->
        val call = httpClient.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(ApiError.from(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val payload = response.use {
                        if (!it.isSuccessful) throw ApiError.Http(it.code)
                        val body = it.body?.string()?.takeIf { text -> text.isNotBlank() }
                            ?: throw ApiError.InvalidResponse()
                        ResponseData(it.code, body)
                    }
                    continuation.resumeWith(Result.success(payload))
                } catch (error: Exception) {
                    continuation.resumeWithException(ApiError.from(error))
                }
            }
        })
    }

    // ========================================================
    // UTILIDAD PARA MENSAJES DE ERROR
    // ========================================================

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun obtenerMensajeError(
        json: JSONObject
    ): String {

        return when {

            json.has("detalle") &&
                    !json.isNull("detalle") -> {

                json.optString(
                    "detalle",
                    "Error en el servidor."
                )
            }


            json.has("error") &&
                    !json.isNull("error") -> {

                json.optString(
                    "error",
                    "Error en el servidor."
                )
            }


            json.has("mensaje") &&
                    !json.isNull("mensaje") -> {

                json.optString(
                    "mensaje",
                    "Error en el servidor."
                )
            }


            else -> {

                "No fue posible completar la solicitud."
            }
        }
    }
}
