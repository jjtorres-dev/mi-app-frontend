package com.dvt.medicimapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun open(label: String) {
        compose.onNodeWithText(label).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun back() {
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Inicio").assertExists()
    }

    @Test fun guideKeepsNavigationAndCategoryFilter() {
        open("Guía de Prevención")
        compose.onNodeWithText("Recomendaciones contra el dengue").assertIsDisplayed()
        compose.onNode(hasText("Personal") and isNotSelected()).performClick()
        compose.onNodeWithText("4").assertExists()
        back()
    }

    @Test fun healthCentersKeepsMapListAndReturn() {
        open("Centros de Salud")
        compose.onNodeWithText("Establecimientos en Tarapoto").assertIsDisplayed()
        compose.onNodeWithText("Lista").performClick()
        compose.onNodeWithText("Hospital II-2 Tarapoto").assertExists()
        compose.onNodeWithText("Mapa").performClick()
        back()
    }

    @Test fun reportsDestinationAndReturnArePreserved() {
        open("Reportes de Zona")
        compose.onNodeWithText("Reportes de Criaderos").assertIsDisplayed()
        back()
    }

    @Test fun reportRestoresFieldsAfterActivityRecreation() {
        open("Reportar Criadero")
        compose.onNodeWithText("Nombre completo").performTextInput("Usuario de prueba")
        compose.onNodeWithText("Distrito").performClick()
        compose.onNodeWithText("Morales").performClick()
        compose.onNodeWithText("Barrio / Sector / Referencia").performScrollTo().performTextInput("Sector de prueba")
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText("Usuario de prueba").assertExists()
        compose.onNodeWithText("Morales").assertExists()
        compose.onNodeWithText("Sector de prueba").assertExists()
        back()
    }

    @Test fun assistantRestoresItsQuestionAndStartsFreshWhenReopened() {
        compose.onNodeWithText("🦟").performClick()
        compose.onNodeWithText("Comenzar evaluación").performScrollTo().performClick()
        compose.onNodeWithText("No").performScrollTo().performClick()
        compose.onNodeWithText("¿Tiene dolor de cabeza?").assertExists()
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText("¿Tiene dolor de cabeza?").assertExists()
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithText("🦟").performClick()
        compose.onNodeWithText("Hola 👋").assertExists()
        compose.onNodeWithText("Cerrar").performClick()
    }
}
