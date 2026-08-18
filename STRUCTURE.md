# 🏗️ Estructura del Proyecto - Game Booster Turbo

Este documento detalla la organización de directorios, capas de arquitectura modular y flujo de datos de la aplicación.

---

## 📂 Árbol de Directorios del Proyecto

```
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # Permisos, ShizukuProvider, queries de paquetes
│   │   │   ├── cpp/                         # Capa Nativa C++ (Android NDK)
│   │   │   │   ├── CMakeLists.txt           # Configuración de compilación CMake
│   │   │   │   └── native-lib.cpp           # Funciones JNI y telemetría de bajo nivel
│   │   │   ├── rust/                        # Capa Nativa Rust
│   │   │   │   ├── Cargo.toml               # Dependencias del crate Rust (jni, libc)
│   │   │   │   └── src/
│   │   │   │       └── lib.rs               # Implementación FFI en Rust
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt          # Actividad principal y refresco de ciclo de vida
│   │   │   │   ├── data/                    # Capa de datos y persistencia
│   │   │   │   │   ├── BoosterPreferences.kt# Fachada unificada de persistencia SharedPreferences
│   │   │   │   │   └── preferences/         # Módulos especializados de preferencias por dominio
│   │   │   │   │       ├── GameConfigPreferences.kt   # Drivers GPU, escalas de resolución y flags por juego
│   │   │   │   │       ├── GamerDndPreferences.kt     # Modo DND Gamer, llamadas, heads-up y excepciones
│   │   │   │   │       ├── HibernationPreferences.kt  # Whitelist de excepciones y blacklist de hibernación
│   │   │   │   │       ├── TouchPreferences.kt        # Overclock táctil (pointer_speed), 120Hz y animaciones
│   │   │   │   │       ├── NetworkPreferences.kt      # Wi-Fi alto rendimiento y optimizaciones de energía
│   │   │   │   │       ├── CrosshairPreferences.kt    # Retícula táctica, estilo, tamaño y color
│   │   │   │   │       └── BoosterStatsPreferences.kt # Contador de optimizaciones y memoria RAM liberada
│   │   │   │   ├── model/                   # Modelos de datos
│   │   │   │   │   ├── DeviceMetrics.kt     # Métricas de RAM, batería, ping, pantalla
│   │   │   │   │   ├── DisplayResolutionScale.kt # Perfiles y métricas de escala de resolución y DPI
│   │   │   │   │   └── GameItem.kt          # Modelo de juegos, perfiles y resultados de boost
│   │   │   │   ├── receiver/                # Receptores de difusión del sistema
│   │   │   │   │   ├── BootRecoveryReceiver.kt # Fail-safe de arranque para restaurar drivers, resolución y GMS
│   │   │   │   │   └── EmergencyResetReceiver.kt # Botón de pánico para restablecer resolución y DPI al instante
│   │   │   │   ├── service/                 # Servicios en segundo plano
│   │   │   │   │   ├── GameWatcherService.kt# Centinela en juego: hibernación, resolución, monitoreo y reversión
│   │   │   │   │   ├── GameOverlayService.kt# Servicio Foreground para HUD Flotante in-game
│   │   │   │   │   └── overlay/             # Módulos del HUD Flotante y telemetría in-game
│   │   │   │   │       ├── DraggableOverlayWindowManager.kt # Ventana flotante, layout y gestos de arrastre
│   │   │   │   │       ├── OverlayLifecycleOwner.kt         # Ciclo de vida y SavedStateRegistry para WindowManager
│   │   │   │   │       ├── FpsTracker.kt                    # Conteo de FPS en tiempo real vía Choreographer
│   │   │   │   │       ├── OverlayResolutionTester.kt       # Cronómetro y auto-revert de 15s en prueba de resolución
│   │   │   │   │       └── OverlayGamerActions.kt           # Acciones in-game: DND, hibernación, drivers y quick boost
│   │   │   │   ├── ui/                      # Capa de Presentación (Jetpack Compose)
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── AppPickerSheet.kt       # Selector de aplicaciones instaladas
│   │   │   │   │   │   ├── BoostDialog.kt          # Diálogo con barra de progreso y reporte
│   │   │   │   │   │   ├── BoostProfileSelector.kt # Selector de perfiles (Ultra/Batería/Red)
│   │   │   │   │   │   ├── BoosterHeaderBar.kt     # Barra de encabezado, marca, estados y accesos directos
│   │   │   │   │   │   ├── GameConfigSheet.kt      # Menú modal orquestador de ajustes de rendimiento
│   │   │   │   │   │   ├── GameLauncherSection.kt  # Cuadrícula y lista de juegos con badges de driver
│   │   │   │   │   │   ├── GameOverlayHudView.kt   # Fachada coordinadora de la vista flotante in-game
│   │   │   │   │   │   ├── GamerHudGauge.kt        # Tacómetro/indicador circular interactivo
│   │   │   │   │   │   ├── MetricCards.kt          # Tarjetas de CPU real, RAM, Ping, Batería, Almacenamiento
│   │   │   │   │   │   ├── QuickToolsBanner.kt     # Banner de limpieza rápida de memoria y optimización
│   │   │   │   │   │   ├── ShizukuControlCard.kt   # Tarjeta de estado y permisos de Shizuku
│   │   │   │   │   │   ├── SpeedTestDialog.kt      # Diagnóstico de latencia en vivo
│   │   │   │   │   │   ├── sheet/                  # Subcomponentes modulares del menú de ajustes
│   │   │   │   │   │   │   ├── SheetHeaderSection.kt              # Encabezado del juego y botón cerrar
│   │   │   │   │   │   │   ├── SheetResolutionCountdownBanner.kt  # Banner de prueba de 15s con auto-revert
│   │   │   │   │   │   │   ├── SheetDriversSection.kt             # Selección de Vulkan / ANGLE / OpenGL
│   │   │   │   │   │   │   ├── SheetResolutionSection.kt          # Escala de resolución, DPI y 5 capas de seguridad
│   │   │   │   │   │   │   ├── SheetAdvancedOptionsSection.kt     # Conmutadores: Hibernación, GMS, DND, Touch, Wi-Fi, Crosshair
│   │   │   │   │   │   │   └── SheetActionButtons.kt              # Botones Guardar y Boost & Jugar
│   │   │   │   │   │   └── overlay/                # Subcomponentes modulares del HUD Flotante
│   │   │   │   │   │       ├── HudTypes.kt             # Enums de pestañas y utilidades de formato
│   │   │   │   │   │       ├── FloatingGamerBubble.kt  # Burbuja flotante minimizada con animación de pulso
│   │   │   │   │   │       ├── ExpandedGamerPanel.kt   # Panel gamer desplegable con pestañas y controles
│   │   │   │   │   │       ├── HudTelemetryTab.kt      # Pestaña de FPS, SoC Temp y uso de RAM
│   │   │   │   │   │       ├── HudResolutionTab.kt     # Pestaña de escala de resolución y DPI con test de 15s
│   │   │   │   │   │       ├── HudDriversTab.kt        # Pestaña de cambio de motor gráfico al vuelo
│   │   │   │   │   │       ├── HudDndTab.kt            # Pestaña de Modo DND Gamer, bloqueo de heads-up y excepciones
│   │   │   │   │   │       ├── HudHibernationTab.kt    # Pestaña de gestión de hibernación y lista de apps despiertas
│   │   │   │   │   │       └── HudQuickBoostTab.kt     # Pestaña de Quick Boost instantáneo en partida
│   │   │   │   │   ├── overlay/
│   │   │   │   │   │   └── CrosshairOverlayView.kt # Retícula táctica vectorial acelerada por hardware
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   └── BoosterHomeScreen.kt    # Pantalla principal limpia y orquestadora
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt             # Paleta de colores Gamer Neón
│   │   │   │   │   │   ├── Theme.kt             # Configuración de MaterialTheme M3
│   │   │   │   │   │   └── Type.kt              # Tipografía
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       ├── BoosterViewModel.kt  # Fachada ViewModel que conecta los módulos con Compose
│   │   │   │   │       └── modules/             # Módulos de lógica de negocio desacoplados
│   │   │   │   │           ├── DeviceTelemetryManager.kt # Polling de telemetría y medición de ping
│   │   │   │   │           ├── GameCatalogManager.kt     # Carga, escaneo y configuración por juego
│   │   │   │   │           └── GameBoostOrchestrator.kt  # Flujo de ejecución del Boost y reportes
│   │   │   │   └── util/                    # Utilidades y puentes de sistema
│   │   │   │       ├── NativeEngineBridge.kt    # Puente Kotlin-JNI hacia C++
│   │   │   │       ├── RustCoreBridge.kt        # Puente Kotlin-FFI hacia Rust
│   │   │   │       ├── ShizukuManager.kt        # Fachada de conexión e IPC Shizuku (ADB)
│   │   │   │       ├── SystemInfoHelper.kt      # Fachada unificada de telemetría de hardware
│   │   │   │       ├── shizuku/                 # Módulos especializados de Shizuku (ADB)
│   │   │   │       │   ├── ShizukuTypes.kt                  # Modelos de estado, reportes y resultados Shell
│   │   │   │       │   ├── AdbShellExecutor.kt              # Ejecución de comandos Shell vía IPC Shizuku
│   │   │   │       │   ├── DisplayScaleController.kt        # Controlador de resolución y DPI con failsafe de 5 capas
│   │   │   │       │   ├── GraphicsDriverController.kt      # Inyección aislada de drivers (Vulkan/ANGLE/OpenGL)
│   │   │   │       │   ├── ProcessHibernationController.kt  # Suspensión de GMS, objetivos de reposo y excepciones
│   │   │   │       │   ├── GamerDndController.kt            # Modo DND Gamer, bloqueo de heads-up y filtros
│   │   │   │       │   ├── TouchResponseController.kt       # Overclock táctil a nivel 7, 120Hz/Max y 0 animación
│   │   │   │       │   ├── NetworkOptimizerController.kt    # Desactivación de ahorro Wi-Fi y anti-jitter
│   │   │   │       │   ├── ProcessImmunityController.kt     # Blindaje OOM Score (-1000) y whitelist Doze contra el LMK
│   │   │   │       │   └── AppProcessInspector.kt           # Detección de apps en primer plano
│   │   │   │       └── system/                  # Módulos especializados de telemetría de hardware
│   │   │   │           ├── ThermalTelemetryReader.kt        # Lector de nodos térmicos `/sys/class/thermal/`
│   │   │   │           ├── NetworkPingTester.kt             # Medidor de latencia por sockets ICMP/DNS
│   │   │   │           ├── InstalledAppScanner.kt           # Escaneo y categorización de juegos instalados
│   │   │   │           └── MemoryCacheCleaner.kt            # Limpieza de caché y terminación de procesos
│   │   │   └── res/                         # Recursos gráficos, iconos y strings
│   │   └── test/                            # Tests unitarios con Robolectric y Roborazzi
│   └── build.gradle.kts                     # Dependencias de la app y configuración de compilación
├── gradle/
│   └── libs.versions.toml                   # Catálogo de versiones centralizado
├── .github/
│   └── workflows/
│       ├── sync-from-zip.yml                # Workflow de sincronización automática desde archivo comprimido en zip/
│       └── build-debug-apk.yml              # Workflow manual para compilar y firmar el APK Debug con caché
├── zip/
│   └── .gitkeep                             # Carpeta para alojar archivos .zip / .7z / .tar.gz para sincronización
├── commit_message.txt                       # Archivo opcional para personalizar el mensaje de commit al sincronizar
├── metadata.json                            # Metadatos del entorno AI Studio
├── README.md                                # Documentación general del proyecto
├── ROADMAP.md                               # Plan de desarrollo y siguientes versiones
├── STRUCTURE.md                             # Arquitectura y mapa de archivos (este archivo)
├── AI_CONTEXT.md                            # Contexto para asistentes de inteligencia artificial
└── AGENTS.md                                # Reglas e instrucciones para agentes de desarrollo
```

