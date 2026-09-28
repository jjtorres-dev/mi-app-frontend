package com.dvt.medicimapp.data

import com.dvt.medicimapp.data.model.ApiError
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test

class ApiServiceHttpTest {
    @Test fun successfulResponsePreservesBody() = withServer(200, "{\"ok\":true,\"data\":[]}") { url ->
        val response = runBlocking { ApiService.executeRequest(Request.Builder().url(url).build()) }
        assertEquals(200, response.code)
        assertEquals("{\"ok\":true,\"data\":[]}", response.body)
    }

    @Test fun httpFailurePreservesStatusWithoutPublishingServerDetails() = withServer(503, "internal details") { url ->
        val error = runCatching { runBlocking { ApiService.executeRequest(Request.Builder().url(url).build()) } }.exceptionOrNull()
        assertTrue(error is ApiError.Http)
        assertEquals(503, (error as ApiError.Http).code)
        assertFalse(error.message.orEmpty().contains("internal details"))
    }

    @Test fun emptySuccessIsAnInvalidResponse() = withServer(200, "") { url ->
        val error = runCatching { runBlocking { ApiService.executeRequest(Request.Builder().url(url).build()) } }.exceptionOrNull()
        assertTrue(error is ApiError.InvalidResponse)
    }

    @Test fun connectionFailuresAndTimeoutsHaveDistinctTypes() {
        assertTrue(ApiError.from(java.net.ConnectException()) is ApiError.Connection)
        assertTrue(ApiError.from(java.net.SocketTimeoutException()) is ApiError.Timeout)
        assertTrue(ApiError.from(IllegalArgumentException()) is ApiError.InvalidResponse)
    }

    @Test fun slowResponseProducesTimeout() = stalledServer { url, _ ->
        val client = OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS).build()
        val error = runCatching { runBlocking { ApiService.executeRequest(Request.Builder().url(url).build(), client) } }.exceptionOrNull()
        assertTrue(error is ApiError.Timeout)
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    @Test fun coroutineCancellationCancelsPendingHttp() = stalledServer { url, accepted ->
        runBlocking {
            val request = async { ApiService.executeRequest(Request.Builder().url(url).build()) }
            withTimeout(3000) { accepted.await() }
            request.cancelAndJoin()
            assertTrue(request.isCancelled)
        }
    }

    private fun withServer(status: Int, body: String, check: (String) -> Unit) {
        ServerSocket(0).use { server ->
            val worker = thread(isDaemon = true) {
                server.accept().use { socket ->
                    val reader = socket.getInputStream().bufferedReader()
                    while (!reader.readLine().isNullOrEmpty()) Unit
                    val data = body.toByteArray(Charsets.UTF_8)
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 $status Test\r\nContent-Length: ${data.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        write(data)
                        flush()
                    }
                }
            }
            check("http://127.0.0.1:${server.localPort}/test")
            worker.join(3000)
        }
    }

    private fun stalledServer(check: (String, CompletableDeferred<Unit>) -> Unit) {
        ServerSocket(0).use { server ->
            val accepted = CompletableDeferred<Unit>()
            val finished = java.util.concurrent.CountDownLatch(1)
            val worker = thread(isDaemon = true) {
                server.accept().use {
                    accepted.complete(Unit)
                    finished.await(5, TimeUnit.SECONDS)
                }
            }
            try { check("http://127.0.0.1:${server.localPort}/test", accepted) }
            finally { finished.countDown(); worker.join(3000) }
        }
    }
}
