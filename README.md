# ⚡ Game Booster Turbo (con Integración Shizuku)

[![Android](https://img.shields.io/badge/Android-11%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Shizuku](https://img.shields.io/badge/Shizuku-API%2013-red.svg)](https://shizuku.rikka.app)
[![Distribution](https://img.shields.io/badge/Release-Uptodown%20%2F%20Direct%20APK-orange.svg)](https://uptodown.com)

**Game Booster Turbo** es una aplicación moderna y de alto rendimiento diseñada para optimizar dispositivos Android al momento de jugar. A través de la API oficial de **Shizuku**, la aplicación puede ejecutar optimizaciones profundas de nivel ADB y modo de juego (Game Mode) directamente desde el teléfono sin necesidad de conectarlo a una PC ni requerir root obligatorio.

---

## 🎯 Características Principales

- **⚡ Optimización con Shizuku (ADB / Root)**:
  - Cierre forzado real de procesos consumidores en segundo plano (`am force-stop`).
  - Purga global de la caché del sistema operativo (`pm trim-caches`).
  - Activación forzada del Modo Rendimiento de Android 12+ (`cmd game mode 2 <package>`).
- **🎮 Selector y Forzado de Motor Gráfico (Por Juego)**:
  - Configuración individual del controlador GPU: **Vulkan Game Driver**, **ANGLE (OpenGL ES sobre Vulkan)**, **OpenGL ES Nativo** o **Automático del Sistema**.
  - Inyección segura de driver por paquete mediante Shizuku (`settings put global updatable_driver_production_opt_in_apps` y `angle_gl_driver_selection_pkgs`).
  - Cero uso de propiedades `persist.sys.*` para máxima seguridad del firmware.
- **🖥️ Selector de Resolución y Escala DPI (Failsafe 5 Capas)**:
  - Reducción de la carga de renderizado del GPU hasta un **75%** (`wm size` y `wm density`) con perfiles de escala: **100% Nativo**, **85% Balanceado**, **75% Rendimiento HD+** y **50% Ultra Fluidez**.
  - **Arquitectura de Seguridad en 5 Capas contra Cierres Inesperados**:
    1. **Watchdog Daemon (Dead-Man's Switch)**: Proceso script en shell independiente con timeout de 35s que monitorea un archivo heartbeat y restaura la pantalla a resolución de fábrica si Android o el LMK cierran la aplicación.
    2. **Botón de Pánico Permanente**: Notificación persistente con receptor directo `EmergencyResetReceiver` para volver a la pantalla nativa con un solo toque desde cualquier lugar.
    3. **Boot Recovery Automático**: Restablecimiento garantizado si el dispositivo se apaga o reinicia (`BootRecoveryReceiver`).
    4. **Clamping Proporcional Simétrico**: Ancho y alto calculados siempre en números pares y densidad DPI recalculada de forma milimétrica para evitar descalibración táctil o que los botones se vuelvan diminutos/gigantes.
    5. **Modo Prueba de 15 Segundos**: Prueba en vivo en la interfaz de configuración con cuenta regresiva interactiva y reversión automática si el usuario no confirma.
- **🛡️ Blindaje Anti-Cierre y Prioridad Máxima (`ProcessImmunityController`)**:
  - **Inmunidad OOM Score (-1000)**: Asigna la prioridad más alta de proceso a nivel de kernel mediante Shizuku para evitar que el *Low Memory Killer* (LMK) de Android cierre el centinela en juegos exigentes (Genshin Impact, Warzone, Free Fire).
  - **Lista Blanca de Doze y AppOps**: Exime la app de optimizaciones agresivas de batería del fabricante (`dumpsys deviceidle whitelist +pkg` y `cmd appops set RUN_IN_BACKGROUND allow`).
  - **Servicios de Primer Plano `specialUse`**: Cumplimiento estricto de Android 14/15 con `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` en `GameWatcherService` y `GameOverlayService`.
- **❄️ Centinela de Hibernación en Juego (RAM Boost Extremo)**:
  - **Monitoreo en Vivo (`GameWatcherService`)**: Servicio centinela en primer plano que detecta en tiempo real cuándo estás dentro del juego y cuándo sales o minimizas.
  - **Hibernación de Procesos con Lista de Excepciones**: Congela apps secundarias y redes sociales (`am set-inactive` y `pm suspend`) mientras juegas, con soporte completo para listas de excepciones (mantener apps despiertas) y objetivos específicos configurables.
  - **Suspensión de Google Play Services (Opcional)**: Deshabilita temporalmente los servicios de Google Play mientras estás en la partida, liberando entre **+350MB y +600MB de RAM**.
  - **Reversión Automática de Fábrica**: En cuanto sales del juego o se cierra el proceso, el centinela reactiva inmediatamente los servicios de Google, los procesos congelados y los controladores originales de Android.
  - **Protección Fail-Safe (`BootRecoveryReceiver`)**: Restaura el sistema automáticamente tras cualquier reinicio del dispositivo.
- **🔕 Modo No Molestar Gamer Automatizado (DND)**:
  - **Bloqueo Inteligente de Notificaciones y Heads-Up**: Silencia alertas intrusivas y bloquea banners emergentes durante la partida mediante Shizuku (`zen_mode` y `heads_up_notifications_enabled`).
  - **Lista Blanca de Excepciones**: Recibe notificaciones importantes de apps críticas que elijas (WhatsApp, Discord, etc.) sin interrumpir el juego.
  - **Pase de Llamadas Prioritarias**: Filtra o permite llamadas telefónicas entrantes según tu preferencia.
  - **Restauración Automática**: Devuelve el modo DND y los banners emergentes a sus valores originales al terminar la partida.
- **⚡ Touch Boost & Latencia Táctil Ultrabaja (`TouchResponseController`)**:
  - **Sensibilidad y Muestreo al Máximo**: Ajusta la velocidad del puntero del sistema (`pointer_speed 7`) y fija la tasa de refresco a 120Hz/máxima (`min_refresh_rate`, `peak_refresh_rate`, `user_refresh_rate`) para reducir el tiempo de respuesta del panel táctil.
  - **Zero-Latency de Animaciones**: Desactiva las escalas de animación del sistema operativo (`window_animation_scale`, `transition_animation_scale`, `animator_duration_scale 0`) durante la sesión de juego.
  - **Restauración Automática de Fábrica**: Los valores originales se guardan y se restauran íntegramente al salir del juego o reiniciar.
- **📶 Optimizador de Red Wi-Fi Anti-Jitter (`NetworkOptimizerController`)**:
  - **Eliminación de Picos de Lag**: Desactiva el modo de ahorro y suspensión de energía del chip Wi-Fi (`wifi_suspend_optimizations_enabled 0`, `cmd wifi set-power-save-mode 0`, `cmd wifi set-low-latency-mode enabled`) mediante Shizuku.
  - **Estabilización de Ping UDP/TCP**: Mantiene los sockets de paquetes de red abiertos y priorizados, ideal para shooters (Free Fire, COD Mobile, PUBG) y MOBAs (Wild Rift).
  - **Restauración al Salir**: Reactiva las políticas estándar de ahorro de batería al cerrar el juego.
- **🎯 Mira Gamer Táctica Flotante (Crosshair HUD)**:
  - **Retícula Vectorial Acelerada por Hardware (`CrosshairOverlayView`)**: Superpone una mira personalizable en el centro exacto de la pantalla para mejorar la precisión de disparo de cadera.
  - **Estilos y Colores Neón**: Diseños en Cruz Táctica (`CROSS`), Punto Central (`DOT`), Círculo con Punto (`CIRCLE_DOT`) y Diamante (`DIAMOND`) con colores Neón Cyan, Verde, Rojo o Púrpura.
- **🎛️ HUD Flotante Gamer In-Game (`GameOverlayService`)**:
  - **Burbuja Flotante Arrastrable**: Monitor de FPS en vivo, temperatura de SoC y uso de RAM sobre cualquier juego.
  - **Panel Desplegable con Pestañas**: Cambia controladores GPU (Vulkan, ANGLE, OpenGL), prueba o ajusta resoluciones/DPI, conmuta el Modo DND y gestiona la hibernación de aplicaciones al vuelo sin pausar la partida.
- **📊 Telemetría y Monitoreo en Tiempo Real**:
  - Indicador HUD circular estilo Gamer con estado de optimización.
  - **Cálculo Exacto y Real de CPU**: Monitor nativo en C++ leyendo deltas de `/proc/stat` y temperatura del SoC en tiempo real (`/sys/class/thermal/`).
  - Medición de latencia (Ping real mediante sockets ICMP/HTTP con servidores DNS globales).
  - Monitoreo de memoria RAM libre/ocupada, batería, almacenamiento y tasa de refresco (Hz).
- **🕹️ Lanzador de Juegos Personalizado**:
  - Menú de ajustes individuales para cada juego con badges de renderizado.
  - Perfiles de optimización: *Ultra Rendimiento*, *Modo Batería Ahorro* y *Modo Red Baja Latencia*.
  - Botón "BOOST & JUGAR" con aceleración y lanzamiento directo.
- **🛡️ 100% Funcional desde el Teléfono**:
  - No requiere PC ni cables para activar Shizuku (compatible con Depuración Inalámbrica de Android 11+).
  - Diseñado para distribución directa en **Uptodown** y tiendas APK de terceros.
- **⚙️ Puentes Nativos Listos (C++ y Rust)**:
  - Estructura `CMake` y puente JNI para extensiones en C++.
  - Estructura `Cargo` y puente FFI para extensiones en Rust.

---

## 📱 Guía Rápida: Activar Shizuku en tu Teléfono (Sin PC)

Si tienes **Android 11 o superior**, puedes activar todas las funciones avanzadas en 2 minutos desde el propio móvil:

1. **Instala Shizuku** (desde Uptodown o GitHub oficial).
2. Ve a los **Ajustes del teléfono > Opciones de desarrollador**.
3. Activa la casilla **Depuración inalámbrica** (Wireless Debugging).
4. Abre **Shizuku**, toca en *Emparejamiento* (Pairing) y selecciona la opción de pantalla dividida o notificación emergente para introducir el código de 6 dígitos que te da Android.
5. Inicia el servicio en Shizuku.
6. Abre **Game Booster Turbo**, toca el botón **"AUTORIZAR PERMISO SHIZUKU"** ¡y listo!

> *Nota: Si no usas Shizuku, Game Booster seguirá funcionando con el motor de optimización estándar de RAM y búfer.*

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
| :--- | :--- |
| **Lenguaje Principal** | Kotlin 2.0+ con Coroutines y StateFlow |
| **Interfaz de Usuario** | Jetpack Compose + Material Design 3 (Gamer Neon Theme) |
| **Elevación de Sistema** | Shizuku API v13.1.5 + Shizuku Provider |
| **Almacenamiento Local**| Room Database / SharedPreferences |
| **Motor Nativo (C++)**  | Android NDK (CMakeLists.txt + JNI) |
| **Motor Nativo (Rust)** | Rust 2021 Edition (Cargo + FFI) |
| **CI/CD & Automatización**| GitHub Actions (`sync-from-zip.yml` & `build-debug-apk.yml`) |
| **Distribución**        | APK directo sin dependencias cerradas de Google Play Services |

---

## 📦 Flujos de Trabajo de GitHub Actions

### 1. 🔄 Sincronización Automática vía Archivo ZIP (`sync-from-zip.yml`)
- Sube un archivo comprimido (`.zip`, `.7z`, `.tar.gz`) a la carpeta `zip/`.
- Opcionalmente, define el mensaje en `commit_message.txt`.
- Extrae y sincroniza automáticamente todo el código con un commit limpio `--amend`.

### 2. 🔨 Compilación Manual de APK Debug (`build-debug-apk.yml`)
- **Activación 100% Manual**: Ve a la pestaña **Actions** en tu repositorio de GitHub > selecciona **Build Debug APK (Manual Trigger)** > pulsa **Run workflow**.
- **Firma Automática en el Runner**: Genera o decodifica la llave `debug.keystore` al vuelo y firma el APK.
- **Caché Inteligente de Gradle**: Utiliza caché de dependencias y wrappers para compilaciones ultrarrápidas.
- **Descarga Directa en el Móvil**: Al finalizar, el archivo `GameBooster-Turbo-Debug-APK` queda disponible en la sección de **Artifacts** para descargarlo e instalarlo directamente en tu teléfono.

---

## 🚀 Compilación Local y Generación de APK

Para compilar el proyecto en modo release para subirlo a Uptodown:

```bash
# Compilar APK de Release
gradle :app:assembleRelease

# Compilar APK de Pruebas (Debug)
gradle :app:assembleDebug
```

El archivo APK resultante se generará en:
`app/build/outputs/apk/release/app-release.apk`