---

## 🔄 Flujo de Datos y Arquitectura Modular

```
[ Hardware / Sistema Android / Shizuku ADB ]
                    │
                    ▼
     [ SystemInfoHelper (Fachada) ] ◄──► [ ShizukuManager (Fachada) ]
          ├── ThermalTelemetryReader          ├── AdbShellExecutor
          ├── NetworkPingTester               ├── GraphicsDriverController
          ├── InstalledAppScanner             ├── ProcessHibernationController
          └── MemoryCacheCleaner              ├── GamerDndController
                                              ├── TouchResponseController
                                              ├── NetworkOptimizerController
                                              ├── ProcessImmunityController
                                              └── AppProcessInspector
                    │
     [ NativeEngineBridge ]
     [ RustCoreBridge ]
                    │
                    ▼
             [ BoosterPreferences (Fachada) ]
                    ├── GameConfigPreferences
                    ├── GamerDndPreferences
                    ├── HibernationPreferences
                    ├── TouchPreferences
                    ├── NetworkPreferences
                    ├── CrosshairPreferences
                    └── BoosterStatsPreferences
                    │
                    ▼
             [ BoosterViewModel ]
                    ├── DeviceTelemetryManager (Polling & Ping)
                    ├── GameCatalogManager (Juegos & Preferencias)
                    └── GameBoostOrchestrator (Boost Pipeline)
                    │
                    ▼
        [ BoosterHomeScreen (Jetpack Compose) ]
                    │
           ┌─────────┼─────────┬──────────────┐
           ▼         ▼         ▼              ▼
      [GamerHud] [Shizuku] [GameLauncher] [HeaderBar] [QuickTools]
                    │
                    ▼
          [ GameConfigSheet (Orquestador Modal) ]
                    ├── SheetHeaderSection
                    ├── SheetResolutionCountdownBanner
                    ├── SheetDriversSection
                    ├── SheetResolutionSection
                    ├── SheetAdvancedOptionsSection
                    └── SheetActionButtons
```

