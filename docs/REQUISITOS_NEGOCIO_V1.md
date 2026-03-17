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