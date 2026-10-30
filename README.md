# Control

Plataforma Android de **gestión y planificación financiera personal**, completamente local, en MXN. Kotlin, Jetpack Compose, Room, DataStore y MVVM con Repository.

## Fase 1

- Tres pestañas: Inicio, Movimientos y Apartados. Acceso secundario **Planificar** desde Inicio.
- Cuentas con saldo actual total, reservado y disponible; desglose individual en Ajustes.
- Filtros compartidos de cuenta, categoría y periodo entre Inicio y Movimientos. Periodos semanal, catorcenal, quincenal, mensual y personalizado, con fechas de referencia configurables y guardadas en DataStore.
- Ingresos, gastos con categoría y subcategoría, transferencias entre cuentas, aportaciones y retiros. Edición y eliminación con validación de los saldos resultantes. Las operaciones internas no cuentan como ingresos ni gastos.
- Metas en progreso, metas completadas visibles, edición para reabrir objetivos, archivado y reapertura desde Ajustes. Historial y aportaciones previstas en el detalle de cada meta.
- Planificación mensual de ingresos, gastos y aportaciones, edición, cancelación y eliminación de pendientes. Estados pendiente, ejecutado y cancelado.
- Fuentes de ingreso semanales, catorcenales, quincenales y mensuales. Las fechas parten de la referencia configurada; catorcenal significa cada 14 días. Se ajustan días como el 31 al último día del mes. Quitar una fuente conserva las operaciones ya confirmadas.
- Proyección encadenada entre meses con saldo libre y patrimonial separados. Las reservas futuras reducen solo el saldo libre. Los saldos proyectados negativos se resaltan.
- Confirmación de planes como movimientos reales con la fecha de confirmación y cuenta seleccionada; vínculo persistente e identificador determinista para impedir duplicados.
- Perfil local, persistencia offline, CSV con importes reales y previstos diferenciados y respaldo/restauración JSON mediante el selector de documentos Android.

## Persistencia e integridad

El Repository guarda el libro financiero completo como un estado versionado dentro de una transacción Room. Así, los movimientos, sus vínculos de planificación y sus efectos sobre los saldos se guardan juntos. Los saldos se derivan del historial; no existen contadores independientes que puedan quedar desactualizados. Los importes y cálculos monetarios usan centavos `Long` y operaciones exactas; las aproximaciones se limitan al dibujo de gráficas.

La base local comienza en la versión 1, sin migraciones destructivas. La entrega anterior mantenía todo en memoria y no tenía una base persistente que migrar. Las futuras versiones de la base o del formato de respaldo deberán incorporar migraciones explícitas. Los errores de lectura se muestran sin sustituir silenciosamente los datos por un libro vacío.

Los respaldos financieros incluyen cuentas, movimientos, metas, fuentes, planes, estados, fechas y vínculos. Se validan antes de reemplazar los datos actuales; una restauración inválida conserva el libro anterior. El perfil y las preferencias de consulta pertenecen al dispositivo. Antes de restaurar se muestra una confirmación porque reemplaza el libro completo.

## Supuestos de planificación básica

Las proyecciones parten del dinero real actual y consideran operaciones pendientes desde hoy. Los ingresos y gastos se suponen ejecutados según las fechas previstas; no se generan movimientos reales automáticamente. El mes actual representa el resto del mes desde hoy; los meses siguientes parten del cierre libre proyectado anterior. Los pendientes vencidos siguen visibles en su mes para reprogramarlos o confirmarlos.

El CSV incluye todo el historial, todos los planes almacenados y los cobros generados del mes actual y los doce meses siguientes. El respaldo conserva las fuentes completas para regenerar fechas posteriores a ese horizonte.

No se incluyen servicios externos, bancos, sincronización, presupuestos avanzados ni XLSX.

## Verificación

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest --console=plain
.\gradlew.bat connectedDebugAndroidTest --console=plain
```

Las pruebas cubren saldos, transferencias, reservas, edición y eliminación, fechas salariales, proyecciones negativas, confirmación idempotente, reapertura de metas, filtros, respaldo/restauración, reapertura de Room y rollback atómico. La prueba de inicio comprueba píxeles reales y navegación. Si el emulador muestra una pantalla negra, consultar [diagnóstico del entorno](docs/BLACK_SCREEN.md).

Requisitos: [ampliación de fase 1](<Requisitos fase 1.md>) y [documento original](Requirimientos.md).
