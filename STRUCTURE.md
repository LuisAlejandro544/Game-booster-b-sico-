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
│   │   │   │   │   └── BoosterPreferences.kt# SharedPreferences / ajustes del usuario
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
│   │   │   │   │   └── overlay/             # Módulos del HUD Flotante
│   │   │   │   │       ├── DraggableOverlayWindowManager.kt # Ventana flotante, layout y gestos táctiles de arrastre
│   │   │   │   │       └── OverlayLifecycleOwner.kt         # Ciclo de vida y SavedStateRegistry para WindowManager
│   │   │   │   ├── ui/                      # Capa de Presentación (Jetpack Compose)
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── AppPickerSheet.kt       # Selector de aplicaciones instaladas
│   │   │   │   │   │   ├── BoostDialog.kt          # Diálogo con barra de progreso y reporte
│   │   │   │   │   │   ├── BoostProfileSelector.kt # Selector de perfiles (Ultra/Batería/Red)
│   │   │   │   │   │   ├── BoosterHeaderBar.kt     # Barra de encabezado, marca, estados y accesos directos
│   │   │   │   │   │   ├── GameConfigSheet.kt      # Menú de ajuste: GPU Drivers y Escala de Resolución/DPI (5 capas)
│   │   │   │   │   │   ├── GameLauncherSection.kt  # Cuadrícula y lista de juegos con badges de driver
│   │   │   │   │   │   ├── GameOverlayHudView.kt   # Fachada coordinadora de la vista flotante in-game
│   │   │   │   │   │   ├── GamerHudGauge.kt        # Tacómetro/indicador circular interactivo
│   │   │   │   │   │   ├── MetricCards.kt          # Tarjetas de CPU real, RAM, Ping, Batería, Almacenamiento
│   │   │   │   │   │   ├── QuickToolsBanner.kt     # Banner de limpieza rápida de memoria y optimización
│   │   │   │   │   │   ├── ShizukuControlCard.kt   # Tarjeta de estado y permisos de Shizuku
│   │   │   │   │   │   ├── SpeedTestDialog.kt      # Diagnóstico de latencia en vivo
│   │   │   │   │   │   └── overlay/                # Subcomponentes modulares del HUD Flotante
│   │   │   │   │   │       ├── HudTypes.kt             # Enums de pestañas y utilidades de formato
│   │   │   │   │   │       ├── FloatingGamerBubble.kt  # Burbuja flotante minimizada con animación de pulso
│   │   │   │   │   │       ├── ExpandedGamerPanel.kt   # Panel gamer desplegable con pestañas y controles
│   │   │   │   │   │       ├── HudTelemetryTab.kt      # Pestaña de FPS, SoC Temp y uso de RAM
│   │   │   │   │   │       ├── HudResolutionTab.kt     # Pestaña de escala de resolución y DPI con test de 15s
│   │   │   │   │   │       ├── HudDriversTab.kt        # Pestaña de cambio de motor gráfico al vuelo
│   │   │   │   │   │       └── HudQuickBoostTab.kt     # Pestaña de Quick Boost instantáneo en partida
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
│   │   │   │       │   ├── ProcessHibernationController.kt  # Suspensión de GMS y congelamiento de procesos
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
          └── MemoryCacheCleaner              └── AppProcessInspector
                    │
     [ NativeEngineBridge ]
     [ RustCoreBridge ]
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
```

### 🧩 Desglose de Responsabilidades Modulares:

1. **Capa de Telemetría del Sistema (`util/system/`, `SystemInfoHelper.kt`, `native-lib.cpp`)**:
   - `native-lib.cpp`: Telemetría nativa en C++ a cero asignaciones (*Zero-Alloc / Zero-GC Jank*) leyendo `/proc/stat` y `/sys/class/thermal/` con buffers en pila, además de calcular frametimes en nanosegundos (`CLOCK_MONOTONIC_RAW`), latencia media por fotograma y percentiles 1% Low FPS.
   - `ThermalTelemetryReader`: Lectura a bajo nivel de la temperatura del procesador/SoC leyendo nodos térmicos reales del kernel Linux (`/sys/class/thermal/`).
   - `NetworkPingTester`: Medición de latencia real contra servidores DNS globales de alta disponibilidad mediante sockets TCP/UDP.
   - `InstalledAppScanner`: Escaneo eficiente del paquete de aplicaciones instaladas con detección heurística y por flags de juegos.
   - `MemoryCacheCleaner`: Vaciado de cachés temporales, invocación de GC y liberación segura de memoria RAM.
   - `SystemInfoHelper`: Fachada unificada que ensambla todas las métricas en un objeto inmutable `DeviceMetrics`.

2. **Capa de Control Shizuku (`util/shizuku/`)**:
   - `AdbShellExecutor`: Ejecución asíncrona segura de comandos ADB por Binder (`Shizuku.newProcess`) con captura de stdout/stderr y exit codes.
   - `GraphicsDriverController`: Inyección por paquete de controladores gráficos (`updatable_driver_production_opt_in_apps`, `angle_gl_driver_selection_pkgs`) y reversión garantizada sin tocar `persist.sys.*`.
   - `ProcessHibernationController`: Suspensión y deshibernación de Google Play Services (`pm suspend` / `am set-inactive`) y congelamiento de aplicaciones secundarias.
   - `AppProcessInspector`: Detección en tiempo real de la app en primer plano mediante `dumpsys activity` / `cmd activity`.
   - `ShizukuManager`: Fachada unificada que administra el ciclo de vida del Binder y expone una API limpia hacia el resto de la aplicación.

3. **Capa de Lógica del ViewModel (`ui/viewmodel/modules/`)**:
   - `DeviceTelemetryManager`: Gestiona el hilo periódico de refresco de telemetría de hardware y el test de ping en vivo.
   - `GameCatalogManager`: Administra la consulta de aplicaciones instaladas, detección automática de juegos y almacenamiento de configuraciones por juego.
   - `GameBoostOrchestrator`: Coordina la secuencia de animación y ejecución técnica de la optimización (limpieza de RAM/caché, ejecución ADB, cálculo de deltas y arranque del centinela).
   - `BoosterViewModel`: Punto único de enlace para la UI en Jetpack Compose, manteniendo los `StateFlow` reactivos.

4. **Capa de Presentación y HUD Flotante (`ui/components/overlay/` y `service/overlay/`)**:
   - `FloatingGamerBubble`: Burbuja flotante animada con contador de FPS, temperatura de SoC y acceso rápido al panel.
   - `ExpandedGamerPanel`: Panel expandible in-game con pestañas de telemetría, selector de drivers gráficos y botón Quick Boost.
   - `HudTelemetryTab`, `HudResolutionTab`, `HudDriversTab`, `HudQuickBoostTab`: Vistas modulares por pestaña con responsabilidad única.
   - `GameOverlayHudView`: Punto de entrada que coordina el estado expandido/minimizado.
   - `DraggableOverlayWindowManager`: Controla la adición/remoción de la vista flotante en el `WindowManager`, `LayoutParams` (`TYPE_APPLICATION_OVERLAY`) y el cálculo de gestos táctiles de arrastre en pantalla.
   - `OverlayLifecycleOwner`: Suministra el ciclo de vida de Android (`LifecycleOwner`, `SavedStateRegistryOwner`, `ViewModelStoreOwner`) para renderizar componentes Jetpack Compose sobre el sistema.
   - `BoosterHeaderBar` y `QuickToolsBanner`: Componentes visuales independientes de la pantalla principal.