### 🧩 Desglose de Responsabilidades Modulares:

1. **Capa de Persistencia Modular (`data/preferences/` y `BoosterPreferences.kt`)**:
   - `BoosterPreferences`: Fachada unificada y limpia que centraliza el acceso a las preferencias del usuario.
   - `GameConfigPreferences`: Maneja la configuración por juego (drivers Vulkan/ANGLE/OpenGL, escala de resolución y banderas de servicios).
   - `GamerDndPreferences`: Gestiona el Modo No Molestar (DND), bloqueo de llamadas, bloqueo de heads-up y excepciones.
   - `HibernationPreferences`: Administra listas de congelación profunda, whitelist de apps despiertas y blacklist personalizada.
   - `TouchPreferences`: Almacena y restaura overclock táctil (`pointer_speed`), 120Hz/máxima tasa de refresco y animaciones.
   - `NetworkPreferences`: Persistencia de Wi-Fi de alta performance y anti-jitter.
   - `CrosshairPreferences`: Almacenamiento de estilo, tamaño, color y visibilidad de la retícula táctica.
   - `BoosterStatsPreferences`: Registro acumulativo de sesiones optimizadas y megabytes de RAM liberados.

2. **Capa de HUD Flotante y Servicios en Segundo Plano (`service/overlay/` y `GameOverlayService.kt`)**:
   - `GameOverlayService`: Servicio en primer plano que administra el ciclo de vida del HUD flotante y las notificaciones persistentes.
   - `FpsTracker`: Conteo no intrusivo de fotogramas por segundo en tiempo real mediante `Choreographer.FrameCallback`.
   - `OverlayResolutionTester`: Administrador de pruebas de resolución de 15 segundos con cuenta regresiva y auto-revert failsafe.
   - `OverlayGamerActions`: Ejecutor de acciones en partida (DND, hibernación selectiva, cambio de drivers y Quick Boost).
   - `DraggableOverlayWindowManager`: Controla la ventana flotante en el `WindowManager` y los gestos táctiles de arrastre.
   - `OverlayLifecycleOwner`: Ciclo de vida y SavedStateRegistry para renderizado Compose en ventana de sistema.

