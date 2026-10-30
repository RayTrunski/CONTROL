# Control — Actualización de requisitos funcionales

**Versión:** Fase 1 — Ampliación de alcance y correcciones
**Plataforma:** Android / Kotlin / Jetpack Compose
**Arquitectura:** Offline-first
**Mercado inicial:** México (MXN)

## 1. Actualización del objetivo del proyecto

Modificar la descripción general de Control para establecer que su propósito es ofrecer una plataforma de **gestión y planificación financiera personal**.

La aplicación deberá permitir administrar las finanzas actuales y planificar ingresos, gastos, compromisos y objetivos de ahorro para los próximos meses.

La gestión del dinero real y la planificación financiera deberán funcionar de forma independiente, pero compartir la información necesaria para generar cálculos consistentes.

**Objetivos principales:**

- Registrar y administrar las finanzas actuales.
- Organizar ingresos según la periodicidad salarial.
- Distribuir dinero entre cuentas y apartados.
- Planificar ingresos y gastos de meses futuros.
- Calcular saldos disponibles y proyectados.
- Proporcionar herramientas de análisis financiero personal.
- Funcionar completamente sin internet.

---

## 2. Nuevo módulo: Planificación financiera

**ID:** M10
**Nombre:** Planificación financiera mensual
**Prioridad:** Alta — Incorporar una versión básica a la Fase 1.

### Requisitos funcionales

**RF-P01 — Selector de periodo futuro**

Permitir seleccionar meses posteriores al actual para consultar y crear planificaciones financieras.

**RF-P02 — Ingresos proyectados**

Calcular ingresos estimados a partir de las fuentes de ingreso registradas, considerando:

- Salario semanal.
- Salario catorcenal.
- Salario quincenal.
- Salario mensual.
- Ingresos adicionales planificados.

Respetar las fechas de cobro configuradas. No asumir que todos los meses tienen cuatro semanas ni que los periodos catorcenales equivalen a las quincenas.

**RF-P03 — Gastos planificados**

Permitir registrar gastos futuros indicando:

- Nombre o concepto.
- Importe.
- Fecha estimada.
- Categoría y subcategoría.
- Cuenta de origen prevista, opcional.

Estos registros no deben afectar los saldos reales hasta que se confirme su ejecución.

**RF-P04 — Apartados planificados**

Permitir definir aportaciones futuras a objetivos de ahorro existentes.

Las aportaciones planificadas deben disminuir el saldo libre proyectado sin tratarse como gastos reales.

**RF-P05 — Resumen financiero proyectado**

Mostrar para cada mes:

- Saldo inicial proyectado.
- Ingresos previstos.
- Gastos previstos.
- Aportaciones a apartados previstas.
- Saldo libre final proyectado.

Calcular el saldo de cada mes tomando como referencia el cierre proyectado del mes anterior. Mantener diferenciados el saldo patrimonial y el saldo libre para evitar descontar dos veces los apartados.

**RF-P06 — Edición de planificación**

Permitir crear, editar, cancelar y eliminar operaciones futuras sin modificar el historial financiero real.

**RF-P07 — Confirmación de operaciones**

Permitir convertir un ingreso o gasto planificado en una operación real.

Al confirmar:

- Registrar el movimiento real en la cuenta correspondiente.
- Actualizar el saldo de dicha cuenta.
- Marcar el elemento planificado como ejecutado.
- Evitar duplicar movimientos al volver a confirmar.
- Recalcular las proyecciones pendientes.

**RF-P08 — Estados de planificación**

Distinguir los estados:

- Pendiente.
- Ejecutado.
- Cancelado.

### Integración en la interfaz

Mantener las tres pestañas principales:

1. Inicio.
2. Movimientos.
3. Apartados.

Agregar en Inicio un acceso secundario llamado **«Planificar»**.

Este acceso abrirá una pantalla de planificación que incluya un selector mensual, resumen de ingresos y gastos previstos y operaciones futuras.

