# 📋 Especificación de Requisitos de Negocio (PRD) - NutriPharma MVP
**Versión:** 1.1 (Actualizado con lógica de Turnos, Bonus y Vistas Específicas)
**Módulo:** Portal de Nutricionistas y Farmacias

## 1. MATRIZ DE ROLES Y ACCESOS (Frontend Menu)
El sistema presenta un menú lateral dinámico condicionado por el rol del usuario autenticado (JWT).

| Apartado | Rol: Nutricionista | Rol: Farmacia |
| :--- | :---: | :---: |
| **1. Resumen** | ✅ Acceso (Vista Nutricionista) | ✅ Acceso (Vista Farmacia) |
| **2. Pedidos y Liquidación**| ✅ Acceso (Multi-farmacia) | ✅ Acceso (Farmacia Propia) |
| **3. Turnos y Consultas** | ✅ Acceso Total | ❌ Bloqueado |
| **4. Suministros/Materiales** | ✅ Acceso Total | ❌ Bloqueado |
| **5. Documentación** | ✅ Acceso Total | ✅ Acceso Total |

---

## 2. DESGLOSE DE FUNCIONALIDADES (Features)

### 2.1. Módulo: Resumen (Dashboard Operativo)

**Vista Exclusiva Nutricionista:**
* **Filtro Temporal:** Selector de Mes/Año.
* **Control Horario (Bolsa de Horas):** * Se calcula sumando las horas exactas registradas en los turnos de trabajo confirmados.
    * Comparativa contra "Horas por Contrato". Genera saldo a favor (positivo) o en contra (negativo).
* **Objetivos y Bonus:**
    * Algoritmo de cálculo basado en 3 variables: Volumen de ventas en pedidos (cantidad x precio), Horas de contrato y Cantidad de consultas realizadas.
* **Kilometraje:** Compensación económica por desplazamientos.
* **Resumen Anual:** Tabla histórica con el total de consultas y horas trabajadas por año.

**Vista Exclusiva Farmacia:**
* **Gráfica de Compras:** Evolución visual mensual del volumen de productos comprados.
* **Estado de Liquidación:** Indicador claro del dinero que debe liquidar actualmente la farmacia al administrador.

### 2.2. Módulo: Pedidos y Liquidaciones

**A. Formulario de Pedidos:**
* **Asignación de Autoría:** Si un Nutricionista hace el pedido, queda registrado como el "Vendedor" para su cálculo de bonus.
* **Destino (Farmacia):** * *Nutricionista:* Ve un desplegable para elegir a qué farmacia va el pedido.
    * *Farmacia:* El sistema autoselecciona su propia entidad (sin desplegable).
* **Fecha Real de Pedido:** Selector manual de fecha para trazabilidad del Administrador.
* **Líneas de Producto:** Producto, Unidades compradas, Unidades bonificadas (gratis) y Precio aplicado.

**B. Subapartado: Liquidación:**
* Solo se contabilizan las unidades reales pagadas.
* **Regla de Bloqueo (Umbral Mínimo):** No se permite la liquidación si el valor del pedido es `< 80€`. Este valor base residirá en configuración de Base de Datos para ser modificable por el Admin.

### 2.3. Módulo: Turnos y Consultas (Solo Nutricionistas)
*Registro de jornada laboral y actividad clínica.*

* **Estructura Diaria:** El usuario puede registrar un "Turno de Mañana" y/o un "Turno de Tarde". No son excluyentes ni obligatorios ambos.
* **Datos del Turno:**
    * Farmacia donde se prestó el servicio.
    * Rango Horario (Hora Inicio - Hora Fin), el cual delimita la jornada.
* **Métricas Agrupadas por Turno (Numérico):**
    * Pacientes Nuevos.
    * Revisiones.
    * Personal de Farmacia (Staff atendido).
    * Promocionales / Captaciones.
* **Flujo de Estados (Máquina de Estados):**
    1. *Pendiente/Borrador:* El nutricionista está rellenando los datos.
    2. *Confirmado (Bloqueado):* El nutricionista envía el turno. A partir de aquí, **no puede editarlo**.
    3. *Incidencia Abierta:* Si el nutricionista nota un error tras confirmar, pulsa "Abrir Incidencia". Esto notifica al Admin, quien es el único con poder para corregir los datos o desbloquear el turno.

### 2.4. Módulo: Suministros y Materiales (Solo Nutricionistas)
* Checklist predefinido en Base de Datos (ej. "Necesito Folletos", "Necesito Bolígrafos").
* Cantidades estandarizadas por la empresa, el usuario solo marca el *check* de necesidad.
* Registro histórico para auditoría administrativa (evitar abusos).

