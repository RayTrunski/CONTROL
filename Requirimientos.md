Módulos de la Fase 1
ID	Módulo	Prioridad
M01	Configuración y perfil local	Esencial
M02	Cuentas y saldos	Esencial
M03	Gestión de ingresos	Esencial
M04	Gastos y movimientos	Esencial
M05	Categorías y subcategorías	Esencial
M06	Apartados y objetivos de ahorro	Esencial
M07	Dashboard financiero	Esencial
M08	Historial y estadísticas	Esencial
M09	Exportación y respaldo	Esencial

2. Requisitos funcionales
   M01. Configuración y perfil
   Configuración inicial sin crear una cuenta online.
- Nombre o alias opcional.
- Moneda MXN y formato de fecha mexicano.
- Periodicidad de ingresos: semanal, catorcenal, quincenal o mensual.
- Primer día de la semana configurable.
- Preferencias de tema (claro, oscuro o sistema).
- Posibilidad de modificar la configuración posteriormente.
  M02. Cuentas y saldos
  Permitirá administrar las diferentes fuentes donde se encuentra el dinero.
- Crear cuentas de efectivo, débito y ahorro.
- Asignar nombre, saldo inicial y color identificador.
- Registrar transferencias entre cuentas.
- Mostrar saldo individual y saldo total.
- Editar y archivar cuentas.
- Mantener el historial de operaciones.
  Regla importante: las transferencias entre cuentas propias no se contabilizarán como ingresos o gastos.
  M03. Gestión de ingresos
  El salario será uno de los componentes centrales de nuestra aplicación.
- Registrar un salario principal.
- Configurar frecuencia y próxima fecha de cobro.
- Registrar fuentes adicionales: comisiones, propinas, bonos, trabajo independiente, etc.
- Diferenciar ingresos esperados y confirmados.
- Asociar cada ingreso confirmado a una cuenta.
- Consultar los ingresos por semana, quincena y mes.
  En esta fase el usuario confirmará manualmente los ingresos recibidos. La automatización del salario quedará para después.
  M04. Gastos y movimientos
  Registro de las operaciones financieras cotidianas.
- Agregar gasto con importe, fecha, cuenta y categoría.
- Descripción opcional.
- Editar o eliminar movimientos.
- Reflejar inmediatamente cada operación en el saldo.
- Permitir gastos con fecha anterior.
- Validar montos y evitar duplicaciones por operaciones repetidas.
  Un gasto de $250 realizado desde la cuenta de efectivo reducirá su saldo en $250.
  M05. Categorías y subcategorías
  Permitirá personalizar la organización financiera.

Ejemplos iniciales:
Categoría	Subcategorías
Alimentación	Supermercado, restaurantes, comida rápida
Transporte	Gasolina, transporte público, Uber
Vivienda	Renta, mantenimiento
Servicios	Internet, electricidad, agua, teléfono
Salud	Consultas, medicamentos
Entretenimiento	Streaming, videojuegos, salidas
Educación	Colegiaturas, materiales
Otros	Personalizables

El usuario podrá crear, renombrar, editar y archivar categorías y subcategorías. Eliminar una categoría no deberá borrar sus movimientos históricos.
M06. Apartados y objetivos de ahorro
Uno de los módulos principales para comenzar a diferenciar nuestra aplicación.
- Crear apartados con nombre personalizado.
- Definir monto objetivo y fecha límite opcional.
- Asignar fondos desde una cuenta existente.
- Retirar fondos y regresarlos a la cuenta.
- Visualizar progreso monetario y porcentual.
- Mostrar cuánto falta para cumplir la meta.
- Archivar los apartados completados.
  Los apartados serán virtuales, por lo que el dinero permanecerá contabilizado dentro de su cuenta original, pero no estará disponible para otros gastos sin liberarlo.
  M07. Dashboard financiero
  Pantalla principal enfocada en información fácil de interpretar.
  Ejemplo de resumen financiero
  Mi dinero


Saldo total
$15,000.00

Apartado

$5,000

Disponible

$10,000


Ingresos del mes

$12,000


Gastos del mes

$4,500



Accesos rápidos
Ingreso


Gasto


Apartado




Referencia funcional de contenido, no diseño visual definitivo.
M08. Historial y estadísticas
- Historial de ingresos, gastos, transferencias y operaciones de apartados.
- Filtros por fecha, cuenta, categoría y tipo de movimiento.
- Búsqueda por descripción.
- Totales por semana, quincena y mes.
- Gráfica básica de gastos por categoría.
- Comparativa de ingresos contra gastos.
- Acceso al detalle de cada movimiento.
  M09. Exportación y respaldo local
  La aplicación no dependerá de servicios externos.
- Exportar movimientos a CSV.
- Filtrar qué periodo se exportará.
- Incluir columnas compatibles con Excel y LibreOffice.
- Crear un archivo de respaldo local con todos los registros y configuraciones.
- Restaurar respaldos.
- Validar la integridad del archivo antes de restaurar.
- Utilizar el selector de archivos de Android para guardar y recuperar documentos.
  El respaldo será diferente de la exportación CSV: el primero permitirá recuperar toda la información; el segundo estará orientado al análisis.

3. Reglas fundamentales del motor financiero
   Estas reglas deben implementarse y probarse antes de comenzar a desarrollar las estadísticas.
   Operación	Resultado
   Ingreso confirmado de $2,000	Aumenta $2,000 el saldo de la cuenta
   Gasto de $500	Reduce $500 el saldo de la cuenta
   Transferencia de $1,000	Disminuye una cuenta y aumenta otra; el total no cambia
   Apartar $800	El saldo total permanece igual, pero disminuye el disponible
   Retirar $300 de un apartado	Aumenta el disponible sin crear un ingreso
   Editar un gasto de $200 a $250	Corrige el saldo en $50 adicionales
   Eliminar un gasto	Revierte su efecto sin alterar otros movimientos

Todas las operaciones que afecten varias entidades deberán ser atómicas mediante transacciones de Room.

El sistema también deberá conservar la precisión de los importes y evitar almacenar dinero mediante Float o Double.
4. Navegación inicial de la aplicación
   Propongo cinco secciones principales:

Inicio



Movimientos



Apartados



Análisis



Ajustes




El registro de ingresos y gastos se haría mediante un botón flotante o una acción principal accesible desde Inicio y Movimientos.

Las cuentas y categorías podrían gestionarse desde Ajustes, evitando sobrecargar la barra principal.