3. **Capa de Componentes de Configuración (`ui/components/sheet/` y `GameConfigSheet.kt`)**:
   - `GameConfigSheet`: Modal orquestador ultra limpio (<120 líneas) que ensambla las secciones de configuración.
   - `SheetHeaderSection`: Encabezado con icono del juego, título y botón de cierre.
   - `SheetResolutionCountdownBanner`: Banner dinámico de prueba de resolución con contador regresivo y auto-revert.
   - `SheetDriversSection`: Selector visual interactivo para inyección de drivers gráficos (Vulkan, ANGLE, OpenGL).
   - `SheetResolutionSection`: Selector de perfiles de escala de resolución y DPI con tarjeta explicativa del failsafe de 5 capas.
   - `SheetAdvancedOptionsSection`: Conmutadores individuales de hibernación de procesos, suspensión de GMS, DND gamer, overclock táctil, Wi-Fi anti-jitter y crosshair táctico.
   - `SheetActionButtons`: Botones ergonómicos de Guardar y Boost & Jugar.

4. **Capa de Telemetría del Sistema (`util/system/`, `SystemInfoHelper.kt`, `native-lib.cpp`)**:
   - `native-lib.cpp`: Telemetría nativa en C++ a cero asignaciones (*Zero-Alloc / Zero-GC Jank*) leyendo `/proc/stat` y `/sys/class/thermal/`.
   - `ThermalTelemetryReader`: Lectura a bajo nivel de la temperatura del procesador/SoC en nodos térmicos reales del kernel Linux.
   - `NetworkPingTester`: Medición de latencia real por sockets ICMP/DNS contra servidores globales.
   - `InstalledAppScanner`: Escaneo eficiente del paquete de aplicaciones instaladas con categorización automática.
   - `MemoryCacheCleaner`: Vaciado de cachés temporales, invocación de GC y liberación segura de memoria RAM.

