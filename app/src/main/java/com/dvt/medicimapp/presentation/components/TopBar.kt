package com.dvt.medicimapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TopBar(
    titulo: String,
    subtitulo: String,
    color: Color,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, top = 40.dp)
        ) {
            Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitulo, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}