No crear una cuarta pestaña de navegación en esta fase.

---

## 3. Correcciones de Inicio (Dashboard)

**FIX-01 — Selector de cuentas**

Reemplazar el botón de mostrar/ocultar saldo por un selector de cuentas.

Permitir seleccionar una cuenta específica o todas las cuentas.

El filtro deberá afectar los indicadores y movimientos correspondientes, manteniendo consistencia entre las cifras presentadas.

**FIX-02 — Periodicidad configurable**

Eliminar la dependencia exclusiva del mes actual para mostrar ingresos y gastos.

Permitir seleccionar:

- Semanal.
- Catorcenal.
- Quincenal.
- Mensual.
- Periodo personalizado.

Los periodos deberán calcularse según las preferencias configuradas por el usuario.

**FIX-03 — Consistencia de saldos**

Mantener la siguiente separación:

- Saldo total: dinero real registrado en las cuentas.
- Saldo reservado: dinero asignado a apartados.
- Saldo disponible: saldo total menos saldo reservado.

Cambiar el periodo de análisis no debe modificar el saldo actual. Las consultas de saldos históricos o proyectados deberán identificarse explícitamente.

**FIX-04 — Desglose financiero**

Permitir consultar el saldo individual por cuenta, incluyendo su cantidad reservada y disponible.

---

## 4. Correcciones de Movimientos

**FIX-05 — Clasificación de movimientos**

Distinguir visual y funcionalmente:

- Ingresos.
- Gastos.
- Movimientos entre cuentas propias.
- Aportaciones y retiros de apartados.

Las operaciones internas no deben afectar los totales de ingresos y gastos.

**FIX-06 — Integridad financiera**

Editar o eliminar un movimiento deberá actualizar correctamente los saldos afectados.

Las operaciones que involucren múltiples registros deberán ejecutarse mediante transacciones atómicas de Room.

**FIX-07 — Filtros consistentes**

Mantener coherencia entre los filtros de cuenta, categoría y periodo de Inicio y Movimientos.

**FIX-08 — Operaciones planificadas**

Las operaciones futuras pendientes podrán consultarse desde Planificación, sin mezclarlas con los movimientos confirmados.

---

## 5. Correcciones de Apartados

**FIX-09 — Terminología**

Cambiar «Apartados activos» por **«Metas en progreso»**.

Una meta que alcanza el 100% dejará de contabilizarse como meta en progreso, pero el apartado seguirá existiendo.

**FIX-10 — Porcentaje reservado**

Cambiar «69% de tus activos» por **«69% de tu saldo total»**, calculándolo dinámicamente según los importes reales.

**FIX-11 — Apartados completados**

Mantener visibles los apartados que hayan alcanzado su meta hasta que el usuario decida archivarlos.

**FIX-12 — Edición y reapertura**

Permitir editar apartados incluso después de alcanzar el 100%.

El usuario podrá modificar el objetivo o fecha para reutilizar el apartado sin tener que crearlo nuevamente.

Si el objetivo aumenta por encima de la cantidad ahorrada, el apartado volverá al estado de meta en progreso.

**FIX-13 — Movimientos internos**

Permitir agregar y retirar dinero de cada apartado, conservando un historial de operaciones.

Estas operaciones no deberán contabilizarse como ingresos ni gastos.

**FIX-14 — Planificación de ahorro**

Permitir consultar las aportaciones futuras previstas para un apartado, diferenciándolas del dinero efectivamente reservado.

---

## 6. Reglas del motor financiero

Implementar y validar las siguientes reglas:

1. Los movimientos confirmados afectan el saldo real.
2. Los movimientos planificados no afectan el saldo real.
3. Las transferencias entre cuentas propias no generan ingresos ni gastos.
4. Los movimientos entre cuentas y apartados no modifican el patrimonio total.
5. Los ingresos proyectados no representan ingresos recibidos.
6. Las aportaciones planificadas no representan dinero ya reservado.
7. Las proyecciones deberán recalcularse cuando cambien los movimientos reales o los datos de planificación.
8. Confirmar una operación futura no deberá generar duplicados.
9. Los cálculos de dinero utilizarán valores exactos en centavos, evitando `Float` y `Double`.
10. Los saldos negativos deberán identificarse claramente, especialmente en las proyecciones de meses futuros.

