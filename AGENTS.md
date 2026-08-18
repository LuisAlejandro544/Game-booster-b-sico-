# 🤖 AGENTS.md - Reglas e Instrucciones para Agentes de IA

Este archivo contiene las directivas obligatorias para cualquier agente de inteligencia artificial que opere sobre esta base de código.

---

## 🎯 Principios Fundamentales del Proyecto

1. **Razonamiento Previo Obligatorio**:
   - Antes de ejecutar cualquier comando o modificación en los archivos, el agente debe analizar metódicamente el alcance, las herramientas a utilizar y el impacto en la estabilidad de la compilación.
2. **Entorno del Usuario**:
   - El usuario programa y opera directamente desde su **teléfono móvil (sin PC)**. Las soluciones deben ser claras, accionables y no requerir comandos complejos de escritorio o configuraciones externas engorrosas.
3. **Canal de Distribución**:
   - La aplicación está orientada a tiendas como **Uptodown** o distribución directa en formato **APK**.
   - No forzar dependencias exclusivas de Google Play Store (como Play Core In-App Review o Google Play Billing).
4. **Respeto Estricto al Alcance (Scope Discipline)**:
   - Construir exactamente lo que el usuario solicita con máxima artesanía visual y técnica. Evitar agregar módulos no solicitados.

---

## 🔄 Flujo de Desarrollo (Las 7 Fases del Desarrollador)

Cualquier intervención debe alinearse con una o varias de las fases del ciclo de software:

### 01. El Arquitecto (Planificación y Diseño)
- Diseñar la arquitectura técnica respetando Clean Architecture (MVVM, StateFlow, Room/Preferences).
- Mantener desacopladas la capa de presentación (Compose), la capa de negocio (ViewModels) y la capa de sistema/nativa (Shizuku, JNI, FFI).

### 02. El Constructor (Generación de Código Funcional)
- Escribir código Kotlin modular, listo para producción y con manejo exhaustivo de excepciones.
- Respetar la nomenclatura estándar y declarar siempre `Modifier.testTag` en componentes interactivos de Compose.

### 03. El Detective (Debugging y Resolución Metódica)
- Al encontrar fallos o errores de compilación, formular hipótesis, analizar la causa raíz y aplicar correcciones quirúrgicas sin alterar código no relacionado.

### 04. El Crítico (Revisión de Código)
- Evaluar seguridad (evitar inyecciones en comandos Shell), rendimiento en Compose (`derivedStateOf`, `remember`) y consumo de memoria.

### 05. El Optimizador (Refactoring y Rendimiento)
- Mantener las funciones pequeñas y con una sola responsabilidad.
- Optimizar la carga de iconos y drawables para minimizar el tamaño final del APK.

### 06. El Escudo (Testing y Cobertura)
- Utilizar **Robolectric** para pruebas locales de la JVM y **Roborazzi** para tests visuales de captura de pantalla.
- Prohibido intentar ejecutar tests instrumentados con emulador `adb` en este entorno.

### 07. El Narrador (Documentación Técnica)
- Mantener sincronizados y actualizados los archivos `README.md`, `ROADMAP.md`, `STRUCTURE.md`, `AI_CONTEXT.md` y este `AGENTS.md`.

---

## 🛠️ Reglas Específicas de Tecnología

- **Shizuku API**: Toda interacción con procesos de sistema debe pasar por `ShizukuManager.kt`. Siempre verificar que `ShizukuState` sea `AUTHORIZED` antes de emitir llamadas IPC.
- **Forzado de Renderizado Gráfico (Por Juego)**: Nunca usar propiedades globales `persist.sys.*`. Utilizar inyección aislada por nombre de paquete mediante `settings put global` y garantizar la reversión automática al volver a la aplicación.
- **Centinela en Juego e Hibernación (`GameWatcherService`)**:
  - La hibernación de procesos en segundo plano (`am set-inactive`, `pm suspend`) y la suspensión de Google Play Services solo deben operar activamente mientras el juego esté en primer plano.
  - Al salir del juego o pasar a segundo plano, se debe ejecutar inmediatamente la restauración completa del sistema a sus valores normales de fábrica.
  - Registrar y mantener `BootRecoveryReceiver` para garantizar el restablecimiento ante cualquier reinicio imprevisto del dispositivo.
- **Escala de Resolución y DPI (`DisplayScaleController`)**: Toda modificación de `wm size` o `wm density` debe regirse por la arquitectura failsafe de 5 capas (Watchdog Daemon con timeout de 35s en shell desacoplado, Botón de Pánico permanente en notificación `EmergencyResetReceiver`, `BootRecoveryReceiver`, clamping simétrico par y test de 15s con auto-revert).
- **Touch Boost y Optimizador Wi-Fi**:
  - `TouchResponseController` y `NetworkOptimizerController` deben siempre guardar el estado previo del usuario en `BoosterPreferences` antes de modificar `pointer_speed`, `refresh_rate`, escalas de animación o `wifi_suspend_optimizations_enabled`.
  - La restauración a los valores originales debe ejecutarse fielmente al salir del juego mediante `GameWatcherService` y ante reinicios en `BootRecoveryReceiver`.
- **Nativo C++ / Rust**: Mantener siempre los métodos de respaldo (*fallback*) en Kotlin puro dentro de `NativeEngineBridge.kt` y `RustCoreBridge.kt` para asegurar que la app compila y se ejecuta incluso si los archivos `.so` no han sido generados por el NDK.
- **Telemetría Exacta**: Realizar el cálculo del uso de CPU en tiempo real leyendo deltas de `/proc/stat` y temperatura del SoC en nodos térmicos reales del kernel.
- **Material Design 3**: Utilizar la paleta de colores centralizada en `Theme.kt` y `Color.kt` con estilo Neon Gamer.
