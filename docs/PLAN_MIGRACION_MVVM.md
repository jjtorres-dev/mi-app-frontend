# Plan de migración gradual a MVVM

Este documento describe una migración del proyecto actual; no representa cambios ya implementados. Las referencias de línea corresponden al código revisado el 28 de septiembre de 2026.

## Objetivo y orden

Cada pantalla obtiene sus datos mediante este flujo:

```text
Screen (conexión con navegación y ViewModel)
  └─ Content (dibuja UiState y entrega acciones)
       └─ ViewModel → Repository → ApiService o catálogo local
```

El ViewModel expone `StateFlow<UiState>` inmutable. La pantalla lo observa con `collectAsStateWithLifecycle()`. Los eventos del usuario invocan métodos del ViewModel o callbacks de navegación. Los ViewModels reciben repositorios por constructor.

Orden: infraestructura → Reportes de Zona → Inicio → Reportar Criadero → asistente MoskiCheck → Guía → Centros de Salud → extracción de modelos de API.

El primer código que se traslada de una pantalla es el estado y el bloque `LaunchedEffect(Unit)` de `ReportsScreen.kt`, líneas actuales 31 a 62, hacia `ReportsUiState.kt` y `ReportsViewModel.kt`.

Todas las rutas conservan sus nombres: `home`, `health_centers`, `report`, `guide`, `reports`. Cada función existente `XScreen(navController)` continúa como entrada de su destino. `MoskiCheckAssistantDialog(onDismiss)` también continúa como entrada del diálogo. Así se migra una función por vez sin cambiar todas las llamadas de navegación.

Los paths de Kotlin indicados a continuación son relativos a:

```text
app/src/main/java/com/dvt/medicimapp/
```

## 0. Registrar una referencia verificable

Antes de mover código:

1. Compilar y abrir las cinco pantallas; registrar capturas, textos, navegación y mensajes de error.
2. Comprobar Inicio → cada destino → volver, los tabs Mapa/Lista, filtros de Guía y selección de foto.
3. Registrar el comportamiento actual al rotar, al estar sin conexión y al cerrar/reabrir MoskiCheck.
4. Añadir `app/src/androidTest/java/com/dvt/medicimapp/MigrationNavigationTest.kt`: comprobar los cinco destinos y el regreso a Inicio. Evitar depender de nombres o cantidades cambiantes de producción.

Ejecutar como referencia:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Los tests instrumentados requieren un emulador o dispositivo:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Guardar cada etapa en un commit independiente cuando sus comprobaciones pasen. Una regresión se corrige o se revierte en esa etapa antes de migrar la siguiente pantalla.

## 1. Preparar repositorios e inyección manual

### Modificar dependencias

En `gradle/libs.versions.toml`, añadir entradas para:

