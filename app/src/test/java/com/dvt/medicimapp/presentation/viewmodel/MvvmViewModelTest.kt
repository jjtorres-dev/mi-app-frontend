package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.dvt.medicimapp.data.model.*
import com.dvt.medicimapp.data.repository.*
import com.dvt.medicimapp.presentation.state.LoadStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Test

class MvvmViewModelTest {
    private val dispatcher = Dispatchers.Unconfined

    @Test fun homeKeepsTarapotoAndStatisticsWhenAlertFails() {
        var district: String? = null
        val repository = object : HomeRepository {
            override suspend fun obtenerTotales() = Result.success(TotalesApi(23, 5))
            override suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi> {
                district = distrito
                return Result.failure(ApiError.Connection())
            }
        }
        val state = HomeViewModel(repository, dispatcher).uiState.value
        assertEquals("Tarapoto", district)
        assertEquals(LoadStatus.Error, state.alertaStatus)
        assertNull(state.alerta)
        assertEquals(LoadStatus.Success, state.totalesStatus)
        assertEquals(23, state.totales?.totalReportes)
    }

    @Test fun homeRetryReplacesErrorWithSuccessfulAlert() {
        var fail = true
        val repository = object : HomeRepository {
            override suspend fun obtenerTotales() = Result.success(TotalesApi(23, 5))
            override suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi> =
                if (fail) Result.failure(ApiError.Timeout())
                else Result.success(AlertaApi(distrito, "alto", 5, "Alerta de prueba"))
        }
        val vm = HomeViewModel(repository, dispatcher)
        fail = false
        vm.cargar()
        assertEquals(LoadStatus.Success, vm.uiState.value.alertaStatus)
        assertEquals("alto", vm.uiState.value.alerta?.nivel)
        assertNull(vm.uiState.value.errorAlerta)
    }

    @Test fun reportsKeepRankingAndLimitRecentItemsToTen() {
        val vm = ReportsViewModel(FakeReports(), dispatcher)
        val state = vm.uiState.value
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(10, state.reportesRecientes.size)
        assertEquals(23, state.totalReportes)
        assertEquals("Tarapoto", state.zonas.single().distrito)
    }

    @Test fun failedStatisticsAreUnavailableInsteadOfZero() {
        val repository = FakeReports().apply { statistics = Result.failure(ApiError.Timeout()) }
        val state = ReportsViewModel(repository, dispatcher).uiState.value
        assertNull(state.totalReportes)
        assertNull(state.totalDistritos)
        assertNotNull(state.errorEstadisticas)
        assertEquals(10, state.reportesRecientes.size)
    }