Para la primera implementación, las proyecciones se calcularán bajo el supuesto de que los ingresos esperados se reciben y los gastos planificados se ejecutan en las fechas configuradas.

---

## 7. Persistencia y arquitectura

Mantener Kotlin, Jetpack Compose, Room, DataStore y arquitectura MVVM + Repository.

Ampliar el modelo de datos para permitir almacenar:

- Fuentes de ingresos y periodicidades.
- Operaciones financieras reales.
- Operaciones planificadas.
- Estados de planificación.
- Fechas estimadas y ejecutadas.
- Relaciones entre movimientos planificados y confirmados.
- Aportaciones futuras a apartados.

Separar la lógica de cálculo financiero de los componentes visuales.

No implementar Firebase, Supabase, servicios externos ni APIs bancarias.

Todas las funcionalidades deberán funcionar localmente.

---

## 8. Exportación y respaldos

Mantener los requisitos de exportación CSV y respaldo local.

Ampliar la exportación para permitir incluir, cuando corresponda, las operaciones financieras planificadas.

Los archivos deberán distinguir claramente:

- Importe real.
- Importe previsto.
- Fecha.
- Estado.
- Categoría.
- Cuenta asociada.

La restauración deberá recuperar tanto los registros reales como las planificaciones futuras.

La exportación XLSX con fórmulas avanzadas permanece fuera del alcance de esta fase.

---

## 9. Pruebas de aceptación

Validar los siguientes escenarios:

- Registrar un ingreso semanal y comprobar las fechas proyectadas de los meses siguientes.
- Confirmar un ingreso previsto y verificar que el saldo real aumente una sola vez.
- Registrar un gasto futuro y comprobar que el saldo actual no cambie.
- Editar un gasto planificado y verificar el recálculo de los saldos futuros.
- Crear una aportación futura a un apartado y comprobar que solo afecte al saldo libre proyectado.
- Transferir dinero entre cuentas sin alterar los ingresos o gastos globales.
- Completar un apartado, modificar su objetivo y verificar que pueda volver a estar en progreso.
- Aplicar filtros semanales, quincenales y mensuales y comprobar la consistencia de los resultados.
- Verificar que una planificación mensual pueda mostrar un saldo proyectado negativo sin modificar el saldo real.
- Reiniciar la aplicación y comprobar que movimientos y planificaciones sigan disponibles.
- Exportar y restaurar un respaldo con información histórica y futura.

---

## 10. Alcance final de la Fase 1

La Fase 1 incluirá:

- Configuración y perfil local.
- Gestión de cuentas y saldos.
- Registro de ingresos y periodicidades.
- Gastos y movimientos.
- Categorías y subcategorías.
- Apartados y objetivos de ahorro.
- Dashboard financiero.
- Historial y estadísticas básicas.
- Exportación CSV y respaldo local.
- **Planificación financiera mensual básica.**

### Fuera del alcance

Mantener para fases posteriores:

- Automatización completa de cargos recurrentes.
- Integraciones bancarias.
- Tarjetas de crédito y meses sin intereses.
- Presupuestos avanzados.
- Simulación comparativa de múltiples escenarios.
- Fórmulas financieras personalizadas.
- Exportaciones XLSX avanzadas.
- Sincronización en la nube.

### Criterio de finalización

La Fase 1 se considerará completa cuando el usuario pueda administrar su dinero actual, organizar sus ahorros y planificar los próximos meses, con resultados consistentes, registros persistentes y funcionamiento completamente offline.

**Importante:** conservar las funcionalidades existentes y realizar cambios incrementales. No reconstruir las tres pantallas principales ni alterar el historial financiero actual sin una migración de datos segura.