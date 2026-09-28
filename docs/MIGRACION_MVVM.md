# Migración aplicada a MedicimApp

## Plan y límites

Flujo: Composable → ViewModel → Repository → ApiService → Railway.

Se conservan los colores, componentes Material 3, rutas `home`, `reports`, `report`, `guide`, `health_centers`, la URL `https://mi-app-backend-production-01e4.up.railway.app/api` y los contratos JSON actuales. La corrección solicitada del error de alerta usa un estado explícito de información no disponible.

1. Revisar pantallas, estados y contratos; añadir únicamente Lifecycle ViewModel Compose y Runtime Compose.
2. Crear repositorios, contenedor y factory con inyección manual.
3. Migrar primero Reportes: listado, totales, ranking, loading/error.
4. Migrar Inicio: estadísticas y alerta de Tarapoto con Loading/Success/Error.
5. Migrar formulario: campos, validación, envío, éxito/error y restauración.
6. Migrar MoskiCheck: preguntas, respuestas, catorce campos, resultado y sesión.
7. Extraer catálogos locales de Guía y Centros y sus ViewModels.
8. Extraer los siete modelos del cliente HTTP a `data/model`.
9. Distinguir timeout, HTTP, conexión y respuesta inválida, respetando cancelación.
10. Compilar, probar lógica y revisar rutas, recursos y solicitudes de Railway.

Cada etapa se valida con `./gradlew assembleDebug` antes de comenzar la siguiente.

## Pantallas y estados identificados

| Pantalla | Estados que pasan al ViewModel |
|---|---|
| ReportsScreen | zonas, reportes recientes, estadísticas, loading, error |
| HomeScreen | totales, alerta, estados de carga/error por consulta |
| ReportScreen | campos, anonimato, validación, envío, éxito/error |
| MoskiCheckAssistantDialog | preguntas, respuestas, inicio, índice, envío, resultado/error |
| GuideScreen | catálogo y filtro por categoría |
| HealthCentersScreen | catálogo y tab seleccionado |

Los launchers de imágenes, navegación, objetos de mapa y el dibujo de componentes permanecen en la interfaz.

## Contratos de red conservados

| Método | Ruta | Consumidor |
|---|---|---|
| GET | `/api/reportes` | Reportes |
| GET | `/api/reportes/por-distrito` | Reportes |
| GET | `/api/reportes/total` | Reportes e Inicio |
| GET | `/api/alertas/hoy?distrito=Tarapoto` | Inicio |
| POST | `/api/reportes` | Crear reporte |
| POST | `/api/diagnosticos/analizar` | MoskiCheck |

La selección de imagen conserva su comportamiento actual de previsualización. La subida de imagen requiere un contrato adicional y queda fuera de esta migración.

## Verificación

Se ejecutó `./gradlew assembleDebug` después de cada fase, con resultado `BUILD SUCCESSFUL` antes de continuar:

| Fase | Resultado |
|---|---|
| Estado inicial y preparación de dependencias | Compila |
| Arquitectura base e inyección manual | Compila |
| ReportsScreen | Compila |
| HomeScreen | Compila |
| ReportScreen | Compila |
| MoskiCheck | Compila |
| Guía y Centros de salud | Compila |
| Extracción de modelos | Compila |
| Manejo de errores y cancelación HTTP | Compila |
| Pruebas finales | Compila y pruebas aprobadas |

Validaciones del 28 de septiembre de 2026:

- 23 pruebas unitarias aprobadas: 16 de ViewModels/restauración/contratos, 6 de transporte HTTP y 1 existente.
- 10 pruebas instrumentadas aprobadas en Pixel 8, Android 15: navegación existente, pantallas, filtros, recreación, errores, formulario y cuestionario. La suite pasó con Gradle antes del último ajuste del parser y el APK final pasó por ejecución directa de ADB: `OK (10 tests)` en 13,236 s.
- `lintDebug` sin errores; informa 36 advertencias que no bloquean la compilación.
- Las cuatro rutas GET de Railway respondieron HTTP 200. La alerta real conserva `ok`, `nivel`, `descripcion` y `total_reportes`; Railway omite `distrito`, por lo que se mantiene el distrito solicitado como referencia.
- No hay referencias a `ApiService` en pantallas ni ViewModels. Los repositorios son el único acceso desde presentación a la capa de red.
- `MainActivity.kt`, `AppNavigation.kt` y los archivos de tema no se modificaron. Se compararon las referencias a colores y las medidas `dp`/`sp` de las seis interfaces con su versión inicial.
- La URL, los seis contratos de ruta y las claves de los payloads JSON permanecen iguales.
- La revisión de diferencias no encontró errores de formato.

Los envíos de reporte y evaluación se probaron con repositorios simulados, sin crear registros en producción. La comprobación real de Railway fue de lectura. No se realizó una comparación automática de capturas píxel a píxel ni una prueba del selector de fotos con todas las aplicaciones de galería.

## Cambios de comportamiento solicitados

- Inicio distingue `Loading`, `Success` y `Error` para totales y alerta. Una consulta fallida muestra `No disponible`, y nunca se convierte en riesgo bajo.
- Las estadísticas ausentes dejan de presentarse como ceros reales.
- Los campos del formulario, anonimato y URI seleccionada se conservan mediante `SavedStateHandle`; una recreación no provoca un segundo envío.
- Las respuestas e índice de MoskiCheck se conservan. La rotación retiene el ViewModel y su resultado. Tras restaurar un proceso con cuestionario completo, se solicita procesar nuevamente en vez de enviar automáticamente.
- Cerrar o reiniciar MoskiCheck cancela su solicitud y evita que una respuesta de una sesión anterior cambie la siguiente.
- Se distinguen timeout, conexión, HTTP y respuesta inválida. Un nivel de alerta desconocido o un contador obligatorio ausente se consideran una respuesta inválida.
- Se conservan los tiempos de espera existentes: conexión 15 s, lectura 20 s y escritura 15 s. La cancelación de la coroutine cancela también la llamada OkHttp.