### 2.5. Módulo: Documentación
* Repositorio de lectura y descarga de PDFs/Manuales para Nutricionistas y Farmacias.
* Gestión de subida/borrado exclusiva para el Administrador.
* ## APÉNDICE A: Modelo Financiero y Normativa Legal (Andalucía)

### A.1. Contexto Legal (Servicios Externos)
Debido a la normativa vigente en Andalucía, las farmacias no pueden ofrecer servicios de nutrición directa y facturarlos como propios. Por tanto, NutriPharma actúa como una **empresa de servicios externos**. Las nutricionistas son empleadas de NutriPharma que se desplazan a la farmacia (que actúa únicamente como espacio físico cedido).

### A.2. Tarifario de Consultas
Cada vez que una nutricionista registra un turno (cierra una consulta), el sistema debe calcular el dinero generado basándose en el siguiente tarifario fijo a cobrar al paciente:
* **Consulta Nueva:** 25,00 €
* **Revisión:** 20,00 €
* **Promocional:** 0,00 € (Gratuita)
* **Personal de Farmacia:** 0,00 € (Gratuita)

### A.3. Reparto de Beneficios (Modelo 70/30)
El dinero generado en la farmacia durante el turno se divide por contrato:
* **70% para NutriPharma:** Beneficio directo de la empresa por el servicio prestado.
* **30% para la Farmacia:** Comisión por la cesión del espacio físico y la captación del cliente.

### A.4. El "Monedero Virtual" de la Farmacia (Liquidación Legal)
Por restricciones legales, NutriPharma **no puede ingresar directamente el 30%** en efectivo o transferencia a la cuenta de la farmacia.
* **Regla de Negocio:** Ese 30% se acumula en el sistema como un **"Saldo Virtual"** a favor de la farmacia.
* **Uso del Saldo:** La farmacia solo puede canjear este saldo virtual obteniendo productos físicos gratuitos de NutriPharma.
* **Condición de Desbloqueo (Regla de los 80€):** Para que una farmacia pueda aplicar su "Saldo Virtual" y llevarse productos gratis, está obligada a realizar un **pedido mínimo al por mayor de 80,00 €** (dinero real que pagan a NutriPharma). Si el pedido supera los 80€, pueden añadir productos extra y pagarlos con su saldo virtual.
* *Beneficio final de la Farmacia:* Vender esos productos conseguidos "gratis" a sus pacientes a Precio de Venta al Público (PVP), obteniendo así su comisión de forma legal (en especie).

### A.5. Sistema de Incentivos de Nutricionistas (Complementos Salariales)
El salario de las nutricionistas no es únicamente fijo. Su panel de "Resumen" debe reflejar dos métricas que afectan a su nómina a final de mes:
1.  **Bolsa de Horas:** Comparativa de horas reales trabajadas en los turnos vs. las horas estipuladas en su contrato.
2.  **Comisiones por Ventas:** Un porcentaje (bonus) asignado a la nutricionista en función del volumen en euros de los pedidos al por mayor que la farmacia donde ella trabaja realiza a NutriPharma. *(Fórmula exacta y porcentajes a definir en siguientes fases).*
* 
* ### A.6. Flujo Unificado de Pedidos y Liquidación (La "Doble Cesta")
Para resolver el conflicto de intereses entre las bonificaciones de las nutricionistas y las liquidaciones de las farmacias, el sistema de ventas unifica ambos procesos en una sola pantalla mediante la mecánica de "Doble Cesta":

1. **Cesta Principal (Pago Real):** Contiene los productos que la farmacia paga a NutriPharma. Estos productos **SÍ** computan para el cálculo del bonus de la nutricionista.
2. **Desbloqueo del Saldo Virtual:** Cuando el importe total de la Cesta Principal alcanza o supera los 80,00 €, se desbloquea el acceso al "Saldo Virtual" de la farmacia.
3. **Cesta de Liquidación (Pago con Saldo):** Una vez desbloqueado, aparece un segundo botón de acción en el catálogo. Los productos añadidos mediante este botón van a una "Cesta de Liquidación".
  * Estos productos **NO** suman coste económico al pedido.
  * Su valor de Venta a Farmacia (PVF) se resta en tiempo real del Saldo Virtual disponible de la farmacia.
  * **Restricción:** No se puede añadir un producto a esta cesta si su valor supera el saldo virtual remanente.
  * Estos productos **NO** computan para el bonus de la nutricionista.