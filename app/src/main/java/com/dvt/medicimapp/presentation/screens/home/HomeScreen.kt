package com.dvt.medicimapp.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dvt.medicimapp.R
import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.presentation.navigation.Screen
import com.dvt.medicimapp.ui.theme.*
import java.util.Calendar


data class MenuItem(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val route: String
)


data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)


@Composable
fun HomeScreen(
    navController: NavController
) {

    val menuItems = listOf(

        MenuItem(
            "Centros de Salud",
            "Cercanos a ti",
            Icons.Default.LocationOn,
            MoskiBlue,
            MoskiBlueLight,
            Screen.HealthCenters.route
        ),

        MenuItem(
            "Reportar Criadero",
            "Ayuda a tu comunidad",
            Icons.Default.Warning,
            MoskiAmber,
            MoskiAmberLight,
            Screen.Report.route
        ),

        MenuItem(
            "Guía de Prevención",
            "12 recomendaciones",
            Icons.Default.Book,
            MoskiGreen,
            MoskiGreenSurface,
            Screen.Guide.route
        ),

        MenuItem(
            "Reportes de Zona",
            "Ver mapa de calor",
            Icons.Default.BarChart,
            MoskiTeal,
            MoskiTealLight,
            Screen.Reports.route
        )
    )


    val navItems = listOf(

        NavItem(
            "Inicio",
            Icons.Default.Home,
            Screen.Home.route
        ),

        NavItem(
            "Mapa",
            Icons.Default.LocationOn,
            Screen.HealthCenters.route
        ),

        NavItem(
            "Alertas",
            Icons.Default.Warning,
            Screen.Reports.route
        ),

        NavItem(
            "Perfil",
            Icons.Default.Person,
            "perfil"
        )
    )


    val consejos = listOf(

        "Mantén tapados los tanques y depósitos de agua.",

        "Elimina el agua estancada de macetas y recipientes.",

        "Usa repelente de insectos al salir de casa.",

        "Coloca mosquiteros en puertas y ventanas.",

        "Cambia el agua de bebederos de animales cada 3 días.",

        "Mantén los desagües y canaletas limpios.",

        "Guarda las llantas en lugares cubiertos.",

        "Usa ropa de manga larga en zonas de alto riesgo.",

        "Reporta focos de mosquitos a las autoridades de salud.",

        "Fumiga tu hogar periódicamente en épocas de lluvia."
    )


    val diaDelAnio =
        Calendar
            .getInstance()
            .get(
                Calendar.DAY_OF_YEAR
            )


    val consejoHoy =
        consejos[
            diaDelAnio %
                    consejos.size
        ]


    var selectedNav by remember {
        mutableIntStateOf(0)
    }


    // ========================================================
    // ESTADO DEL ASISTENTE MOSKICHECK
    // ========================================================

    var mostrarAsistente by remember {
        mutableStateOf(false)
    }


    // ========================================================
    // ESTADO DESDE LA API
    // ========================================================

    var totalReportes by remember {
        mutableStateOf("-")
    }

    var totalDistritos by remember {
        mutableStateOf("-")
    }

    var nivelAlerta by remember {
        mutableStateOf("moderado")
    }

    var descAlerta by remember {
        mutableStateOf(
            "Cargando información de tu zona..."
        )
    }

    var cargandoStats by remember {
        mutableStateOf(true)
    }


    // ========================================================
    // CARGAR DATOS DEL HOME
    // ========================================================

    LaunchedEffect(Unit) {

        ApiService
            .obtenerTotales()
            .fold(

                onSuccess = {

                    totalReportes =
                        "${it.totalReportes}"

                    totalDistritos =
                        "${it.totalDistritos}"
                },

                onFailure = {

                    totalReportes = "?"
                    totalDistritos = "?"
                }
            )


        ApiService
            .obtenerAlertaHoy(
                "Tarapoto"
            )
            .fold(

                onSuccess = {

                    nivelAlerta =
                        it.nivel

                    descAlerta =
                        it.descripcion
                },

                onFailure = {

                    nivelAlerta =
                        "bajo"

                    descAlerta =
                        "Sin datos de alerta disponibles"
                }
            )


        cargandoStats = false
    }


    val coloresBanner =
        when (nivelAlerta) {

            "critico",
            "alto" -> listOf(
                Color(0xFFFFEBEE),
                MoskiRed,
                Color(0xFF7F0000),
                Color(0xFFB71C1C)
            )

            "moderado" -> listOf(
                MoskiAmberLight,
                MoskiAmber,
                Color(0xFF633806),
                MoskiAmberDark
            )

            else -> listOf(
                MoskiGreenLight,
                MoskiGreen,
                Color(0xFF1B5E20),
                MoskiTeal
            )
        }


    val bannerBackground =
        coloresBanner[0]

    val bannerDotColor =
        coloresBanner[1]

    val bannerTitleColor =
        coloresBanner[2]

    val bannerTextColor =
        coloresBanner[3]


    val tituloAlerta =
        when (nivelAlerta) {

            "critico" ->
                "⚠️ Alerta crítica en tu zona"

            "alto" ->
                "🔴 Alerta alta en tu zona"

            "moderado" ->
                "🟡 Alerta moderada en tu zona"

            else ->
                "🟢 Riesgo bajo en tu zona"
        }


    Scaffold(

        containerColor =
            MoskiBackground,


        // ====================================================
        // BOTÓN FLOTANTE DEL ASISTENTE
        // ====================================================

        floatingActionButton = {

            FloatingActionButton(

                onClick = {
                    mostrarAsistente = true
                },

                containerColor =
                    MoskiGreen,

                contentColor =
                    Color.White,

                shape =
                    CircleShape
            ) {

                Text(
                    text = "🦟",
                    fontSize = 22.sp
                )
            }
        },


        bottomBar = {

            NavigationBar(

                containerColor =
                    MoskiSurface,

                tonalElevation =
                    0.dp,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart =
                                    16.dp,
                                topEnd =
                                    16.dp
                            )
                        )
            ) {

                navItems
                    .forEachIndexed {
                            index,
                            item ->

                        NavigationBarItem(

                            selected =
                                selectedNav ==
                                        index,

                            onClick = {

                                selectedNav =
                                    index

                                if (
                                    item.route !=
                                    "perfil"
                                ) {

                                    navController
                                        .navigate(
                                            item.route
                                        )
                                }
                            },

                            icon = {

                                Icon(
                                    item.icon,
                                    contentDescription =
                                        item.label,
                                    modifier =
                                        Modifier
                                            .size(
                                                22.dp
                                            )
                                )
                            },

                            label = {

                                Text(
                                    item.label,
                                    fontSize =
                                        10.sp
                                )
                            },

                            colors =
                                NavigationBarItemDefaults
                                    .colors(

                                        selectedIconColor =
                                            MoskiGreen,

                                        selectedTextColor =
                                            MoskiGreen,

                                        unselectedIconColor =
                                            MoskiTextHint,

                                        unselectedTextColor =
                                            MoskiTextHint,

                                        indicatorColor =
                                            MoskiGreenLight
                                    )
                        )
                    }
            }
        }
    ) { innerPadding ->


        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
        ) {


            // ====================================================
            // HEADER
            // ====================================================

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MoskiGreen
                        )
                        .padding(
                            horizontal =
                                20.dp
                        )
                        .padding(
                            top = 48.dp,
                            bottom =
                                20.dp
                        )
            ) {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement
                            .SpaceBetween
                ) {

                    Row(

                        verticalAlignment =
                            Alignment
                                .CenterVertically
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .size(
                                        44.dp
                                    )
                                    .background(
                                        Color.White,
                                        CircleShape
                                    )
                                    .padding(
                                        6.dp
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Image(

                                painter =
                                    painterResource(
                                        id =
                                            R.drawable
                                                .logo_moskicheck
                                    ),

                                contentDescription =
                                    "Logo MoskiCheck",

                                modifier =
                                    Modifier
                                        .fillMaxSize(),

                                contentScale =
                                    ContentScale.Fit
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.width(
                                    12.dp
                                )
                        )


                        Column {

                            Text(

                                text =
                                    "MoskiCheck",

                                fontSize =
                                    22.sp,

                                fontWeight =
                                    FontWeight
                                        .SemiBold,

                                color =
                                    Color.White
                            )


                            Text(

                                text =
                                    "San Martín · Tarapoto",

                                fontSize =
                                    12.sp,

                                color =
                                    Color.White
                                        .copy(
                                            alpha =
                                                0.75f
                                        )
                            )
                        }
                    }


                    Box(

                        modifier =
                            Modifier
                                .size(
                                    36.dp
                                )
                                .background(
                                    Color.White
                                        .copy(
                                            alpha =
                                                0.2f
                                        ),
                                    CircleShape
                                )
                                .clickable { },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            Icons.Default
                                .Notifications,

                            contentDescription =
                                "Notificaciones",

                            tint =
                                Color.White,

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    }
                }
            }


            // ====================================================
            // BANNER DE ALERTA
            // ====================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                16.dp
                        )
                        .padding(
                            top =
                                14.dp
                        )
                        .background(
                            bannerBackground,
                            RoundedCornerShape(
                                10.dp
                            )
                        )
                        .padding(
                            horizontal =
                                12.dp,
                            vertical =
                                10.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                if (cargandoStats) {

                    CircularProgressIndicator(

                        color =
                            MoskiAmber,

                        modifier =
                            Modifier.size(
                                14.dp
                            ),

                        strokeWidth =
                            2.dp
                    )

                } else {

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    8.dp
                                )
                                .background(
                                    bannerDotColor,
                                    CircleShape
                                )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                Column {

                    Text(

                        text =
                            tituloAlerta,

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight
                                .SemiBold,

                        color =
                            bannerTitleColor
                    )


                    Text(

                        text =
                            descAlerta,

                        fontSize =
                            11.sp,

                        color =
                            bannerTextColor
                    )
                }
            }


            // ====================================================
            // CONSEJO DEL DÍA
            // ====================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                16.dp
                        )
                        .padding(
                            top =
                                10.dp
                        )
                        .background(
                            MoskiGreenLight,
                            RoundedCornerShape(
                                10.dp
                            )
                        )
                        .padding(
                            horizontal =
                                12.dp,
                            vertical =
                                10.dp
                        ),

                verticalAlignment =
                    Alignment.Top
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(
                                28.dp
                            )
                            .background(
                                MoskiGreen,
                                CircleShape
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        Icons.Default
                            .Notifications,

                        contentDescription =
                            null,

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                14.dp
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                Column {

                    Text(

                        text =
                            "CONSEJO DEL DÍA",

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight
                                .SemiBold,

                        color =
                            MoskiTeal,

                        letterSpacing =
                            0.8.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )


                    Text(

                        text =
                            consejoHoy,

                        fontSize =
                            12.sp,

                        color =
                            Color(
                                0xFF085041
                            ),

                        lineHeight =
                            17.sp
                    )
                }
            }


            // ====================================================
            // ESTADÍSTICAS
            // ====================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                16.dp
                        )
                        .padding(
                            top =
                                12.dp
                        ),

                horizontalArrangement =
                    Arrangement
                        .spacedBy(
                            8.dp
                        )
            ) {

                StatChip(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    numero =
                        totalReportes,

                    label =
                        "Reportes",

                    color =
                        MoskiRed
                )


                StatChip(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    numero =
                        totalDistritos,

                    label =
                        "Distritos",

                    color =
                        MoskiAmber
                )


                StatChip(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    numero =
                        "12",

                    label =
                        "Guías",

                    color =
                        MoskiGreen
                )
            }


            // ====================================================
            // ACCIONES RÁPIDAS
            // ====================================================

            Text(

                text =
                    "ACCIONES RÁPIDAS",

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight
                        .SemiBold,

                color =
                    MoskiTextHint,

                letterSpacing =
                    0.8.sp,

                modifier =
                    Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        bottom =
                            8.dp
                    )
            )


            Column(

                modifier =
                    Modifier.padding(
                        horizontal =
                            16.dp
                    ),

                verticalArrangement =
                    Arrangement
                        .spacedBy(
                            10.dp
                        )
            ) {

                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    ActionCard(

                        item =
                            menuItems[0],

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {

                            navController
                                .navigate(
                                    menuItems[0]
                                        .route
                                )
                        }
                    )


                    ActionCard(

                        item =
                            menuItems[1],

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {

                            navController
                                .navigate(
                                    menuItems[1]
                                        .route
                                )
                        }
                    )
                }


                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    ActionCard(

                        item =
                            menuItems[2],

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {

                            navController
                                .navigate(
                                    menuItems[2]
                                        .route
                                )
                        }
                    )


                    ActionCard(

                        item =
                            menuItems[3],

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {

                            navController
                                .navigate(
                                    menuItems[3]
                                        .route
                                )
                        }
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        88.dp
                    )
            )
        }
    }


    // ========================================================
    // ASISTENTE MOSKICHECK
    // ========================================================

    if (mostrarAsistente) {

        MoskiCheckAssistantDialog(

            onDismiss = {
                mostrarAsistente =
                    false
            }
        )
    }
}


