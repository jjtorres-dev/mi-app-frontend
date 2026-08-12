package com.dvt.medicimapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


// ============================================================
// MODELOS DE REPORTES
// ============================================================

data class ReporteApi(
    val id: Int,
    val nombre: String,
    val distrito: String,
    val barrio: String?,
    val tipoCriadero: String,
    val descripcion: String?,
    val fechaHora: String,
    val esAnonimo: Boolean,
    val estado: String
)

data class ReporteZonaApi(
    val distrito: String,
    val totalReportes: Int,
    val recipiente: Int,
    val llanta: Int,
    val acequia: Int,
    val maleza: Int,
    val basura: Int
)

data class AlertaApi(
    val distrito: String,
    val nivel: String,
    val totalReportes: Int,
    val descripcion: String
)

data class TotalesApi(
    val totalReportes: Int,
    val totalDistritos: Int
)


// ============================================================
// MODELOS MOSKICHECK V3
// ============================================================

/**
 * Solicitud oficial enviada por Android al Backend V3.
 *
 * 11 campos corresponden a síntomas utilizados por el modelo
 * de Machine Learning.
 *
 * 3 campos corresponden a señales de seguridad y NO son
 * utilizados como características del modelo.
 */
data class EvaluacionMoskiCheckRequest(

    // 11 síntomas del modelo
    val suddenFever: Int,
    val headache: Int,
    val musclePain: Int,
    val jointPain: Int,
    val vomiting: Int,
    val rash: Int,
    val nausea: Int,
    val fatigue: Int,
    val orbitalPain: Int,
    val redEyes: Int,
    val swelling: Int,

    // 3 señales de seguridad
    val dolorAbdominalIntenso: Int,
    val sangrado: Int,
    val vomitosPersistentes: Int
)


/**
 * Probabilidades generadas por el modelo.
 *
 * Pueden ser null cuando:
 *
 * - el resultado es "alert";
 * - hay menos de 3 síntomas.
 */
data class ProbabilidadesMoskiCheckApi(
    val chikungunya: Double,
    val dengue: Double,
    val zika: Double
)


/**
 * Resultado oficial devuelto por MoskiCheck IA V3,
 * pasando primero por el Backend Node.js.
 *
 * status puede ser:
 *
 * orientation
 * inconclusive
 * alert
 */
data class ResultadoMoskiCheckApi(
    val status: String,
    val prediction: String?,
    val confidence: Double?,
    val probabilities: ProbabilidadesMoskiCheckApi?,
    val symptomCount: Int,
    val alertsDetected: List<String>,
    val modelVersion: String?,
    val message: String
)


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

                client.newCall(request).execute().use { response ->

                    val body = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía")
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            Exception(obtenerMensajeError(json))
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
                                barrio = obj.optString(
                                    "barrio"
                                ),
                                tipoCriadero = obj.getString(
                                    "tipo_criadero"
                                ),
                                descripcion = obj.optString(
                                    "descripcion"
                                ),
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

            } catch (e: Exception) {

                Result.failure(e)
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

                client.newCall(request).execute().use { response ->

                    val body = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía")
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            Exception(obtenerMensajeError(json))
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

            } catch (e: Exception) {

                Result.failure(e)
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

                client.newCall(request).execute().use { response ->

                    val body = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía")
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception(obtenerMensajeError(json))
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

            } catch (e: Exception) {

                Result.failure(e)
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
                    .url("$BASE_URL/alertas/hoy?distrito=$distrito")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    val body = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía")
                        )

                    val json = JSONObject(body)

                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception(obtenerMensajeError(json))
                        )
                    }

                    val data = json.getJSONObject("data")

                    Result.success(
                        AlertaApi(
                            distrito = data.optString(
                                "distrito",
                                distrito
                            ),
                            nivel = data.optString(
                                "nivel",
                                "bajo"
                            ),
                            totalReportes = data.optInt(
                                "total_reportes",
                                0
                            ),
                            descripcion = data.optString(
                                "descripcion",
                                "Sin reportes recientes"
                            )
                        )
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
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

                client.newCall(request).execute().use { response ->

                    val respBody = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía")
                        )

                    val json = JSONObject(respBody)

                    if (!response.isSuccessful || !json.optBoolean("ok", false)) {
                        return@withContext Result.failure(
                            Exception(obtenerMensajeError(json))
                        )
                    }

                    Result.success(
                        json.getInt("id")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
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


                client.newCall(request).execute().use { response ->

                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(
                            Exception(
                                "El servidor devolvió una respuesta vacía."
                            )
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
                            Exception(
                                obtenerMensajeError(json)
                            )
                        )
                    }


                    if (
                        !json.has(
                            "resultado_inteligencia_artificial"
                        )
                    ) {

                        return@withContext Result.failure(
                            Exception(
                                "El servidor no devolvió el resultado de la evaluación."
                            )
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
                            Exception(
                                "El servidor devolvió un estado de evaluación desconocido."
                            )
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
                                    probabilitiesJson.optDouble(
                                        "chikungunya",
                                        0.0
                                    ),

                                dengue =
                                    probabilitiesJson.optDouble(
                                        "dengue",
                                        0.0
                                    ),

                                zika =
                                    probabilitiesJson.optDouble(
                                        "zika",
                                        0.0
                                    )
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


            } catch (e: Exception) {

                Result.failure(e)
            }
        }


    // ========================================================
    // UTILIDAD PARA MENSAJES DE ERROR
    // ========================================================

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