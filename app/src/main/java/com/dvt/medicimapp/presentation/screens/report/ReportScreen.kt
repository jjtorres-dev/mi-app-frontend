package com.dvt.medicimapp.presentation.screens.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController) {

    val scope = rememberCoroutineScope()

    var nombre         by remember { mutableStateOf("") }
    var distrito       by remember { mutableStateOf("") }
    var barrio         by remember { mutableStateOf("") }
    var descripcion    by remember { mutableStateOf("") }
    var tipoCriadero   by remember { mutableStateOf("") }
    var fotoUri        by remember { mutableStateOf<Uri?>(null) }
    var esAnonimo      by remember { mutableStateOf(false) }
    var mostrarExito   by remember { mutableStateOf(false) }
    var mostrarError   by remember { mutableStateOf(false) }
    var mensajeError   by remember { mutableStateOf("") }
    var enviando       by remember { mutableStateOf(false) }
    var distritosExpanded by remember { mutableStateOf(false) }
    var tiposExpanded     by remember { mutableStateOf(false) }

    val distritos = listOf(
        "Tarapoto", "Morales", "Banda de Shilcayo",
        "Juan Guerra", "Cacatachi", "Otro"
    )
    val tiposCriadero = listOf(
        "Recipiente con agua", "Llanta/cubierta",
        "Acequia o canal", "Maleza o vegetación",
        "Depósito de basura", "Otro"
    )

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> fotoUri = uri }

    // ── Diálogo éxito ─────────────────────────────────────────────────────────
    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MoskiGreenSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null,
                        tint = MoskiGreen, modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("Reporte enviado", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text  = {
                Text(
                    "Tu reporte ha sido registrado. Gracias por contribuir a la prevención del dengue en San Martín.",
                    fontSize = 14.sp, color = MoskiTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarExito = false; navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MoskiGreen),
                    shape  = RoundedCornerShape(10.dp)
                ) { Text("Aceptar") }
            },
            containerColor = MoskiSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Diálogo error ─────────────────────────────────────────────────────────
    if (mostrarError) {
        AlertDialog(
            onDismissRequest = { mostrarError = false },
            title = { Text("Error al enviar", fontWeight = FontWeight.SemiBold) },
            text  = { Text(mensajeError, fontSize = 14.sp, color = MoskiTextSecondary) },
            confirmButton = {
                Button(
                    onClick = { mostrarError = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MoskiRed),
                    shape  = RoundedCornerShape(10.dp)
                ) { Text("Entendido") }
            },
            containerColor = MoskiSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Layout principal ──────────────────────────────────────────────────────
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
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 56.dp, end = 16.dp)
                ) {
                    Text("Reportar Criadero", fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("Ayuda a prevenir el dengue en tu zona", fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f))
                }
            }
        },
        bottomBar = {
            val formularioValido = distrito.isNotBlank() && tipoCriadero.isNotBlank() &&
                    (esAnonimo || nombre.isNotBlank())
            Surface(color = MoskiSurface, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        // ── Llamada real a la API ─────────────────────────
                        scope.launch {
                            enviando = true
                            val result = ApiService.enviarReporte(
                                nombre       = if (esAnonimo) null else nombre,
                                distrito     = distrito,
                                barrio       = barrio.ifBlank { null },
                                tipoCriadero = tipoCriadero,
                                descripcion  = descripcion.ifBlank { null },
                                esAnonimo    = esAnonimo
                            )
                            enviando = false
                            result.fold(
                                onSuccess = { mostrarExito = true },
                                onFailure = {
                                    mensajeError = "No se pudo conectar con el servidor. Verifica tu conexión."
                                    mostrarError = true
                                }
                            )
                        }
                    },
                    enabled = formularioValido && !enviando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = MoskiGreen,
                        disabledContainerColor = Color(0xFFB2DFDB)
                    )
                ) {
                    if (enviando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar reporte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Toggle anónimo ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoskiBlueLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Reportar anónimamente", fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, color = MoskiBlue)
                    Text("Tu nombre no será visible", fontSize = 11.sp, color = MoskiTextSecondary)
                }
                Switch(
                    checked = esAnonimo,
                    onCheckedChange = { esAnonimo = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor   = Color.White,
                        checkedTrackColor   = MoskiGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MoskiTextHint
                    )
                )
            }

            // ── Datos del reportante ──────────────────────────────────────
            FormSectionTitle("DATOS DEL REPORTANTE")

            if (!esAnonimo) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoskiGreen, focusedLabelColor = MoskiGreen)
                )
            }

            ExposedDropdownMenuBox(expanded = distritosExpanded,
                onExpandedChange = { distritosExpanded = it }) {
                OutlinedTextField(
                    value = distrito, onValueChange = {}, readOnly = true,
                    label = { Text("Distrito") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distritosExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoskiGreen, focusedLabelColor = MoskiGreen)
                )
                ExposedDropdownMenu(expanded = distritosExpanded,
                    onDismissRequest = { distritosExpanded = false }) {
                    distritos.forEach { d ->
                        DropdownMenuItem(text = { Text(d) },
                            onClick = { distrito = d; distritosExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = barrio, onValueChange = { barrio = it },
                label = { Text("Barrio / Sector / Referencia") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MoskiGreen, focusedLabelColor = MoskiGreen)
            )

            // ── Datos del criadero ────────────────────────────────────────
            FormSectionTitle("DATOS DEL CRIADERO")

            ExposedDropdownMenuBox(expanded = tiposExpanded,
                onExpandedChange = { tiposExpanded = it }) {
                OutlinedTextField(
                    value = tipoCriadero, onValueChange = {}, readOnly = true,
                    label = { Text("Tipo de criadero") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tiposExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoskiGreen, focusedLabelColor = MoskiGreen)
                )
                ExposedDropdownMenu(expanded = tiposExpanded,
                    onDismissRequest = { tiposExpanded = false }) {
                    tiposCriadero.forEach { t ->
                        DropdownMenuItem(text = { Text(t) },
                            onClick = { tipoCriadero = t; tiposExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripción del lugar") },
                placeholder = { Text("Ej: Frente a la tienda, recipientes con agua verde", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MoskiGreen, focusedLabelColor = MoskiGreen)
            )

            // ── Foto del criadero ─────────────────────────────────────────
            FormSectionTitle("FOTO DEL CRIADERO")

            Box(
                modifier = Modifier
                    .fillMaxWidth().height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MoskiBorder, RoundedCornerShape(12.dp))
                    .background(MoskiSurface)
                    .clickable { galeriaLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri != null) {
                    AsyncImage(model = fotoUri, contentDescription = "Foto del criadero",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(48.dp)
                            .background(MoskiGreenSurface, CircleShape),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null,
                                tint = MoskiGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para agregar una foto", fontSize = 13.sp, color = MoskiTextSecondary)
                        Text("Opcional", fontSize = 11.sp, color = MoskiTextHint)
                    }
                }
            }

            if (fotoUri != null) {
                TextButton(onClick = { fotoUri = null },
                    modifier = Modifier.align(Alignment.End)) {
                    Text("Quitar foto", color = MoskiRed, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FormSectionTitle(texto: String) {
    Text(texto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        color = MoskiTextHint, letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 4.dp))
}