- `androidx.lifecycle:lifecycle-viewmodel-ktx`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.lifecycle:lifecycle-runtime-compose`
- `androidx.lifecycle:lifecycle-viewmodel-savedstate`

Usar inicialmente la versión Lifecycle existente, `2.6.1`, mediante `version.ref = "lifecycleRuntimeKtx"`. Añadir sus `implementation(...)` en `app/build.gradle.kts`.

Para los tests de corrutinas añadir `org.jetbrains.kotlinx:kotlinx-coroutines-test`, con la misma versión que `kotlinx-coroutines-core` resuelta en la aplicación. Determinarla mediante:

```sh
./gradlew :app:dependencyInsight --dependency kotlinx-coroutines-core --configuration debugRuntimeClasspath
```

### Crear archivos

| Archivo | Contenido |
|---|---|
| `data/repository/ReportesRepository.kt` | Interfaz con las firmas actuales de `obtenerReportes`, `obtenerReportesPorDistrito`, `obtenerTotales` y `enviarReporte`. |
| `data/repository/ApiReportesRepository.kt` | Implementación que delega esos cuatro métodos a `ApiService`. |
| `data/repository/AlertasRepository.kt` | Interfaz con `obtenerAlertaHoy(distrito: String): Result<AlertaApi>`. |
| `data/repository/ApiAlertasRepository.kt` | Implementación que delega a `ApiService.obtenerAlertaHoy`. |
| `data/repository/MoskiCheckRepository.kt` | Interfaz con `analizarSintomas(evaluacion: EvaluacionMoskiCheckRequest): Result<ResultadoMoskiCheckApi>`. |
| `data/repository/ApiMoskiCheckRepository.kt` | Implementación que delega a `ApiService.analizarSintomas`. |
| `di/AppContainer.kt` | Contenedor compartido de repositorios; añadir los catálogos locales cuando se migren sus pantallas. |
| `di/AppViewModelFactory.kt` | `ViewModelProvider.Factory` que recibe el contenedor y construye ViewModels por constructor. Usar `CreationExtras.createSavedStateHandle()` en los que necesitan restauración. |

Las interfaces conservan los modelos, argumentos y retornos `Result<T>` actuales. En esta etapa los repositorios son adaptadores sobre el cliente HTTP existente.

`ApiService.kt` sigue realizando HTTP y parseando JSON. La infraestructura se incorpora primero; cada pantalla empieza a usarla en su propia etapa.

Los ViewModels se obtienen con `viewModel(factory = ...)` dentro del destino de Navigation Compose, usando el `NavBackStackEntry` como propietario. No construirlos directamente en el cuerpo de un Composable ni compartir un único ViewModel entre todas las pantallas.

**Validación:** el proyecto compila y las pantallas existentes siguen ejecutándose antes de conectarlas con los repositorios.

## 2. Migrar Reportes de Zona: primera pantalla

Crear:

```text
presentation/screens/reports/ReportsUiState.kt
presentation/screens/reports/ReportsViewModel.kt
presentation/screens/reports/ReportsContent.kt
```

Modificar `presentation/screens/reports/ReportsScreen.kt`.

### Mover primero, exactamente

| Código actual | Destino |
|---|---|
| `zonas`, `reportesRecientes`, `totalReportes`, `totalDistritos`, `cargando`, `error` | Propiedades de `ReportsUiState`. |
| Todo el `LaunchedEffect(Unit)` que consulta las tres rutas y procesa `fold` | Método `cargar()` de `ReportsViewModel`, ejecutado con `viewModelScope`. |
| `it.take(10)` aplicado a los reportes | Transformación de datos dentro del ViewModel. |
| Cálculo de `masAfectado` | Propiedad derivada del estado o cálculo puro del ViewModel. |
| `Scaffold`, tarjetas, ranking, lista, indicadores y mensajes | `ReportsContent.kt`. |
| `StatCard`, `colorDeRanking`, `colorDeTipo` y cálculo de progreso visual | Permanecen en la capa de presentación. |

`ReportsViewModel` recibe `ReportesRepository`. Una carga inicial se dispara desde `init`; eliminar el disparador anterior del Composable para evitar dos cargas. Guardar el `Job` de carga o impedir cargas superpuestas; `reintentar()` permite una nueva carga explícita.

Primero trasladar las tres consultas con su secuencia y tratamiento actuales. Los cambios de paralelismo, caché o presentación parcial se hacen después de verificar la extracción.

`ReportsScreen(navController)` pasa a realizar únicamente:

1. Obtener el ViewModel de su destino.
2. Observar `uiState` con `collectAsStateWithLifecycle()`.
3. Invocar `ReportsContent(uiState, onBack, onRetry)`.

`onBack` ejecuta `navController.popBackStack()` en la pantalla de entrada; el contenido y el ViewModel no reciben `NavController`.

**Validación:** carga, error, lista vacía, diez registros visibles, ranking, regreso y rotación. La rotación no crea una segunda carga del mismo ViewModel. Comprobar fallos individuales de las tres consultas. Cubrir nombre vacío y totales cero al endurecer la presentación.

Crear `app/src/test/java/com/dvt/medicimapp/presentation/screens/reports/ReportsViewModelTest.kt`.

## 3. Migrar Inicio

Crear:

```text
presentation/screens/home/HomeUiState.kt
presentation/screens/home/HomeViewModel.kt
presentation/screens/home/HomeContent.kt
```

Modificar `presentation/screens/home/HomeScreen.kt`.

Trasladar:

- `totalReportes`, `totalDistritos`, `nivelAlerta`, `descAlerta`, `cargandoStats` a `HomeUiState`.
- El bloque `LaunchedEffect(Unit)` actual, líneas 213 a 263, a `HomeViewModel.cargar(distrito)`.
- Consultas a `obtenerTotales()` y `obtenerAlertaHoy()` mediante `ReportesRepository` y `AlertasRepository`.
- El valor inicial del distrito a una propiedad explícita del ViewModel o sus argumentos; comenzar con el mismo distrito actual para comparar resultados.

Los colores, títulos visibles, tarjetas, `MenuItem`, `NavItem`, `StatChip`, `ActionCard` y el consejo mostrado siguen en presentación. `HomeContent` recibe callbacks para navegar y abrir el asistente. La entrada `HomeScreen` coordina el diálogo y observa el ViewModel.

La visibilidad del asistente es estado de interfaz; usar `rememberSaveable` o un estado de sesión explícito. Derivar la selección de navegación del destino cuando se revise la barra inferior.

Después de validar que los datos y accesos se conservaron, hacer un cambio separado para representar errores de alerta como “sin información”, con estilo neutro. Actualmente se convierten en nivel `bajo`; ese comportamiento requiere una corrección identificable y su propio test. Modelar la alerta disponible, la carga y el error por separado.

**Validación:** ambas consultas, fallos independientes, cinco rutas, botón del asistente y rotación. La apertura del asistente no vuelve a cargar las estadísticas.

Crear `app/src/test/java/com/dvt/medicimapp/presentation/screens/home/HomeViewModelTest.kt`.

## 4. Migrar Reportar Criadero

Crear:

```text
presentation/screens/report/ReportUiState.kt
presentation/screens/report/ReportViewModel.kt
presentation/screens/report/ReportContent.kt
```

Modificar `presentation/screens/report/ReportScreen.kt`.

Trasladar al ViewModel y su estado:

- `nombre`, `distrito`, `barrio`, `descripcion`, `tipoCriadero`, `esAnonimo`.
- Validación actual `distrito.isNotBlank() && tipoCriadero.isNotBlank() && (esAnonimo || nombre.isNotBlank())`.
- `enviando`, error y resultado exitoso pendiente de confirmar.
- El bloque `scope.launch` actual dentro de “Enviar reporte”, líneas 154 a 172, a `enviar()`.
- La preparación de argumentos: nombre nulo si es anónimo y conversión de barrio/descripción vacíos a nulo.

El ViewModel recibe `ReportesRepository`. Añadir métodos para actualizar cada campo, `enviar()`, `cerrarError()` y `confirmarExito()`.

Usar `SavedStateHandle` para los campos pequeños que se decida restaurar. Tras muerte del proceso, restaurar el formulario sin reenviar automáticamente una operación pendiente. El estado “enviando” no demuestra que el servidor haya confirmado la operación.

Permanecen en Compose el launcher `ActivityResultContracts.GetContent`, la previsualización de imagen y la apertura de dropdowns. Para la foto, conservar una URI como texto si se necesita restauración y comprobar que siga siendo accesible. El ViewModel no guarda `Activity`, `Context`, launchers ni bitmaps.

`ReportContent` dibuja el formulario y los diálogos. Al aceptar el éxito, el callback de `ReportScreen` confirma el estado y ejecuta `popBackStack()`. La navegación se produce por esa acción explícita, evitando repetirla al recrear la pantalla.

Proteger `enviar()` frente a dobles pulsaciones mediante un estado o trabajo activo. La migración usa el mismo contrato JSON actual; la subida de foto se incorpora como funcionalidad aparte cuando exista su contrato de backend.

**Validación:** formulario normal y anónimo, campos obligatorios, selección de foto, doble pulsación, éxito, error, rotación y restauración sin reenvío automático.

Crear:

```text
app/src/test/java/com/dvt/medicimapp/presentation/screens/report/ReportViewModelTest.kt
app/src/androidTest/java/com/dvt/medicimapp/ReportStateRestorationTest.kt
```

## 5. Migrar el asistente MoskiCheck

Crear:

```text
presentation/screens/home/MoskiCheckUiState.kt
presentation/screens/home/MoskiCheckViewModel.kt
presentation/screens/home/PreguntasMoskiCheck.kt
presentation/screens/home/EvaluacionMoskiCheckMapper.kt
presentation/screens/home/MoskiCheckAssistantContent.kt
```

Modificar `presentation/screens/home/MoskiCheckAssistantDialog.kt` y la coordinación de apertura/cierre en `HomeScreen.kt`.

| Código actual del diálogo | Destino |
|---|---|
| `PreguntaMoskiCheck` y `preguntasMoskiCheck`, desde la línea 62 | `PreguntasMoskiCheck.kt`; declarar los elementos compartidos con visibilidad `internal`. |
| `respuestas`, `iniciado`, `indicePregunta`, `cargando`, `resultado`, `error`, líneas 143 a 165 | `MoskiCheckUiState` y su actualización en el ViewModel. |
| `reiniciarEvaluacion()`, línea 168 | `MoskiCheckViewModel.reiniciarEvaluacion()`. |
| `crearSolicitud(datos)`, línea 180 | Función pura en `EvaluacionMoskiCheckMapper.kt`. |
| `enviarEvaluacion(datos)`, línea 214 | Método del ViewModel que usa `MoskiCheckRepository` y `viewModelScope`. |
| `responder(valor)`, línea 248 | Método del ViewModel. |
| `Dialog`, botones, progreso y tarjetas de resultados | `MoskiCheckAssistantContent.kt`. |
| `ResultadoEvaluacion`, `porcentaje`, `nombreAlerta` | Funciones de presentación junto al contenido. |

El mapper conserva exactamente los once síntomas y tres señales de seguridad, los nombres actuales de campos y los valores `0`/`1`.

### Alcance de la sesión

El ViewModel del asistente pertenece al destino Inicio. Como sobrevive a cerrar un Composable condicional, definir explícitamente el inicio y fin de sesión:

- Abrir una evaluación nueva inicializa sus respuestas.
- La rotación conserva la sesión que está abierta.
- Cerrar o reiniciar cancela el trabajo correspondiente y limpia el estado según la acción.
- Una respuesta tardía de una sesión cerrada no puede modificar una nueva sesión: usar un identificador de sesión o comprobar el trabajo activo.
- Conservar la regla actual de impedir el cierre mientras se procesa, hasta que exista un cambio específico de ese comportamiento.

La cancelación de corrutina debe propagarse hasta el cliente HTTP. Endurecer esa integración como un cambio independiente si el cliente actual continúa una llamada bloqueante. `catch (Exception)` no debe convertir `CancellationException` en un error de evaluación.

**Validación:** orden de catorce preguntas, payload exacto, reintento, reinicio, cerrar/reabrir, rotación, resultados `orientation`, `inconclusive` y `alert`, probabilidades nulas y errores. Los tests usan repositorios falsos; no envían evaluaciones a producción.

Crear:

```text
app/src/test/java/com/dvt/medicimapp/presentation/screens/home/MoskiCheckViewModelTest.kt
app/src/test/java/com/dvt/medicimapp/presentation/screens/home/EvaluacionMoskiCheckMapperTest.kt
app/src/androidTest/java/com/dvt/medicimapp/MoskiCheckSessionTest.kt
```

## 6. Migrar Guía de Prevención

Crear:

```text
data/GuiaItem.kt
data/repository/GuideRepository.kt
data/repository/LocalGuideRepository.kt
presentation/screens/guide/GuideUiState.kt
presentation/screens/guide/GuideViewModel.kt
presentation/screens/guide/GuideContent.kt
```

Modificar `GuideScreen.kt`, el contenedor y la factory.

- Mover `GuiaItem`, línea 28, a `data/GuiaItem.kt`, con package `com.dvt.medicimapp.data`; actualizar su import.
- Mover la lista actual de doce recomendaciones y sus categorías a `LocalGuideRepository`.
- El repositorio expone `obtenerGuias(): List<GuiaItem>`.
- Mover la categoría seleccionada y el filtrado al ViewModel; restaurar la categoría mediante `SavedStateHandle`.
- Conservar colores, iconos, animaciones y expansión de tarjetas en Compose, con estado guardable y claves estables por recomendación.

Un ViewModel es útil aquí para separar el catálogo y el filtro; el estado visual de cada tarjeta sigue perteneciendo a la interfaz.

**Validación:** doce recomendaciones, cuatro filtros, expansión y rotación.

Crear `app/src/test/java/com/dvt/medicimapp/presentation/screens/guide/GuideViewModelTest.kt`.

## 7. Migrar Centros de Salud

Crear:

```text
data/CentroSalud.kt
data/repository/HealthCentersRepository.kt
data/repository/LocalHealthCentersRepository.kt
presentation/screens/healthcenters/HealthCentersUiState.kt
presentation/screens/healthcenters/HealthCentersViewModel.kt
presentation/screens/healthcenters/HealthCentersContent.kt
```

Modificar `HealthCentersScreen.kt`, el contenedor y la factory.

- Mover `CentroSalud`, línea 38, a `data/CentroSalud.kt`, con package `com.dvt.medicimapp.data`; actualizar su import.
- Mover la lista actual de seis establecimientos, desde la línea 52, a `LocalHealthCentersRepository`.
- El repositorio expone `obtenerCentros(): List<CentroSalud>`.
- El ViewModel entrega los establecimientos mediante `HealthCentersUiState`.
- El tab Mapa/Lista permanece como estado guardable de interfaz.
- `MapView`, `GeoPoint`, `Marker`, configuración osmdroid, `AndroidView`, `DisposableEffect`, `Context` e `Intent.ACTION_DIAL` permanecen en Compose. Son integración de la vista con Android, no lógica de datos.
- Gestionar el ciclo de vida del mapa desde la capa de interfaz y comprobar sus pausas, reanudaciones y liberación.

**Validación:** seis marcadores, lista, teléfonos, cambio de tabs, regreso, rotación y ausencia de referencias a una Activity destruida.

Crear `app/src/test/java/com/dvt/medicimapp/presentation/screens/healthcenters/HealthCentersViewModelTest.kt`.

## 8. Extraer los modelos del archivo de API

Cuando las pantallas ya usen repositorios, crear:

```text
data/ReporteApi.kt
data/ReporteZonaApi.kt
data/AlertaApi.kt
data/TotalesApi.kt
data/EvaluacionMoskiCheckRequest.kt
data/ProbabilidadesMoskiCheckApi.kt
data/ResultadoMoskiCheckApi.kt
```

Mover las siete `data class` actuales de `ApiService.kt` a esos archivos. Conservar su package `com.dvt.medicimapp.data`, nombres, tipos y propiedades. Esta separación organiza el cliente sin cambiar los imports existentes ni el contrato con Railway.

Los modelos de interfaz `MenuItem` y `NavItem` continúan en presentación. Crear modelos de dominio adicionales solo cuando aparezcan reglas que lo justifiquen.

**Validación:** compilación, tests y payloads/respuestas con el mismo contrato anterior.

## 9. Comprobar el resultado completo

Crear estos apoyos de tests a medida que se necesitan:

```text
app/src/test/java/com/dvt/medicimapp/testsupport/MainDispatcherRule.kt
app/src/test/java/com/dvt/medicimapp/testsupport/FakeReportesRepository.kt
app/src/test/java/com/dvt/medicimapp/testsupport/FakeAlertasRepository.kt
app/src/test/java/com/dvt/medicimapp/testsupport/FakeMoskiCheckRepository.kt
```

Los repositorios falsos deben permitir respuestas controladas, retrasos y fallos. Para los catálogos locales bastan pequeñas implementaciones de prueba dentro de sus tests.

Criterios para finalizar:

1. Las cinco rutas y el diálogo funcionan y conservan sus acciones.
2. Las pantallas de presentación no importan ni llaman a `ApiService`.
3. Los ViewModels reciben repositorios por constructor y no guardan `NavController`, `Activity`, `Context` ni `MapView`.
4. La recreación de pantallas conserva el estado previsto y no repite envíos.
5. La reapertura del asistente inicia una sesión nueva y sus respuestas tardías no contaminan otra sesión.
6. Las consultas y operaciones conservan los contratos de Railway.
7. Compilación, tests unitarios, tests instrumentados y Lint pasan, o los problemas previos están identificados y no aumentaron.

## Cambios posteriores a la extracción

Tras validar MVVM, se pueden abordar por separado: consultas en paralelo, paginación, actualización después de un reporte, configuración de entornos, subida real de fotos, endurecimiento del cliente HTTP y actualización de dependencias.

La exposición pública de identidades detectada en `/api/reportes` requiere una corrección en el backend y debe tener seguimiento propio.

## Referencias

- [Recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations): repositorios, ViewModels, estado observable, inyección y pruebas.
- [Creación de ViewModels con dependencias](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories): factories y `CreationExtras`.
- [Conservación del estado en Compose](https://developer.android.com/develop/ui/compose/state-saving): `rememberSaveable`, `SavedStateHandle` y restauración mínima.
- [Buenas prácticas de corrutinas](https://developer.android.com/kotlin/coroutines/coroutines-best-practices): propagación de cancelación y manejo de excepciones.