5. **Capa de Control Shizuku (`util/shizuku/`)**:
   - `AdbShellExecutor`: Ejecución asíncrona segura de comandos ADB por Binder (`Shizuku.newProcess`).
   - `GraphicsDriverController`: Inyección por paquete de controladores gráficos y reversión automática.
   - `ProcessHibernationController`: Suspensión y deshibernación de Google Play Services y apps de fondo.
   - `GamerDndController`: Modo No Molestar automatizado, bloqueo de banners heads-up y filtros.
   - `TouchResponseController`: Overclock de sensibilidad táctil a nivel 7, 120Hz/máxima tasa de refresco y 0 animaciones.
   - `NetworkOptimizerController`: Desactivación de ahorro de energía Wi-Fi para estabilidad anti-jitter.
   - `ProcessImmunityController`: Inmunidad de proceso y protección contra el LMK (`oom_score_adj -1000` y whitelist Doze).
   - `AppProcessInspector`: Detección en tiempo real del juego en primer plano.
   - `ShizukuManager`: Fachada unificada que administra el ciclo de vida del Binder y expone una API limpia.

6. **Capa de Lógica del ViewModel (`ui/viewmodel/modules/`)**:
   - `DeviceTelemetryManager`: Gestiona el hilo de telemetría de hardware y el test de ping en vivo.
   - `GameCatalogManager`: Administra la consulta de aplicaciones instaladas y preferencias por juego.
   - `GameBoostOrchestrator`: Coordina la secuencia y ejecución técnica del Boost.
   - `BoosterViewModel`: Punto único de enlace para la UI en Jetpack Compose.