@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    numero: String,
    label: String,
    color: Color
) {

    Card(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(
                10.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MoskiSurface
                ),

        elevation =
            CardDefaults
                .cardElevation(
                    0.dp
                ),

        border =
            androidx.compose
                .foundation
                .BorderStroke(
                    0.5.dp,
                    MoskiBorder
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical =
                            10.dp
                    ),

            horizontalAlignment =
                Alignment
                    .CenterHorizontally
        ) {

            Text(

                text =
                    numero,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight
                        .SemiBold,

                color =
                    color
            )


            Text(

                text =
                    label,

                fontSize =
                    10.sp,

                color =
                    MoskiTextHint
            )
        }
    }
}


@Composable
fun ActionCard(
    item: MenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(

        modifier =
            modifier
                .height(
                    110.dp
                )
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(
                14.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MoskiSurface
                ),

        elevation =
            CardDefaults
                .cardElevation(
                    0.dp
                ),

        border =
            androidx.compose
                .foundation
                .BorderStroke(
                    0.5.dp,
                    MoskiBorder
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        14.dp
                    ),

            verticalArrangement =
                Arrangement.Center
        ) {

            Box(

                modifier =
                    Modifier
                        .size(
                            40.dp
                        )
                        .background(
                            item.iconBackground,
                            RoundedCornerShape(
                                10.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    item.icono,

                    contentDescription =
                        null,

                    tint =
                        item.iconTint,

                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(

                text =
                    item.titulo,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight
                        .SemiBold,

                color =
                    MoskiTextPrimary,

                lineHeight =
                    15.sp
            )


            Text(

                text =
                    item.subtitulo,

                fontSize =
                    10.sp,

                color =
                    MoskiTextHint,

                lineHeight =
                    13.sp
            )
        }
    }
}