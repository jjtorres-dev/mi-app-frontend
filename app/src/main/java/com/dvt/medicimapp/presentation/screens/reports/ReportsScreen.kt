package com.dvt.medicimapp.presentation.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.data.ReporteApi
import com.dvt.medicimapp.data.ReporteZonaApi
import com.dvt.medicimapp.ui.theme.*

@Composable
fun ReportsScreen(navController: NavController) {

    // ── Estado desde la API ───────────────────────────────────────────────────
    var zonas            by remember { mutableStateOf<List<ReporteZonaApi>>(emptyList()) }
    var reportesRecientes by remember { mutableStateOf<List<ReporteApi>>(emptyList()) }
    var totalReportes    by remember { mutableStateOf(0) }
    var totalDistritos   by remember { mutableStateOf(0) }
    var cargando         by remember { mutableStateOf(true) }
    var error            by remember { mutableStateOf<String?>(null) }

    // ── Cargar datos al abrir la pantalla ─────────────────────────────────────
    LaunchedEffect(Unit) {
        cargando = true
        error    = null

        val zonasResult   = ApiService.obtenerReportesPorDistrito()
        val reportesResult = ApiService.obtenerReportes()
        val totalesResult  = ApiService.obtenerTotales()

        zonasResult.fold(
            onSuccess = { zonas = it },
            onFailure = { error = "No se pudo cargar el ranking de zonas" }
        )
        reportesResult.fold(
            onSuccess = { reportesRecientes = it.take(10) }, // mostrar los 10 más recientes
            onFailure = { if (error == null) error = "No se pudo cargar los reportes recientes" }
        )
        totalesResult.fold(
            onSuccess = { totalReportes = it.totalReportes; totalDistritos = it.totalDistritos },
            onFailure = { }
        )

        cargando = false
    }

    val maxReportes  = zonas.maxOfOrNull { it.totalReportes }?.toFloat() ?: 1f
    val masAfectado  = zonas.maxByOrNull { it.totalReportes }?.distrito?.split(" ")?.first() ?: "-"

    fun colorDeRanking(index: Int): Color = when (index) {
        0    -> MoskiRed
        1    -> MoskiAmber
        2    -> Color(0xFFFB8C00)
        else -> MoskiGreen
    }

    fun colorDeTipo(tipo: String): Color = when (tipo) {
        "Recipiente con agua" -> MoskiBlue
        "Llanta/cubierta"    -> MoskiAmber
        "Acequia o canal"    -> MoskiTeal
        "Maleza o vegetación" -> MoskiGreen
        else                 -> MoskiTextHint
    }

    Scaffold(
        containerColor = MoskiBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoskiGreen)
                    .padding(top = 36.dp, bottom = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 56.dp)) {
                    Text("Reportes de Criaderos", fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("Zonas más afectadas en San Martín", fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    ) { innerPadding ->

        // ── Estado de carga ───────────────────────────────────────────────────
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MoskiGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando reportes...", fontSize = 13.sp, color = MoskiTextSecondary)
                }
            }
            return@Scaffold
        }

        // ── Estado de error ───────────────────────────────────────────────────
        if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)) {
                    Text("⚠️", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error!!, fontSize = 14.sp, color = MoskiTextSecondary,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifica que el servidor esté corriendo",
                        fontSize = 12.sp, color = MoskiTextHint, textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        // ── Contenido principal ───────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Chips de estadísticas ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp).padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("$totalReportes", "Total reportes", MoskiRed,   Modifier.weight(1f))
                    StatCard("$totalDistritos", "Distritos",     MoskiAmber, Modifier.weight(1f))
                    StatCard(masAfectado,       "Más afectado",  MoskiGreen, Modifier.weight(1f))
                }
            }

            // ── Título ranking ────────────────────────────────────────────
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = MoskiGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reportes por distrito", fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, color = MoskiTextPrimary)
                }
            }

            // ── Ranking de zonas desde la API ─────────────────────────────
            itemsIndexed(zonas) { index, zona ->
                val barColor = colorDeRanking(index)
                val tipos = mapOf(
                    "Recipiente" to zona.recipiente,
                    "Llanta"     to zona.llanta,
                    "Acequia"    to zona.acequia,
                    "Maleza"     to zona.maleza,
                    "Basura"     to zona.basura
                ).filter { it.value > 0 }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = MoskiSurface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MoskiBorder),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp).padding(vertical = 5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp)
                                .background(barColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center) {
                                Text("#${index + 1}", fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = barColor)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(zona.distrito, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold, color = MoskiTextPrimary,
                                modifier = Modifier.weight(1f))
                            Text("${zona.totalReportes} reportes", fontSize = 13.sp,
                                color = barColor, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { zona.totalReportes / maxReportes },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = barColor, trackColor = Color(0xFFEEEEEE)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tipos.entries.take(3).forEach { (tipo, cantidad) ->
                                val tipoColor = colorDeTipo(tipo)
                                Box(modifier = Modifier
                                    .background(tipoColor.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text("$tipo ($cantidad)", fontSize = 10.sp,
                                        color = tipoColor, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // ── Título reportes recientes ─────────────────────────────────
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(MoskiAmber, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reportes recientes", fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, color = MoskiTextPrimary)
                }
            }

            // ── Lista de reportes recientes desde la API ──────────────────
            items(reportesRecientes) { reporte ->
                val tipoColor = colorDeTipo(reporte.tipoCriadero)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = MoskiSurface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MoskiBorder),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp).padding(vertical = 5.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        // Avatar con inicial
                        Box(modifier = Modifier.size(40.dp)
                            .background(
                                if (reporte.esAnonimo) MoskiBackground else MoskiGreenSurface,
                                CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text(
                                if (reporte.esAnonimo) "?" else reporte.nombre.first().toString(),
                                fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                                color = if (reporte.esAnonimo) MoskiTextHint else MoskiGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(reporte.nombre, fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, color = MoskiTextPrimary)
                                Text(reporte.fechaHora.take(10), fontSize = 10.sp, color = MoskiTextHint)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier
                                .background(tipoColor.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)) {
                                Text(reporte.tipoCriadero, fontSize = 10.sp,
                                    color = tipoColor, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null,
                                    tint = MoskiTextHint, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(reporte.distrito, fontSize = 11.sp, color = MoskiTextSecondary)
                            }
                            if (!reporte.descripcion.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(reporte.descripcion, fontSize = 12.sp,
                                    color = MoskiTextSecondary, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MoskiSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MoskiBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                color = color, textAlign = TextAlign.Center)
            Text(etiqueta, fontSize = 10.sp, color = MoskiTextHint,
                textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}