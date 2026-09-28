package com.dvt.medicimapp.data.model

import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException
import org.json.JSONException

sealed class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Timeout(cause: Throwable? = null) : ApiError("El servidor tardó demasiado en responder. Intenta nuevamente.", cause)
    class Connection(cause: Throwable? = null) : ApiError("No se pudo conectar con el servidor. Verifica tu conexión.", cause)
    class Http(val code: Int) : ApiError("El servidor no pudo completar la solicitud (HTTP $code).")
    class InvalidResponse(cause: Throwable? = null) : ApiError("El servidor devolvió una respuesta inválida. Intenta nuevamente.", cause)
    class Rejected(message: String) : ApiError(message)

    companion object {
        fun from(error: Throwable): ApiError = when (error) {
            is CancellationException -> throw error
            is ApiError -> error
            is InterruptedIOException -> Timeout(error)
            is IOException -> Connection(error)
            is JSONException, is IllegalArgumentException, is IllegalStateException -> InvalidResponse(error)
            else -> InvalidResponse(error)
        }
    }
}
