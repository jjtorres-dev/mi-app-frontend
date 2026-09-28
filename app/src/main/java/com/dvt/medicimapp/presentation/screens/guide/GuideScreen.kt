package com.dvt.medicimapp.presentation.screens.guide

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dvt.medicimapp.di.AppViewModelFactory
import com.dvt.medicimapp.presentation.viewmodel.GuideViewModel
import com.dvt.medicimapp.data.model.GuiaItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dvt.medicimapp.ui.theme.*

@Composable
fun GuideScreen(
    navController: NavController,
    viewModel: GuideViewModel = viewModel(factory = AppViewModelFactory())
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categorias = state.categorias
    val categoriaSeleccionada = state.categoriaSeleccionada

    // Color por categoría centralizado
    fun colorDeCategoria(categoria: String): Color = when (categoria) {
        "Hogar"     -> MoskiBlue
        "Personal"  -> MoskiGreen
        "Comunidad" -> MoskiRed
        else        -> MoskiGreen
    }

    val guiasFiltradas = state.guias

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
                        "Guía de Prevención",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        "Recomendaciones contra el dengue",
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

            // ── Chips de filtro en fila scrolleable ──────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoskiSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { categoria ->
                    val seleccionado = categoria == categoriaSeleccionada
                    val chipColor = if (categoria == "Todas") MoskiGreen
                    else colorDeCategoria(categoria)

                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.seleccionarCategoria(categoria) },
                        label = {
                            Text(
                                categoria,
                                fontSize = 13.sp,
                                fontWeight = if (seleccionado) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White,
                            containerColor = chipColor.copy(alpha = 0.08f),
                            labelColor = chipColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = seleccionado,
                            selectedBorderColor = Color.Transparent,
                            borderColor = chipColor.copy(alpha = 0.3f),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        )
                    )
                }
            }

            // ── Contador ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${guiasFiltradas.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MoskiGreen
                )
                Text(
                    " recomendaciones",
                    fontSize = 13.sp,
                    color = MoskiTextSecondary
                )
            }

            // ── Lista expandible ─────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(guiasFiltradas, key = { it.titulo }) { guia ->
                    var expandido by rememberSaveable { mutableStateOf(false) }
                    val categoriaColor = colorDeCategoria(guia.categoria)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MoskiSurface),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, MoskiBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandido = !expandido }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {

                            // ── Fila principal ────────────────────────────
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Ícono con color de categoría
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            categoriaColor.copy(alpha = 0.10f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(guia.icono, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        guia.titulo,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MoskiTextPrimary,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        guia.categoria,
                                        fontSize = 11.sp,
                                        color = categoriaColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Flecha más grande y tappable
                                Icon(
                                    imageVector = if (expandido)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MoskiTextHint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // ── Contenido expandible con animación ────────
                            AnimatedVisibility(
                                visible = expandido,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        guia.descripcion,
                                        fontSize = 13.sp,
                                        color = MoskiTextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
