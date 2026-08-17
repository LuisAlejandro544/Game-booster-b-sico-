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
  - Hibernación en Vivo: `am set-inactive <pkg> true` y `pm suspend <pkg>` para dormir apps secundarias mientras se juega.
  - Suspensión temporal de Google Play Services: `pm suspend com.google.android.gms` (+350MB-600MB de RAM libre durante la partida).
- **Centinela en Primer Plano (`GameWatcherService`)**: Monitorea con `dumpsys activity` / `cmd activity` si el juego sigue en pantalla. Cuando el usuario sale del juego, el servicio restaura instantáneamente la configuración normal de Android (`pm unsuspend`, `settings delete/put`, `am set-inactive false`).
- **HUD Flotante Gamer (`GameOverlayService` & `GameOverlayHudView`)**: Servicio `SYSTEM_ALERT_WINDOW` con un ciclo de vida `ComposeView` sobre `WindowManager`. Muestra una burbuja flotante arrastrable con contador de FPS dinámico vía `Choreographer.FrameCallback`, temperatura de SoC y RAM en tiempo real, así como un panel desplegable con selector al vuelo de motores gráficos (Vulkan, ANGLE, OpenGL) y botón de Quick Boost.
- **Fail-Safe de Arranque (`BootRecoveryReceiver`)**: Restaura drivers y servicios automáticamente si el teléfono se reinicia inesperadamente.
- **Regla Estricta de Seguridad**: NUNCA usar propiedades `persist.sys.*` para evitar modificar valores persistentes del firmware del usuario.
- **Aislamiento y Reversión**: Los cambios gráficos son por paquete y se revierten automáticamente al salir del juego o regresar a la aplicación.
- **Nunca asumir que Shizuku siempre está disponible**: Siempre verificar `ShizukuState.AUTHORIZED` antes de ejecutar comandos Shell y proveer caminos de fallback seguros en Kotlin.

### 2. Telemetría Real y Capas Nativas (C++ y Rust)
- **Cálculo Exacto de CPU**: Se calcula en tiempo real a nivel de kernel mediante delta de `/proc/stat` implementado en `native-lib.cpp` con fallback en Kotlin puro.
- **Temperatura del SoC**: Se lee de los nodos `/sys/class/thermal/thermal_zone*` para reportar la temperatura real del procesador.
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

### 5. Flujo de CI/CD y Sincronización ZIP
- `.github/workflows/sync-from-zip.yml`: Automatización que procesa archivos comprimidos (`.zip`, `.7z`, `.tar.gz`) colocados en la carpeta `zip/`, los descomprime, sincroniza el código en la raíz del repositorio y actualiza el commit usando el contenido de `commit_message.txt` si está presente.
