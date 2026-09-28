package com.dvt.medicimapp.presentation.screens.healthcenters

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dvt.medicimapp.di.AppViewModelFactory
import com.dvt.medicimapp.presentation.viewmodel.HealthCentersViewModel
import com.dvt.medicimapp.data.model.CentroSalud
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.dvt.medicimapp.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HealthCentersScreen(
    navController: NavController,
    viewModel: HealthCentersViewModel = viewModel(factory = AppViewModelFactory())
) {

    val context = LocalContext.current

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val centros = state.centros
    val tabSeleccionado = state.tabSeleccionado

    // Referencia del mapa para evitar pantalla gris
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef.value?.onDetach()
        }
    }

    fun colorDeTipo(tipo: String): Color = when (tipo) {
        "Hospital"       -> MoskiRed
        "Clínica Privada" -> MoskiAmber
        "Posta Médica"   -> MoskiGreen
        else             -> MoskiBlue
    }

    fun emojiDeTipo(tipo: String): String = when (tipo) {
        "Hospital"       -> "🏥"
        "Clínica Privada" -> "🏨"
        "Posta Médica"   -> "🩺"
        else             -> "🏪"
    }

    Scaffold(
        containerColor = MoskiBackground,
        topBar = {
            // ── Header verde consistente ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoskiGreen)
                    .padding(top = 36.dp, bottom = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 56.dp)
                ) {
                    Text(
                        "Centros de Salud",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        "Establecimientos en Tarapoto",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ── Tabs con indicador verde ──────────────────────────────────
            TabRow(
                selectedTabIndex = tabSeleccionado,
                modifier = Modifier.zIndex(1f),
                containerColor = MoskiSurface,
                contentColor = MoskiGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                        color = MoskiGreen,
                        height = 2.dp
                    )
                }
            ) {
                listOf("Mapa", "Lista").forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionado == index,
                        onClick = { viewModel.seleccionarTab(index) },
                        selectedContentColor = MoskiGreen,
                        unselectedContentColor = MoskiTextHint,
                        text = {
                            Text(
                                titulo,
                                fontWeight = if (tabSeleccionado == index)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MoskiBackground)
                    .clipToBounds()
            ) {
                when (tabSeleccionado) {

                    // ── Vista Mapa ────────────────────────────────────────
                    0 -> {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                Configuration.getInstance().load(
                                    ctx,
                                    PreferenceManager.getDefaultSharedPreferences(ctx)
                                )
                                Configuration.getInstance().userAgentValue = ctx.packageName

                                MapView(ctx).apply {
                                    mapViewRef.value = this
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(14.0)
                                    controller.setCenter(GeoPoint(-6.4851, -76.3615))
                                    setScrollableAreaLimitDouble(
                                        BoundingBox(-6.44, -76.33, -6.53, -76.41)
                                    )
                                    centros.forEach {
                                        val marker = Marker(this)
                                        marker.position = GeoPoint(it.lat, it.lon)
                                        marker.title = it.nombre
                                        marker.snippet = "${it.tipo} · ${it.telefono}"
                                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        overlays.add(marker)
                                    }
                                    invalidate()
                                }
                            },
                            update = { mapView ->
                                mapView.invalidate()
                            }
                        )
                    }

                    // ── Vista Lista ───────────────────────────────────────
                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            items(centros) { centro ->
                                val tipoColor = colorDeTipo(centro.tipo)

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    colors = CardDefaults.cardColors(containerColor = MoskiSurface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp, MoskiBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        // ── Ícono tipo ────────────────────
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    tipoColor.copy(alpha = 0.10f),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emojiDeTipo(centro.tipo), fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // ── Info ──────────────────────────
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                centro.nombre,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MoskiTextPrimary
                                            )
                                            Text(
                                                centro.tipo,
                                                fontSize = 11.sp,
                                                color = tipoColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MoskiTextHint,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    centro.direccion,
                                                    fontSize = 11.sp,
                                                    color = MoskiTextSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // ── Botón llamar con fondo verde ──
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(
                                                        Intent.ACTION_DIAL,
                                                        Uri.parse("tel:${centro.telefono}")
                                                    )
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(MoskiGreenSurface, CircleShape)
                                            ) {
                                                Icon(
                                                    Icons.Default.Call,
                                                    contentDescription = "Llamar",
                                                    tint = MoskiGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(
                                                "Llamar",
                                                fontSize = 9.sp,
                                                color = MoskiGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}