## Archivos creados

39 archivos Kotlin de producción; las rutas siguientes son relativas a `app/src/main/java/com/dvt/medicimapp/`:

```text
data/model/
  AlertaApi.kt
  ApiError.kt
  CentroSalud.kt
  EvaluacionMoskiCheckRequest.kt
  GuiaItem.kt
  PreguntaMoskiCheck.kt
  ProbabilidadesMoskiCheckApi.kt
  ReporteApi.kt
  ReporteZonaApi.kt
  ResultadoMoskiCheckApi.kt
  TotalesApi.kt
data/repository/
  ApiHomeRepository.kt
  ApiMoskiCheckRepository.kt
  ApiReportRepository.kt
  ApiReportsRepository.kt
  GuideRepository.kt
  HealthCentersRepository.kt
  HomeRepository.kt
  LocalGuideRepository.kt
  LocalHealthCentersRepository.kt
  MoskiCheckQuestions.kt
  MoskiCheckRepository.kt
  ReportRepository.kt
  ReportsRepository.kt
  RepositoryResult.kt
di/
  AppContainer.kt
  AppViewModelFactory.kt
presentation/state/
  GuideUiState.kt
  HealthCentersUiState.kt
  HomeUiState.kt
  MoskiCheckUiState.kt
  ReportUiState.kt
  ReportsUiState.kt
presentation/viewmodel/
  GuideViewModel.kt
  HealthCentersViewModel.kt
  HomeViewModel.kt
  MoskiCheckViewModel.kt
  ReportViewModel.kt
  ReportsViewModel.kt
```

Además, se crean cuatro archivos de pruebas y este documento, con un total de 44 archivos nuevos en esta migración:

```text
app/src/test/java/com/dvt/medicimapp/data/ApiServiceHttpTest.kt
app/src/test/java/com/dvt/medicimapp/presentation/viewmodel/MvvmViewModelTest.kt
app/src/androidTest/java/com/dvt/medicimapp/MigrationNavigationTest.kt
app/src/androidTest/java/com/dvt/medicimapp/MvvmScreenTest.kt
docs/MIGRACION_MVVM.md
```

`docs/PLAN_MIGRACION_MVVM.md` ya existía como documento no versionado y se conserva como antecedente; no forma parte del inventario creado en esta ejecución.

## Archivos modificados

Diez archivos existentes:

```text
app/build.gradle.kts
gradle/libs.versions.toml
gradlew
app/src/main/java/com/dvt/medicimapp/data/ApiService.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/reports/ReportsScreen.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/home/HomeScreen.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/home/MoskiCheckAssistantDialog.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/report/ReportScreen.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/guide/GuideScreen.kt
app/src/main/java/com/dvt/medicimapp/presentation/screens/healthcenters/HealthCentersScreen.kt
```

`gradlew` solo cambia su permiso de ejecución. En Gradle se añaden únicamente `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`, usando el alias de versión Lifecycle ya existente; Compose y el resto del toolchain conservan sus versiones.

## Problemas encontrados y resueltos

- El wrapper Gradle no tenía permiso de ejecución; se habilitó para permitir `./gradlew`.
- La caché de Gradle y el acceso al emulador requieren permisos del entorno de ejecución; no se modificó la configuración de la aplicación por ese motivo.
- Un selector de prueba de Guía encontraba tanto el filtro `Personal` como sus tarjetas. Se corrigió para seleccionar el filtro.
- Desconexiones intermitentes de ADB interrumpieron algunas ejecuciones de `connectedDebugAndroidTest`, sin registrar una aserción fallida. El emulador no se reinició. La suite completa del APK final se ejecutó directamente con ADB y pasó; la intermitencia del recolector de Gradle queda como limitación del entorno.
- Había valores por defecto que podían presentar datos ausentes como información válida. Se eliminaron los valores de riesgo y estadísticas que ocultaban fallos.
- Quedan advertencias de APIs existentes como `PreferenceManager` de osmdroid y `menuAnchor()`. Su actualización se mantiene fuera del alcance visual de esta migración.

## Comandos de validación

Desde la raíz del proyecto:

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
# Requiere un emulador o dispositivo conectado:
./gradlew connectedDebugAndroidTest
```

Ante la desconexión intermitente del recolector de Gradle, la verificación final utilizó los APK ya compilados y estos comandos, todos con resultado satisfactorio:

```sh
/home/juanjo/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
/home/juanjo/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
/home/juanjo/Android/Sdk/platform-tools/adb shell am instrument -w -r com.dvt.medicimapp.test/androidx.test.runner.AndroidJUnitRunner
```

APK generado: `app/build/outputs/apk/debug/app-debug.apk`.

## Recomendaciones siguientes

1. Mantener estas suites en CI y añadir pruebas de contrato contra un entorno de pruebas del backend.
2. Revisar las advertencias y actualizar dependencias en un cambio separado, comprobando navegación, mapas y estilos.
3. Acordar un contrato de subida de imágenes antes de implementar la carga de la foto, que actualmente solo se previsualiza.
4. Añadir recarga y paginación de reportes cuando se defina su comportamiento de producto, aprovechando los repositorios y ViewModels nuevos.
5. Si aparece lógica de negocio compartida entre pantallas, extraer casos de uso; la inyección manual actual permite hacerlo sin introducir Hilt todavía.
