# Control

Aplicación Android de finanzas personales, local y en MXN, desarrollada con Kotlin y Jetpack Compose.

## Primera entrega

- Motor financiero inmutable con importes en centavos (`Long`) y validación de saldos.
- Creación de cuentas y pantalla inicial con saldo total, apartado y disponible.
- Registro manual de ingresos, gastos, transferencias, reservas y liberaciones.
- Eliminación de movimientos con validación del saldo resultante; edición disponible en el motor.
- Pruebas unitarias de las reglas financieras y de conversión exacta de importes.

## Inicio

- Diseño basado en la referencia: tarjeta verde de saldo, resumen mensual, gráfica de dona, accesos rápidos y barra inferior.
- Saldo total, disponible y apartado calculados desde el motor, con opción de ocultar importes.
- Ingresos y gastos del mes con distribución en ocho intervalos y comparación contra el mes anterior. Sin datos del mes anterior, no se inventa un porcentaje.
- Distribución de gastos por categoría, alimentada por la selección de categoría al registrar gastos.
- Formularios de ingresos, gastos, transferencias y reservas; creación de la primera cuenta desde el registro.
- Últimos movimientos con acceso al detalle y al historial completo.
- Barra inferior con Inicio, Movimientos y Apartados. Ajustes y perfil se abren desde el icono de usuario de cada pantalla; el análisis de categorías se consulta desde «Ver más».
- Inicio vacío por defecto, sin saldos ni operaciones de demostración. Vista previa de Compose disponible en `HomeScreen.kt`.

## Movimientos y apartados

- Historial agrupado por día, resumen diario de ingresos menos gastos y detalle de operaciones.
- Pestañas de todos, ingresos, gastos y transferencias; búsqueda y filtros por mes, cuenta y categoría. Las transferencias aparecen para la cuenta de origen y la de destino.
- Botón flotante para registrar operaciones.
- Apartados individuales con nombre, icono, monto objetivo y fecha opcional; tarjetas con avance y detalle del dinero reservado por cuenta.
- Agregar y retirar fondos por apartado. El motor impide retirar fondos de otra meta o cuenta y mantiene el saldo total sin generar ingresos o gastos.

Los gráficos usan proporciones aproximadas únicamente para dibujar; los importes y las sumas conservan centavos enteros. La interfaz actual sigue el tema claro de la referencia.

Esta entrega es un prototipo en memoria: los datos se pierden al cerrar o recrear la actividad. No debe usarse todavía para guardar información financiera real.

## Siguientes etapas

1. Persistencia Room con transacciones atómicas y conservación del estado durante el ciclo de vida de Android.
2. Perfil local, cuentas con tipo, saldo inicial, color y archivado.
3. Gestión personalizada de categorías, edición de movimientos e ingresos esperados.
4. Edición y archivado de objetivos; ampliar las tres secciones y los ajustes del perfil.
5. Historial y estadísticas después de verificar el motor persistente.
6. CSV, respaldo local y restauración validada mediante el selector de documentos Android.

Los requisitos originales se encuentran en `Requirimientos.md`.

## Verificación

Si el emulador muestra una pantalla negra, consultar [diagnóstico y correcciones del entorno](docs/BLACK_SCREEN.md). Se añadió una prueba instrumentada que verifica píxeles reales del inicio y la navegación, además de las pruebas unitarias.

En Windows, con JDK y Android SDK configurados:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest --console=plain
```

Con un emulador o dispositivo conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest --console=plain
```
