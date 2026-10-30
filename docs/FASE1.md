# Verificación de la ampliación de fase 1

## Cobertura de requisitos

| Requisitos | Implementación |
| --- | --- |
| RF-P01, RF-P05 | Selector mensual en Planificar, proyección encadenada desde el saldo real actual; patrimonio separado del dinero libre. |
| RF-P02 | Fuentes de ingreso con fechas reales de calendario: cada 7 días, cada 14 días, dos días configurados por mes o día mensual. |
| RF-P03, RF-P04 | Planes independientes de ingreso, gasto y aportación; cuenta prevista opcional, categoría y subcategoría para gastos. |
| RF-P06, RF-P08 | Edición, cancelación y eliminación de pendientes; estados visibles. Cancelar un cobro generado guarda una excepción para que no reaparezca. |
| RF-P07 | Confirmación atómica de movimiento y estado, fecha ejecutada, cuenta y vínculo persistente. Repetir la confirmación no aumenta el saldo otra vez. |
| FIX-01 a FIX-04 | Selector en la tarjeta de Inicio, periodos configurables, saldos actuales independientes del periodo, desglose por cuenta en Ajustes. |
| FIX-05 a FIX-08 | Tipos internos diferenciados, edición y eliminación validadas, filtros compartidos con Inicio; planes separados del historial real. |
| FIX-09 a FIX-12 | Metas en progreso, porcentaje sobre saldo total, metas completadas visibles, edición, archivado y reapertura. |
| FIX-13, FIX-14 | Aportaciones y retiros con historial; aportaciones previstas separadas en el detalle. |
| Persistencia | Room con escritura transaccional del estado financiero versionado, ViewModel y Repository; perfil y preferencias en DataStore. |
| CSV y respaldo | CSV con columnas reales/previstas, fecha, estado, categoría, subcategoría y cuentas; JSON financiero completo y restauración validada. |

## Pruebas automatizadas

- `FinanceTest`, `DashboardDataTest`, `SavingsAndFiltersTest`: reglas del historial real, saldos, transferencias, reservas, eliminación y filtros.
- `PlanningTest`: fechas salariales, cinco cobros semanales, diferencia catorcenal/quincenal, fin de febrero, edición y cancelación, confirmación idempotente manual y generada, patrimonio y saldo libre, proyección negativa, reapertura de metas y filtros por cuenta/categoría en los cinco periodos.
- `PersistenceTest`: respaldo completo y vínculos ejecutados, metas archivadas con planes, rechazo de centavos fraccionarios, reapertura de la base y rollback de una transacción interrumpida.
- `PlanningScreenTest`: confirmación de un gasto futuro desde Compose, actualización de saldo y estado ejecutado.
- `StartupRenderingTest`: carga asíncrona, píxeles reales y navegación entre las tres secciones y Ajustes.

## Comprobación manual sugerida

1. Crear dos cuentas en Ajustes y registrar saldo mediante un ingreso.
2. Cambiar cuenta, categoría y periodo en Inicio; abrir Movimientos y comprobar los mismos filtros. Los saldos actuales no deben cambiar por el periodo.
3. Crear una meta, agregar fondos hasta completarla y aumentar el objetivo. Debe volver a contarse como meta en progreso. Archivar y reabrir desde Ajustes.
4. Abrir Planificar desde Inicio, agregar una fuente semanal con fecha de referencia y revisar las fechas de dos meses distintos.
5. Crear y editar un gasto futuro y una aportación; comprobar que solo varían las proyecciones. Confirmar el gasto y comprobar el historial real.
6. Exportar CSV y guardar JSON mediante el selector Android. Restaurar el JSON, reiniciar la app y comprobar historial, planes y estados.

Los supuestos de proyección y el horizonte del CSV se documentan en el README. Los datos del prototipo anterior eran exclusivamente de sesión; no existía una base anterior que migrar.

## Resultado del 28 de octubre de 2026

- `assembleDebug`: aprobado; APK en `app/build/outputs/apk/debug/app-debug.apk`.
- `testDebugUnitTest`: 29 pruebas, 0 fallos.
- `connectedDebugAndroidTest`: 6 pruebas, 0 fallos, en Android 17/API 37.
- `git diff --check`: sin errores de espacios.

La primera comprobación visual encontró un diálogo de ANR de System UI que tapaba la aplicación. Tras reducir la resolución según `BLACK_SCREEN.md` y cerrar ese diálogo del sistema, la captura real y la prueba de navegación aprobaron. No se deshabilitó la verificación de píxeles.
