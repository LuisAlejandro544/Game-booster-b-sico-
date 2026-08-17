# 🗺️ Hoja de Ruta (Roadmap) - Game Booster Turbo

Este documento traza las fases de desarrollo y evolución de **Game Booster Turbo**, priorizando el rendimiento, la independencia de servicios propietarios y la experiencia de usuario gamer en dispositivos móviles.

---

## 📍 Fase 1: Núcleo, Shizuku y Renderizado GPU (Completada ✅)
- [x] Arquitectura base MVVM con Jetpack Compose y Material 3 Neon.
- [x] Monitoreo en tiempo real exacto de CPU nativo (`/proc/stat`), SoC Temp, RAM, batería, ping y pantalla (Hz).
- [x] Integración de Shizuku API v13.1.5 y listeners de estado en vivo.
- [x] Comandos elevados ADB (`am force-stop`, `pm trim-caches`, `cmd game mode`).
- [x] **Selector de Renderizado GPU por Juego**: Forzar Vulkan Game Driver, ANGLE (OpenGL sobre Vulkan) u OpenGL Nativo de manera segura y aislada por paquete.
- [x] **Aislamiento y Reversión Automática**: Restauración automática del estado del sistema Android al salir del juego.
- [x] Selector y lanzador de juegos integrado con perfiles de optimización y botón "BOOST & JUGAR".
- [x] Infraestructura y puentes preparados para extensiones C++ y Rust.

---

## 📍 Fase 2: Automatización, Centinela e Hibernación en Vivo (Completada ✅)
- [x] **Centinela en Primer Plano (`GameWatcherService`)**: Monitoreo dinámico del ciclo de vida del juego activo en tiempo real.
- [x] **Hibernación Profunda de Procesos en Segundo Plano**: Congelación temporal de apps consumidoras (`am set-inactive` / `pm suspend`).
- [x] **Suspensión Temporal de Google Play Services**: Reducción drástica de consumo de memoria RAM (+350MB-600MB liberados) durante la partida.
- [x] **Reversión Instantánea y Fail-Safe**: Restauración inmediata de todos los servicios al salir del juego y receptor de recuperación tras reinicio (`BootRecoveryReceiver`).
- [x] **Burbuja Flotante HUD en Juego (`GameOverlayService`)**: Overlay flotante y panel desplegable con contador de FPS en tiempo real, telemetría SoC/RAM, Quick Boost al vuelo y switcher de controladores gráficos (Vulkan, ANGLE, OpenGL) directamente dentro de la partida.
- [x] **Gestión de Resolución y DPI por Juego (Failsafe en 5 Capas)**:
  - Selector de resolución y densidad (`wm size` / `wm density`) con perfiles: 100% Nativo, 85% Balance, 75% HD+ y 50% Ultra Fluidez.
  - Arquitectura blindada contra congelamientos del teléfono: Watchdog daemon shell (Dead-Man's switch 35s), Botón de Pánico permanente en notificación (`EmergencyResetReceiver`), receptor de reinicio (`BootRecoveryReceiver`), cálculo simétrico de pares de píxeles y prueba de 15 segundos con cuenta regresiva.
- [ ] **Modo No Molestar Gamer (DND)**: Silenciado de notificaciones y llamadas entrantes durante partidas mediante permisos de política de notificaciones.

---

## 📍 Fase 3: Aceleración Nativa (C++ / Rust Core)
- [x] **Telemetría Zero-Alloc en C++ NDK (`native-lib.cpp`)**:
  - Lectura de telemetría de kernel Linux `/proc/stat` y `/sys/class/thermal` a nivel nativo con buffers en pila y 0 asignaciones de memoria (Zero GC Jank).
  - Cálculo de Frametimes de alta precisión y percentiles 1% Low / 0.1% Low FPS con `CLOCK_MONOTONIC_RAW`.
- [ ] **Compilación y Empaquetado Automático NDK / Cargo**:
  - Configuración del toolchain para generar binarios `.so` precompilados de arquitecturas `arm64-v8a` y `armeabi-v7a`.
- [ ] **Módulo Rust Avanzado (`libgamebooster_rust_core.so`)**:
  - Algoritmo de filtrado de jitter y cálculo estadístico de latencia de paquetes en Rust.
  - Gestión segura de buffers de memoria.

---

## 📍 Fase 4: Distribución y Ecosistema Abierto
- [ ] **Publicación en Uptodown**: Preparación de metadatos, capturas de pantalla de alta resolución y changelogs.
- [ ] **Soporte de Actualización In-App Independiente**: Verificador de nuevas versiones APK desde repositorio GitHub / servidor sin depender de Google Play.
- [ ] **Modo Root Nativo Adicional (Magisk / KernelSU / APatch)**: Soporte para comandos directos de su si el usuario dispone de root además de Shizuku.