    @Test fun reportsExposeRankingErrorsAndCanRetry() {
        val repository = FakeReports().apply { zones = Result.failure(ApiError.Connection()) }
        val vm = ReportsViewModel(repository, dispatcher)
        assertEquals("No se pudo cargar el ranking de zonas", vm.uiState.value.error)
        repository.zones = Result.success(emptyList())
        vm.cargar()
        assertNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.zonas.isEmpty())
    }

    @Test fun invalidFormNeverSends() {
        val repository = FakeReport()
        val vm = ReportViewModel(repository, SavedStateHandle(), dispatcher)
        vm.enviar()
        assertEquals(0, repository.calls)
        assertFalse(vm.uiState.value.formularioValido)
    }

    @Test fun anonymousReportUsesTheOriginalNullAndBooleanContract() {
        val repository = FakeReport()
        val vm = ReportViewModel(repository, SavedStateHandle(), dispatcher)
        vm.actualizarDistrito("Tarapoto")
        vm.actualizarTipoCriadero("Llanta/cubierta")
        vm.actualizarAnonimato(true)
        vm.enviar()
        assertEquals(1, repository.calls)
        assertNull(repository.name)
        assertNull(repository.neighborhood)
        assertNull(repository.description)
        assertTrue(repository.anonymous)
        assertTrue(vm.uiState.value.mostrarExito)
        assertFalse(vm.uiState.value.enviando)
    }

    @Test fun pendingSubmissionRejectsDoubleClicks() {
        val repository = FakeReport().apply { pending = CompletableDeferred() }
        val vm = ReportViewModel(repository, SavedStateHandle(), dispatcher)
        vm.actualizarDistrito("Tarapoto")
        vm.actualizarTipoCriadero("Llanta/cubierta")
        vm.actualizarAnonimato(true)
        vm.enviar()
        vm.enviar()
        assertEquals(1, repository.calls)
        assertTrue(vm.uiState.value.enviando)
        repository.pending!!.complete(Result.success(7))
        assertFalse(vm.uiState.value.enviando)
    }

    @Test fun reportRestoresFieldsAndPhotoWithoutSubmitting() {
        val handle = SavedStateHandle()
        val repository = FakeReport()
        val vm = ReportViewModel(repository, handle, dispatcher)
        vm.actualizarNombre("Usuario de prueba")
        vm.actualizarDistrito("Morales")
        vm.actualizarBarrio("Sector de prueba")
        vm.actualizarDescripcion("Descripción de prueba")
        vm.actualizarTipoCriadero("Llanta/cubierta")
        vm.actualizarFoto("content://test/foto")
        val restored = ReportViewModel(repository, handle, dispatcher).uiState.value
        assertEquals("Usuario de prueba", restored.nombre)
        assertEquals("Morales", restored.distrito)
        assertEquals("Sector de prueba", restored.barrio)
        assertEquals("Descripción de prueba", restored.descripcion)
        assertEquals("content://test/foto", restored.fotoUri)
        assertEquals(0, repository.calls)
    }

    @Test fun submissionErrorDoesNotBecomeSuccess() {
        val repository = FakeReport().apply { result = Result.failure(ApiError.Http(500)) }
        val vm = ReportViewModel(repository, SavedStateHandle(), dispatcher)
        vm.actualizarDistrito("Tarapoto")
        vm.actualizarTipoCriadero("Llanta/cubierta")
        vm.actualizarAnonimato(true)
        vm.enviar()
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.mostrarExito)
        assertFalse(vm.uiState.value.enviando)
        vm.cerrarError()
        assertNull(vm.uiState.value.error)
    }

    @Test fun moskiCheckSendsAllFourteenFieldsInTheOriginalOrder() {
        val repository = FakeMoski()
        val vm = MoskiCheckViewModel(repository, SavedStateHandle(), dispatcher)
        vm.iniciar()
        val answers = listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0)
        answers.forEachIndexed { index, answer ->
            vm.responder(answer)
            assertEquals(if (index == 13) 1 else 0, repository.calls)
        }
        assertEquals(EvaluacionMoskiCheckRequest(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0), repository.request)
        assertEquals("orientation", vm.uiState.value.resultado?.status)
    }

    @Test fun moskiCheckPreservesEachBackendResultStatus() {
        listOf("orientation", "inconclusive", "alert").forEach { status ->
            val repository = FakeMoski().apply { result = Result.success(evaluationResult(status)) }
            val vm = MoskiCheckViewModel(repository, SavedStateHandle(), dispatcher)
            vm.iniciar()
            repeat(14) { vm.responder(0) }
            assertEquals(status, vm.uiState.value.resultado?.status)
        }
    }

    @Test fun closingMoskiCheckClearsAnswersAndIgnoresOldResponse() {
        val pending = CompletableDeferred<Result<ResultadoMoskiCheckApi>>()
        val repository = FakeMoski().apply { this.pending = pending }
        val vm = MoskiCheckViewModel(repository, SavedStateHandle(), dispatcher)
        vm.iniciar()
        repeat(14) { vm.responder(1) }
        assertTrue(vm.uiState.value.cargando)
        vm.reiniciarEvaluacion()
        pending.complete(Result.success(evaluationResult("alert")))
        assertTrue(vm.uiState.value.respuestas.isEmpty())
        assertFalse(vm.uiState.value.iniciado)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.resultado)
    }

    @Test fun moskiCheckRestoresPartialQuestionnaireWithoutSending() {
        val handle = SavedStateHandle()
        val repository = FakeMoski()
        val vm = MoskiCheckViewModel(repository, handle, dispatcher)
        vm.iniciar()
        vm.responder(1)
        vm.responder(0)
        val state = MoskiCheckViewModel(repository, handle, dispatcher).uiState.value
        assertEquals(2, state.indicePregunta)
        assertEquals(2, state.respuestas.size)
        assertTrue(state.iniciado)
        assertEquals(0, repository.calls)
    }

    @Test fun guideKeepsTwelveRecommendationsAndRestoresItsFilter() {
        val repository = LocalGuideRepository()
        val handle = SavedStateHandle()
        val vm = GuideViewModel(repository, handle)
        assertEquals(12, vm.uiState.value.guias.size)
        vm.seleccionarCategoria("Personal")
        assertEquals(4, vm.uiState.value.guias.size)
        assertTrue(vm.uiState.value.guias.all { it.categoria == "Personal" })
        assertEquals("Personal", GuideViewModel(repository, handle).uiState.value.categoriaSeleccionada)
    }

    @Test fun healthCentersKeepsSixLocationsAndRestoresSelectedTab() {
        val repository = LocalHealthCentersRepository()
        val handle = SavedStateHandle()
        val vm = HealthCentersViewModel(repository, handle)
        assertEquals(6, vm.uiState.value.centros.size)
        assertEquals("Hospital II-2 Tarapoto", vm.uiState.value.centros.first().nombre)
        vm.seleccionarTab(1)
        assertEquals(1, HealthCentersViewModel(repository, handle).uiState.value.tabSeleccionado)
    }

    private class FakeReports : ReportsRepository {
        var zones: Result<List<ReporteZonaApi>> = Result.success(listOf(ReporteZonaApi("Tarapoto", 23, 1, 2, 3, 4, 5)))
        var statistics = Result.success(TotalesApi(23, 5))
        override suspend fun obtenerReportes() = Result.success((1..15).map {
            ReporteApi(it, "Usuario de prueba", "Tarapoto", null, "Llanta/cubierta", null, "2026-09-28", false, "pendiente")
        })
        override suspend fun obtenerReportesPorDistrito() = zones
        override suspend fun obtenerTotales() = statistics
    }

    private class FakeReport : ReportRepository {
        var calls = 0
        var name: String? = "unset"
        var neighborhood: String? = "unset"
        var description: String? = "unset"
        var anonymous = false
        var result = Result.success(7)
        var pending: CompletableDeferred<Result<Int>>? = null
        override suspend fun enviarReporte(nombre: String?, distrito: String, barrio: String?, tipoCriadero: String, descripcion: String?, esAnonimo: Boolean): Result<Int> {
            calls++
            name = nombre
            neighborhood = barrio
            description = descripcion
            anonymous = esAnonimo
            return pending?.await() ?: result
        }
    }

    private class FakeMoski : MoskiCheckRepository {
        override val preguntas = ApiMoskiCheckRepository().preguntas
        var calls = 0
        var request: EvaluacionMoskiCheckRequest? = null
        var result = Result.success(evaluationResult("orientation"))
        var pending: CompletableDeferred<Result<ResultadoMoskiCheckApi>>? = null
        override suspend fun analizarSintomas(evaluacion: EvaluacionMoskiCheckRequest): Result<ResultadoMoskiCheckApi> {
            calls++
            request = evaluacion
            return pending?.await() ?: result
        }
    }

    companion object {
        private fun evaluationResult(status: String) = ResultadoMoskiCheckApi(
            status, "dengue", 0.7, ProbabilidadesMoskiCheckApi(0.1, 0.7, 0.2),
            4, emptyList(), "v3", "Resultado de prueba"
        )
    }
}
