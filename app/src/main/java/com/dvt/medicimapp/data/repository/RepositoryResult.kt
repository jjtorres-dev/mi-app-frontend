package com.dvt.medicimapp.data.repository

import kotlinx.coroutines.CancellationException
import com.dvt.medicimapp.data.model.ApiError

internal suspend fun <T> repositoryResult(call: suspend () -> Result<T>): Result<T> = try {
    val result = call()
    val failure = result.exceptionOrNull()
    if (failure is CancellationException) throw failure
    if (failure == null) result else Result.failure(ApiError.from(failure))
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(ApiError.from(error))
}
