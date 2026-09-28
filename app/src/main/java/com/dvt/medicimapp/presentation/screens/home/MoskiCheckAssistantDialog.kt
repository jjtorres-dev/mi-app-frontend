package com.dvt.medicimapp.presentation.screens.home

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dvt.medicimapp.di.AppViewModelFactory
import com.dvt.medicimapp.presentation.viewmodel.MoskiCheckViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dvt.medicimapp.data.model.ResultadoMoskiCheckApi
import com.dvt.medicimapp.ui.theme.MoskiAmber
import com.dvt.medicimapp.ui.theme.MoskiAmberDark
import com.dvt.medicimapp.ui.theme.MoskiAmberLight
import com.dvt.medicimapp.ui.theme.MoskiBorder
import com.dvt.medicimapp.ui.theme.MoskiGreen
import com.dvt.medicimapp.ui.theme.MoskiGreenLight
import com.dvt.medicimapp.ui.theme.MoskiRed
import com.dvt.medicimapp.ui.theme.MoskiSurface
import com.dvt.medicimapp.ui.theme.MoskiTextHint
import com.dvt.medicimapp.ui.theme.MoskiTextPrimary
import com.dvt.medicimapp.ui.theme.MoskiTeal
import java.util.Locale




@Composable
fun MoskiCheckAssistantDialog(
    onDismiss: () -> Unit,
    viewModel: MoskiCheckViewModel = viewModel(factory = AppViewModelFactory())
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val iniciado = state.iniciado
    val indicePregunta = state.indicePregunta
    val cargando = state.cargando
    val resultado = state.resultado
    val error = state.error
    val preguntasMoskiCheck = state.preguntas
    val cerrar = {
        viewModel.reiniciarEvaluacion()
        onDismiss()
    }

    Dialog(
        onDismissRequest = {

            if (!cargando) {
                cerrar()
            }
        }
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = MoskiSurface,
            tonalElevation = 6.dp
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(20.dp)
            ) {

                // ====================================================
                // ENCABEZADO
                // ====================================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                MoskiGreenLight,
                                CircleShape
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "🦟",
                            fontSize = 22.sp
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.size(12.dp)
                    )


                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Asistente MoskiCheck",
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color = MoskiTextPrimary
                        )

                        Text(
                            text =
                                "Evaluación orientativa de síntomas",
                            fontSize = 11.sp,
                            color = MoskiTextHint
                        )
                    }


                    OutlinedButton(
                        onClick = cerrar,
                        enabled = !cargando
                    ) {

                        Text("Cerrar")
                    }
                }


                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                // ====================================================
                // INTRODUCCIÓN
                // ====================================================

                if (
                    !iniciado &&
                    resultado == null
                ) {

                    Text(
                        text = "Hola 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoskiTextPrimary
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        text =
                            "Responderás 14 preguntas de Sí o No sobre síntomas y señales de alarma.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MoskiTextPrimary
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MoskiGreenLight
                            ),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            )
                    ) {

                        Text(
                            text =
                                "MoskiCheck brinda una orientación preliminar. No reemplaza la evaluación ni el diagnóstico de un profesional de salud.",
                            modifier =
                                Modifier.padding(14.dp),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MoskiTeal
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )


                    Button(
                        onClick = {

                            viewModel.iniciar()
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    MoskiGreen
                            )
                    ) {

                        Text(
                            text =
                                "Comenzar evaluación",
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                }


                // ====================================================
                // CARGANDO
                // ====================================================

                else if (cargando) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 32.dp
                                ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = MoskiGreen
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        Text(
                            text =
                                "Analizando tus respuestas...",
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Medium,
                            color =
                                MoskiTextPrimary
                        )


                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        Text(
                            text =
                                "Esto puede tomar unos segundos.",
                            fontSize = 12.sp,
                            color = MoskiTextHint
                        )
                    }
                }


                // ====================================================
                // ERROR
                // ====================================================

                else if (error != null) {

                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFFFFEBEE)
                            ),
                        border =
                            BorderStroke(
                                1.dp,
                                MoskiRed
                            ),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                text =
                                    "No se pudo completar la evaluación",
                                fontWeight =
                                    FontWeight.SemiBold,
                                color = MoskiRed
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(
                                text =
                                    error
                                        ?: "Error desconocido",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color =
                                    MoskiTextPrimary
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    Button(
                        onClick = {

                            viewModel.enviarEvaluacion()
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    MoskiGreen
                            )
                    ) {

                        Text("Intentar nuevamente")
                    }


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    OutlinedButton(
                        onClick = {
                            viewModel.reiniciarEvaluacion()
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text("Reiniciar evaluación")
                    }
                }


                // ====================================================
                // RESULTADO
                // ====================================================

                else if (resultado != null) {

                    ResultadoEvaluacion(
                        resultado =
                            resultado!!
                    )


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    Button(
                        onClick = {
                            viewModel.reiniciarEvaluacion()
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    MoskiGreen
                            )
                    ) {

                        Text("Realizar otra evaluación")
                    }


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    OutlinedButton(
                        onClick = cerrar,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text("Volver al inicio")
                    }
                }


                // ====================================================
                // PREGUNTAS
                // ====================================================

                else if (iniciado) {

                    val pregunta =
                        preguntasMoskiCheck[
                            indicePregunta
                        ]


                    val numero =
                        indicePregunta + 1

                    val total =
                        preguntasMoskiCheck.size


                    Text(
                        text =
                            "Pregunta $numero de $total",
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color = MoskiTextHint
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    LinearProgressIndicator(
                        progress = {
                            numero.toFloat() /
                                    total.toFloat()
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        color = MoskiGreen,
                        trackColor =
                            MoskiGreenLight
                    )


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    if (pregunta.esSeguridad) {

                        Card(
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MoskiAmberLight
                                ),
                            shape =
                                RoundedCornerShape(
                                    12.dp
                                )
                        ) {

                            Text(
                                text =
                                    "⚠️ Pregunta de seguridad",
                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            12.dp,
                                        vertical =
                                            8.dp
                                    ),
                                fontSize = 11.sp,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color =
                                    MoskiAmberDark
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )
                    }


                    Text(
                        text = pregunta.texto,
                        modifier =
                            Modifier.fillMaxWidth(),
                        fontSize = 20.sp,
                        lineHeight = 27.sp,
                        textAlign =
                            TextAlign.Start,
                        fontWeight =
                            FontWeight.SemiBold,
                        color = MoskiTextPrimary
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Text(
                        text =
                            "Selecciona la opción que mejor describa tu situación actual.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MoskiTextHint
                    )


                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )


                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        OutlinedButton(
                            onClick = {
                                viewModel.responder(0)
                            },
                            modifier =
                                Modifier.weight(1f),
                            border =
                                BorderStroke(
                                    1.dp,
                                    MoskiBorder
                                )
                        ) {

                            Text(
                                text = "No",
                                color =
                                    MoskiTextPrimary
                            )
                        }


                        Button(
                            onClick = {
                                viewModel.responder(1)
                            },
                            modifier =
                                Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        MoskiGreen
                                )
                        ) {

                            Text("Sí")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ResultadoEvaluacion(
    resultado: ResultadoMoskiCheckApi
) {

    when (resultado.status) {

        // ====================================================
        // ORIENTACIÓN
        // ====================================================

        "orientation" -> {

            val enfermedad =
                when (
                    resultado.prediction
                        ?.lowercase()
                ) {

                    "dengue" ->
                        "Dengue"

                    "zika" ->
                        "Zika"

                    "chikungunya" ->
                        "Chikungunya"

                    else ->
                        resultado.prediction
                            ?: "Sin resultado"
                }


            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MoskiGreenLight
                    ),
                border =
                    BorderStroke(
                        1.dp,
                        MoskiGreen
                    ),
                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Text(
                        text =
                            "Orientación preliminar",
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color = MoskiTeal
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text =
                            "Mayor compatibilidad sintomática:",
                        fontSize = 13.sp,
                        color = MoskiTextPrimary
                    )


                    Text(
                        text = enfermedad,
                        fontSize = 26.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = MoskiGreen
                    )


                    resultado.confidence
                        ?.let { confianza ->

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    "Confianza del modelo: ${
                                        porcentaje(
                                            confianza
                                        )
                                    }",
                                fontSize = 12.sp,
                                color =
                                    MoskiTextPrimary
                            )
                        }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Text(
                text = resultado.message,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MoskiTextPrimary
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(
                text =
                    "Este resultado no constituye un diagnóstico médico. Si los síntomas persisten o empeoran, busca evaluación profesional.",
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = MoskiTextHint
            )
        }


        // ====================================================
        // NO CONCLUYENTE
        // ====================================================

        "inconclusive" -> {

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MoskiAmberLight
                    ),
                border =
                    BorderStroke(
                        1.dp,
                        MoskiAmber
                    ),
                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Text(
                        text =
                            "Resultado no concluyente",
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MoskiAmberDark
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text = resultado.message,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color =
                            MoskiTextPrimary
                    )


                    resultado.confidence
                        ?.let { confianza ->

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    "Mayor probabilidad observada: ${
                                        porcentaje(
                                            confianza
                                        )
                                    }",
                                fontSize = 11.sp,
                                color =
                                    MoskiTextHint
                            )
                        }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(
                text =
                    "MoskiCheck no forzará una enfermedad cuando la información registrada no sea suficiente o el modelo no alcance el nivel de confianza establecido.",
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = MoskiTextHint
            )
        }


        // ====================================================
        // SEÑAL DE ALARMA
        // ====================================================

        "alert" -> {

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFFFFEBEE)
                    ),
                border =
                    BorderStroke(
                        1.dp,
                        MoskiRed
                    ),
                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Text(
                        text =
                            "⚠️ Señal de alarma detectada",
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = MoskiRed
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text = resultado.message,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color =
                            MoskiTextPrimary
                    )


                    if (
                        resultado.alertsDetected
                            .isNotEmpty()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )


                        Text(
                            text =
                                "Señales registradas:",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                MoskiTextPrimary
                        )


                        resultado.alertsDetected
                            .forEach { alerta ->

                                Text(
                                    text =
                                        "• ${
                                            nombreAlerta(
                                                alerta
                                            )
                                        }",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color =
                                        MoskiTextPrimary
                                )
                            }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(
                text =
                    "Ante una señal de alarma, la aplicación no muestra una clasificación de enfermedad como resultado principal. La prioridad es recomendar evaluación profesional.",
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = MoskiTextHint
            )
        }
    }
}


private fun porcentaje(
    valor: Double
): String {

    return String.format(
        Locale.US,
        "%.1f%%",
        valor * 100.0
    )
}


private fun nombreAlerta(
    campo: String
): String {

    return when (campo) {

        "dolor_abdominal_intenso" ->
            "Dolor abdominal intenso o persistente"

        "sangrado" ->
            "Sangrado inusual"

        "vomitos_persistentes" ->
            "Vómitos persistentes o muy frecuentes"

        else ->
            campo
    }
}
