# Diagnóstico de pantalla negra

## Evidencia

El AVD `Medium_Phone_API_37.0` ejecutaba Android 17/API 37 con `hw.gpu.mode=auto` y restauración Fast Boot. Android registró ANR de Control por `Application does not have a focused window`.

La traza capturada mostraba el hilo principal en `MessageQueue.nativePollOnce` y RenderThread esperando trabajo; no había una excepción de Kotlin ni un bucle de cálculo en el hilo principal. El árbol de Compose contenía la pantalla, aunque las capturas eran negras. Por ello comprobar únicamente nodos semánticos no bastaba para validar el renderizado.

Tras reiniciar el mismo AVD sin restaurar la instantánea y con gráficos por software, Inicio apareció. Durante el arranque también se observó un ANR de System UI. Reducir la resolución a 720×1600 y densidad 280 alivió la carga, manteniendo el tamaño lógico de la interfaz. Esta prueba identifica un problema del entorno de emulación; no aísla por separado el controlador gráfico y la instantánea anterior.

## Correcciones

- AVD: `hw.gpu.mode=software`, `fastboot.forceColdBoot=yes`, `fastboot.forceFastBoot=no`. Los datos del dispositivo no se borraron.
- Copia previa de configuración: `.gradle/Medium_Phone.config.before-render-fix.ini` (local, fuera del control de versiones).
- Resolución del emulador: 720×1600, densidad 280. Se puede restaurar con `adb shell wm size reset` y `adb shell wm density reset`.
- App: fondo de ventana explícito y barras de sistema con iconos oscuros coherentes con el tema claro de Control.
- Prueba instrumentada `StartupRenderingTest`: valida colores reales de la captura de pantalla y navega por Inicio, Movimientos, Apartados y Ajustes. Una pantalla negra debe fallar aunque existan nodos de Compose.

Para otro AVD, seleccionar **Software** en Graphics y ejecutar **Cold Boot** desde Device Manager. La corrección del AVD es independiente del APK: reinstalar el APK por sí solo no cambia el renderizador del emulador.

Referencia oficial: https://developer.android.com/studio/run/emulator-acceleration#accel-graphics

## Verificación

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest connectedDebugAndroidTest --console=plain
```

Resultado del 27 de octubre de 2026: compilación correcta, 20 pruebas unitarias y 2 pruebas instrumentadas aprobadas (incluida la prueba de píxeles y navegación). El APK normal se reinstaló después de que el ejecutor limpiara los paquetes de prueba.
