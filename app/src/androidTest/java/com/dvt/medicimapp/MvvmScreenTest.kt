package com.dvt.medicimapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dvt.medicimapp.data.model.*
import com.dvt.medicimapp.data.repository.*
import com.dvt.medicimapp.presentation.screens.home.HomeScreen
import com.dvt.medicimapp.presentation.screens.home.MoskiCheckAssistantDialog
import com.dvt.medicimapp.presentation.screens.report.ReportScreen
import com.dvt.medicimapp.presentation.screens.reports.ReportsScreen
import com.dvt.medicimapp.presentation.viewmodel.*
import com.dvt.medicimapp.ui.theme.MedicimappTheme
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MvvmScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun homeConnectionErrorNeverDisplaysLowRisk() {
        val repository = object : HomeRepository {
            override suspend fun obtenerTotales() = Result.success(TotalesApi(23, 5))
            override suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi> = Result.failure(ApiError.Connection())
        }
        val vm = HomeViewModel(repository, Dispatchers.Unconfined)
        compose.setContent { MedicimappTheme { HomeScreen(rememberNavController(), vm) } }
        compose.onNodeWithText("No disponible").assertExists()
        compose.onNodeWithText("🟢 Riesgo bajo en tu zona").assertDoesNotExist()
    }

    @Test fun reportsRenderStatisticsRankingAndRecentReports() {
        val repository = object : ReportsRepository {
            override suspend fun obtenerTotales() = Result.success(TotalesApi(23, 5))
            override suspend fun obtenerReportesPorDistrito() = Result.success(listOf(ReporteZonaApi("Distrito de prueba", 23, 1, 2, 3, 4, 5)))
            override suspend fun obtenerReportes() = Result.success(listOf(ReporteApi(1, "Usuario de prueba", "Morales", null, "Llanta/cubierta", "Reporte de prueba", "2026-09-28", false, "pendiente")))
        }
        val vm = ReportsViewModel(repository, Dispatchers.Unconfined)
        compose.setContent { MedicimappTheme { ReportsScreen(rememberNavController(), vm) } }
        compose.onNodeWithText("Total reportes").assertExists()
        compose.onNodeWithText("23").assertExists()
        compose.onNodeWithText("Distrito de prueba").assertExists()
        compose.onNodeWithText("Usuario de prueba").assertExists()
    }

    @Test fun successfulReportShowsOriginalConfirmationWithoutWritingToRailway() {
        var calls = 0
        val repository = object : ReportRepository {
            override suspend fun enviarReporte(nombre: String?, distrito: String, barrio: String?, tipoCriadero: String, descripcion: String?, esAnonimo: Boolean): Result<Int> {
                calls++
                return Result.success(7)
            }
        }
        val vm = ReportViewModel(repository, SavedStateHandle(), Dispatchers.Unconfined)
        vm.actualizarNombre("Usuario de prueba")
        vm.actualizarDistrito("Tarapoto")
        vm.actualizarTipoCriadero("Llanta/cubierta")
        compose.setContent { MedicimappTheme { ReportScreen(rememberNavController(), vm) } }
        compose.onNodeWithText("Enviar reporte").assertIsEnabled().performClick()
        compose.onNodeWithText("Reporte enviado").assertIsDisplayed()
        compose.onNodeWithText("Aceptar").performClick()
        assertEquals(1, calls)
    }

    @Test fun questionnaireSendsOnceAndRendersTheOriginalResult() {
        var calls = 0
        val repository = object : MoskiCheckRepository {
            override val preguntas = ApiMoskiCheckRepository().preguntas
            override suspend fun analizarSintomas(evaluacion: EvaluacionMoskiCheckRequest): Result<ResultadoMoskiCheckApi> {
                calls++
                return Result.success(ResultadoMoskiCheckApi("inconclusive", null, null, null, 0, emptyList(), "v3", "Respuesta de prueba"))
            }
        }
        val vm = MoskiCheckViewModel(repository, SavedStateHandle(), Dispatchers.Unconfined)
        compose.setContent { MedicimappTheme { MoskiCheckAssistantDialog({}, vm) } }
        compose.onNodeWithText("Comenzar evaluación").performScrollTo().performClick()
        repeat(14) { compose.onNodeWithText("No").performScrollTo().performClick() }
        compose.onNodeWithText("Respuesta de prueba").assertExists()
        compose.onNodeWithText("Realizar otra evaluación").performScrollTo().performClick()
        compose.onNodeWithText("Hola 👋").assertExists()
        assertEquals(1, calls)
    }
}
