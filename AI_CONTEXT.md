# 🧠 AI Context & Domain Knowledge - Game Booster Turbo

Este archivo proporciona contexto técnico para modelos de lenguaje (LLMs) y asistentes de código que colaboren en el desarrollo y mantenimiento de **Game Booster Turbo**.

---

## 📌 Resumen del Proyecto y Filosofía

- **Propósito**: Aplicación Android para gamers que optimiza el rendimiento del dispositivo, limpia memoria RAM residual, estabiliza la latencia de red (Ping) y permite lanzar juegos con perfiles de aceleración.
- **Diferenciador Clave**: Integración profunda con **Shizuku** para ejecutar comandos con privilegios de **ADB Shell (UID 2000)** o **Root (UID 0)** sin necesidad de cables ni PC (usando Depuración Inalámbrica en el propio dispositivo móvil).
- **Canal de Distribución**: APK independiente publicado en **Uptodown** u otras tiendas de terceros (sin depender de librerías cerradas de Google Play Billing o Firebase mandatory cloud logins).
- **Entorno del Desarrollador**: El usuario gestiona el desarrollo directamente desde su dispositivo móvil sin ordenador de sobremesa.

---

## ⚙️ Directrices Técnicas para la IA

### 1. Manejo de Shizuku, Procesos del Sistema y Centinela en Juego
- La comunicación con Shizuku se realiza mediante `ShizukuManager.kt`.
- Comandos ADB utilizados:
  - `am force-stop <package>`: Cierra apps pesadas en segundo plano antes de iniciar.
  - `pm trim-caches <size>`: Purga cachés del sistema para liberar almacenamiento y memoria.
  - `cmd game mode 2 <package>`: Activa el Modo Rendimiento del sistema en Android 12+ (API 31+).
  - Inyección de renderizado GPU: `settings put global updatable_driver_production_opt_in_apps <pkg>` (Vulkan Game Driver) y `settings put global angle_gl_driver_selection_pkgs <pkg>` (ANGLE OpenGL sobre Vulkan).
  - Escalado de Resolución y DPI (`DisplayScaleController`): Modificación dinámica con `wm size` y `wm density` protegida con failsafe de 5 capas:
    1. *Watchdog Daemon (Dead-Man's switch 35s)*: Script desvinculado en `/data/local/tmp` que restaura resolución física si el proceso muere.
    2. *Botón de Pánico permanente*: Notificación persistente con acción directa `EmergencyResetReceiver`.
    3. *Boot Recovery*: Rollback automático en `BootRecoveryReceiver` ante reinicios.
    4. *Cálculo Par y Proporcional*: Píxeles pares y DPI ajustados para evitar desincronización táctil.
    5. *Prueba de 15 Segundos*: Cuenta atrás interactiva con rollback automático si no se valida.
  - Hibernación en Vivo: `am set-inactive <pkg> true` y `pm suspend <pkg>` para dormir apps secundarias mientras se juega, con soporte para lista de exclusiones (whitelist) y objetivos personalizados configurables desde el HUD o el dashboard.
  - Suspensión temporal de Google Play Services: `pm suspend com.google.android.gms` (+350MB-600MB de RAM libre durante la partida).
  - Modo No Molestar Gamer Automatizado (DND): Control por ADB/Shizuku de `zen_mode` y `heads_up_notifications_enabled` con preservación del estado original del usuario, filtro de llamadas prioritarias y excepciones personalizadas de apps.
  - Touch Boost & Latencia Táctil (`TouchResponseController`): Overclock de velocidad de puntero (`pointer_speed 7`), fijación de frecuencia de refresco máxima/120Hz (`min_refresh_rate`, `peak_refresh_rate`, `user_refresh_rate`) y desactivación de animación (`animator_duration_scale 0`) para respuesta táctil instantánea con respaldo y reversión automática.
  - Wi-Fi Anti-Jitter Optimizer (`NetworkOptimizerController`): Desactivación del modo de suspensión y ahorro energético del chip Wi-Fi (`wifi_suspend_optimizations_enabled 0`, `cmd wifi set-power-save-mode 0`, `cmd wifi set-low-latency-mode enabled`) para eliminar fluctuaciones de ping y caídas de paquetes en shooters y MOBAs.
  - Inmunidad contra el LMK y Blindaje de Proceso (`ProcessImmunityController`): Ajuste de `oom_score_adj -1000`, inclusión en whitelist de Doze (`dumpsys deviceidle whitelist +pkg`) y concesión de AppOps de ejecución en segundo plano para evitar que Android o capas de fabricantes cierren la app en juegos de alta exigencia de RAM.
  - Mira Gamer Táctica Flotante (`CrosshairOverlayView`): Superposición de retícula vectorial en el centro de pantalla con estilos tácticos (Cruz, Punto, Círculo, Diamante) acelerada por hardware en Compose sin bloquear toques.
- **Centinela en Primer Plano (`GameWatcherService`)**: Monitorea con `dumpsys activity` / `cmd activity` si el juego sigue en pantalla con tipo de servicio `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`. Cuando el usuario sale del juego, el servicio restaura instantáneamente la configuración normal de Android (resolución, densidad, `pm unsuspend`, `settings delete/put`, `am set-inactive false`, modo DND, banners heads-up, velocidad táctil, tasa de refresco, optimización Wi-Fi e inmunidad de proceso).
- **HUD Flotante Gamer (`GameOverlayService` & `GameOverlayHudView`)**: Servicio `SYSTEM_ALERT_WINDOW` con un ciclo de vida `ComposeView` sobre `WindowManager`. Muestra una burbuja flotante arrastrable con contador de FPS dinámico vía `Choreographer.FrameCallback`, temperatura de SoC y RAM en tiempo real, así como un panel desplegable con pestañas de telemetría, selector de motores gráficos (Vulkan, ANGLE, OpenGL), escalado de resolución, control de DND y gestión de hibernación/excepciones de apps al vuelo.
- **Fail-Safe de Arranque (`BootRecoveryReceiver`)**: Restaura drivers, resolución/DPI de pantalla, estado de notificaciones DND/Heads-up, velocidad táctil, optimizaciones de red Wi-Fi y servicios de Google automáticamente si el teléfono se reinicia inesperadamente.
- **Regla Estricta de Seguridad**: NUNCA usar propiedades `persist.sys.*` para evitar modificar valores persistentes del firmware del usuario.
- **Aislamiento y Reversión**: Los cambios gráficos son por paquete y se revierten automáticamente al salir del juego o regresar a la aplicación.
- **Nunca asumir que Shizuku siempre está disponible**: Siempre verificar `ShizukuState.AUTHORIZED` antes de ejecutar comandos Shell y proveer caminos de fallback seguros en Kotlin.

### 2. Telemetría Real y Capas Nativas (C++ y Rust)
- **Telemetría Zero-Alloc (Zero Garbage Collection Jank)**: Implementada en `native-lib.cpp` mediante llamadas POSIX `open()` y `read()` directamente sobre buffers en pila (stack buffers) para `/proc/stat` y `/sys/class/thermal/thermal_zone*`. Esto evita crear objetos `String` temporales que activen el Garbage Collector de Android en segundo plano durante partidas.
- **Cálculo de Frametimes de Alta Precisión y Percentiles 1% Low**: Registro en ring-buffer nativo de 120 fotogramas con marcas de tiempo en nanosegundos (`CLOCK_MONOTONIC_RAW`) calculando latencia media por fotograma, percentil 99th (1% Low FPS) y 99.9th (0.1% Low FPS).
- Las capas nativas en `app/src/main/cpp` y `app/src/main/rust` están diseñadas con wrappers Kotlin (`NativeEngineBridge.kt` y `RustCoreBridge.kt`) que incluyen manejo de excepciones `UnsatisfiedLinkError` y lógica de respaldo en Kotlin puro para no romper la ejecución de la app si las librerías `.so` no están compiladas.

### 3. UI y Estilo (Jetpack Compose & M3)
- Utilizar exclusivamente **Jetpack Compose** y la paleta de colores de `com.example.ui.theme`:
  - `NeonCyan` (`#00F0FF`): Acentos principales y telemetría de red.
  - `NeonGreen` (`#00E676`): Estados óptimos, RAM libre y confirmación Shizuku.
  - `NeonPurple` (`#A855F7`): Gradientes, detalles gamer y perfiles.
  - `NeonRed` (`#FF1744`): Advertencias y memoria crítica.
  - `GamerDarkBackground` (`#0A0E17`) y `GamerCardBackground` (`#111827`).
- Todos los elementos interactivos deben contar con `Modifier.testTag("...")` para permitir pruebas automatizadas.

### 4. Directrices de Rendimiento
- Evitar operaciones de red o E/S en el hilo principal (`Dispatchers.Main`); usar siempre corrutinas con `Dispatchers.IO`.
- No añadir librerías pesadas innecesarias que aumenten el peso del APK o ralenticen el dispositivo del usuario.

### 5. Flujos de CI/CD y Compilación en la Nube
- `.github/workflows/sync-from-zip.yml`: Automatización que procesa archivos comprimidos (`.zip`, `.7z`, `.tar.gz`) colocados en la carpeta `zip/`, los descomprime, sincroniza el código en la raíz del repositorio y actualiza el commit usando el contenido de `commit_message.txt` si está presente.
- `.github/workflows/build-debug-apk.yml`: Compilación bajo demanda (`workflow_dispatch` 100% manual) en GitHub Actions para generar y firmar el APK de depuración con caché de Gradle, subiendo el APK como artefacto descargable.
