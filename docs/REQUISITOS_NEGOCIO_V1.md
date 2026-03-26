# 📋 Especificación de Requisitos de Negocio (PRD) - NutriPharma MVP

**Versión:** 2.2 (Consolidada: Arquitectura N:M, Gatekeeper, RBAC, Reglas Geográficas, Comisiones Variables, Gestión de Admins y Soft Delete/Visibilidad)
**Objetivo:** Servir de fuente de verdad absoluta para el desarrollo, justificando el porqué de las decisiones técnicas y de negocio (alineado con BITACORA.md).

---

## 1. ARQUITECTURA DE ENTIDADES Y ACCESOS

### 1.1. Relación Base del Negocio

El sistema abandona la relación 1:N simple para adoptar una arquitectura Bidireccional (N:M) entre Nutricionistas y Farmacias. La relación N:M incluye atributos propios, como la distancia en Kilómetros entre la residencia del empleado y el local comercial.

* **Asignación Manual:** El Administrador asigna explícitamente en qué Farmacia(s) opera cada Nutricionista.
* **Aislamiento de Datos:** Un Nutricionista solo puede interactuar (pedidos, consultas) con las farmacias que tenga en su perfil.
* **Caso de Uso Contemplado:** Aunque es raro, una misma farmacia puede tener asociadas a dos o más nutricionistas simultáneamente, lo que impacta en el motor de comisiones (ver sección 4.3).

### 1.2. Matriz de Roles y Vistas (RBAC)

El menú y los componentes de React mutan dinámicamente según el JWT del usuario.

| Módulo / Funcionalidad | Rol: ADMIN | Rol: NUTRICIONISTA | Rol: FARMACIA |
| :--- | :---: | :---: | :---: |
| **Dashboard (Resumen)** | ✅ Gráficas Generales y Calendario | ✅ KPIs, Bonus y Bolsa Horas | ✅ Saldo Virtual y Compras |
| **Consultas (Registro)** | ❌ (Solo lectura en Gatekeeper) | ✅ Registro y Edición | ❌ Bloqueado |
| **Consultas (Historial)** | ✅ Acceso Total Global | ✅ Historial Propio | ✅ Historial Local (Auditoría) |
| **Pedidos (Catálogo)** | ✅ Proxy (En nombre de otros) | ✅ Selecciona Farmacia (N:M) | ✅ Automático (Propia) |
| **Suministros** | ✅ Aprobación (Gatekeeper) | ✅ Solicitud | ❌ Bloqueado |
| **Documentación** | ✅ Subida y Borrado (Drive) | ✅ Lectura / Descarga | ✅ Lectura / Descarga |
| **Admin Maestro (CRUD)** | ✅ Gestión Total | ❌ Bloqueado | ❌ Bloqueado |

### 1.3. Jerarquía Extendida y Gestión de Datos Históricos (Soft Delete) 🆕 [NUEVO]

Para preservar la integridad de las auditorías y la trazabilidad (facturas, consultas y pedidos pasados), el sistema implementa un **Borrado Lógico (Soft Delete)** en todas las entidades principales (Usuarios, Farmacias, Nutricionistas, Productos). Nunca se hace un `DELETE` físico en la base de datos.

* **Reglas de Visibilidad por Rol:**
  * **Rol SUPERADMIN:** Usuario fundador/dueño. Tiene acceso y control total. Es el **único** que puede ver, crear, suspender o restaurar a otros Administradores.
  * **Rol ADMIN (Personal de Central):** Tienen visibilidad del historial completo de Farmacias, Nutricionistas y Productos (tanto activos como dados de baja/descatalogados) para fines de auditoría y restauración. **No** tienen visibilidad ni control sobre otros usuarios con rol Admin.
  * **Roles Operativos (FARMACIA y NUTRICIONISTA):** Operan en "abstracción total". Solo visualizan entidades `activas` (farmacias vigentes, nutricionistas en plantilla y productos catalogados).

---

## 2. MÓDULOS OPERATIVOS (Features)

### 2.1. Módulo: Administración y Gatekeeper (Control de Flujo)

* **El "Gatekeeper" (Centro de Validaciones):** Bandeja de entrada centralizada. Los pedidos y consultas no afectan a las finanzas ni a los objetivos hasta que el Admin los valida manualmente. Actúa como filtro antifraude y de calidad.
* **Administración Maestro:** CRUD completo para gestionar Farmacias (Fiscal, Dirección, **Porcentaje de Comisión Acordado**), Nutricionistas (Contratos) y Productos (Catálogo, PVP, PVF). El borrado será lógico para no dejar datos huérfanos.
* **Gestión de Personal Interno:** Capacidad exclusiva del `SUPERADMIN` para crear y dar de alta a nuevos usuarios con el rol `ADMIN` directamente desde la interfaz del panel.

### 2.2. Módulo: Turnos y Consultas (Motor de Datos Médicos)

* **Estructura Diaria:** Se permite registrar "Turno Mañana" y/o "Turno Tarde".
* **KPIs Recolectados:** Nuevas, Revisiones, Promo (Gratis), Personal Farmacia (Gratis).
* **Máquina de Estados:**
  1.  **Borrador:** Editable por el creador.
  2.  **Confirmada:** Bloqueada. Pasa al Gatekeeper del Admin.
  3.  **Con Incidencia:** El nutricionista reporta un error; solo el Admin puede desbloquear/corregir.

### 2.3. Módulo: Suministros y Material corporativo

* Catálogo de consumibles (folletos, bolígrafos) con cantidades predefinidas por central.
* **Máquina de Estados:** `SOLICITADO` ➔ `APROBADO` (Admin) ➔ `CANCELADO`.
* **Regla Anti-Spam:** Si un ítem está "Solicitado", desaparece del catálogo del usuario hasta que el Admin resuelva la petición, evitando duplicidades.


### 2.4. Motor de Incidencias y Corrección de Errores (Ticketing B2B)

Para gestionar los errores humanos en la introducción de datos (ej. equivocación en el recuento de pacientes), se implementa un sistema de resolución asíncrono entre Nutricionistas y Central.

* **Single Source of Truth (Ticket Único):** Para evitar la dispersión de información, una consulta solo puede tener un estado de incidencia activo (`CON_INCIDENCIA`).
* **Propiedad del Creador:** La Nutricionista puede abrir una incidencia sobre su consulta en cualquier momento, lo que bloquea su liquidación financiera. Si detecta nuevos errores antes de que la central los resuelva, el sistema le permite **editar** su propio mensaje de incidencia, en lugar de crear uno nuevo.
* **Flujo de Resolución (Admin):** El Administrador, desde el *Gatekeeper* (Centro de Validaciones), visualiza las alertas. Es el único con privilegios para entrar al modo edición, corregir los datos numéricos basándose en el texto de la nutricionista, y marcar el ticket como resuelto (`VALIDADA`).
* **UX/UI No Invasiva:** Los textos largos (Observaciones de jornada o mensajes de incidencia) se resuelven visualmente mediante expansiones verticales (*Accordions*) dentro de la propia tarjeta, evitando la sobrecarga de ventanas modales.

### 2.5. Integridad de Agendas (Control Anti-Solapamiento)

Para garantizar la coherencia operativa y evitar fraudes o errores humanos en la imputación de jornadas, el sistema aplica una validación estricta de tiempo basada en la intersección de intervalos (Time Overlap).

* **Single Source of Truth Temporal:** Aunque el sistema categorice cualitativamente los turnos (Mañana, Tarde, Día Completo), la validación de colisiones se calcula exclusivamente usando los valores `horaInicio` y `horaFin` (precisión de minutos).
* **Reglas de Validación Backend:**
  1.  **Coherencia Lineal:** La `horaFin` debe ser estrictamente posterior a la `horaInicio`.
  2.  **Exclusividad Espacio-Temporal:** Un nutricionista no puede tener dos registros en la misma `fecha` si sus intervalos de horas se cruzan. La regla matemática aplicada es: `(NuevoInicio < ExistenteFin) AND (NuevoFin > ExistenteInicio)`.
  3.  **Excepción de Cancelados:** Los turnos con estado `CANCELADA` se excluyen de esta validación para liberar el espacio horario en caso de error.
* **Gestión de Errores:** Esta validación ocurre en la capa de servicio del backend antes de cualquier persistencia, devolviendo una excepción de conflicto manejada por el frontend mediante alertas amigables.
---

## 3. MÓDULO COMERCIAL Y PEDIDOS B2B

### 3.1. Delegación Administrativa (Pedidos Proxy)

* El Administrador puede suplantar la acción de compra realizando pedidos telefónicos en nombre de una Farmacia.
* **Trazabilidad:** La Base de Datos registra la autoría real (`creadoPorAdmin: true/false`). Las comisiones generadas por este pedido proxy van igualmente destinadas a las nutricionistas de esa farmacia.

### 3.2. Política de Precios Geográfica

* **PVF vs PVP:** Los productos tienen dos tarifas. El sistema decide cuál aplicar en el carrito en tiempo real basándose en la ubicación de la Farmacia.
* **Regla:** Farmacias ubicadas en "Almería" ➔ Aplica **P.V.F.**. Farmacias fuera de Almería ➔ Aplica **P.V.P.**

### 3.3. La "Doble Cesta" y Regla de los 80€ (Legalidad Andaluza)

Por normativa, NutriPharma (Servicio Externo) no puede transferir comisiones en efectivo a la Farmacia, sino en especie (Saldo Virtual).

1.  **Cesta Principal (Pago Real):** Productos pagados en euros. Solo estos computan para el bonus de la nutricionista.
2.  **Desbloqueo (Umbral Mínimo):** Si la Cesta Principal es `< 80€`, el sistema bloquea el uso del monedero. Al superar los 80€, se habilita la segunda cesta.
3.  **Cesta de Liquidación (Pago con Saldo):** Productos adquiridos gratis descontando su valor del "Saldo Virtual" de la farmacia. Estos no suman bonus a la nutricionista.

### 3.4. Regla Comercial de Unidades Bonificadas

Algoritmo automático en la Cesta Principal para proteger márgenes (sobrescribible por el Admin):

* 100 uds ➔ 20 gratis | 20 uds ➔ 5 gratis | 10 uds ➔ 2 gratis | 6 uds ➔ 1 gratis.

### 3.5. Gestión de Catálogo y Estados de Producto 🆕 [NUEVO]

Los productos poseen una doble capa de disponibilidad para mantener la consistencia operativa y visual:
* **Catálogo (Activo vs Descatalogado):** Determina si el producto existe a nivel comercial. Si un producto se descataloga, desaparece de la vista operativa, pero sus registros históricos en pedidos antiguos se conservan intactos por el Borrado Lógico.
* **Inventario (En Stock vs Agotado):** Atributo dinámico para productos activos. Si no hay stock, el producto **sí** se muestra a Farmacias y Nutricionistas, pero su diseño es en escala de grises con el botón de añadir a la cesta deshabilitado (Agotado).

---

## 4. MODELO FINANCIERO Y COMISIONES (Repartos y Nóminas)

### 4.1. Generación Económica en Consultas

El servicio médico a pacientes genera dinero directo a repartir:

* **Tarifario:** Consulta Nueva (25€), Revisión (20€).
* **Modelo de Comisión Variable:** El importe total generado se reparte entre NutriPharma y la Farmacia por cesión de espacio. El porcentaje que se transforma en Saldo Virtual para la Farmacia ya no es un 30% fijo, sino un valor numérico configurable y renegociable de manera individual para cada establecimiento (Ej. 20%, 30%, 40%) en su ficha de Administración.

### 4.2. Sistema de Incentivos de Nutricionistas (Bonus)

El salario se complementa mediante cálculos basados en una jornada estándar de 40h (se aplica un multiplicador según horas reales de contrato).

* **Facturación Computable:** (Consultas Nuevas + Revisiones) + Ventas B2B de Cesta Principal.
* **Tramos de Bonus (Base 40h):**
  * **OB1:** Meta 5.000€ (Mín. Prod 800€) ➔ Bono 200€
  * **OB2:** Meta 6.800€ (Mín. Prod 1.000€) ➔ Bono 400€ + 5% del exceso.
  * **OB3:** Meta 8.700€ (Mín. Prod 1.200€) ➔ Bono 600€ + 10% del exceso.

### 4.3. Motor de Comisiones por Ventas B2B

Cuando una Farmacia (o el Admin como Proxy) compra productos (Cesta Principal), se genera una comisión para el Nutricionista.

* **Escenario Normal (1 Nutricionista):** El 100% de la comisión asignada a esa farmacia se imputa automáticamente al nutricionista vinculado.
* **Escenario Complejo (2+ Nutricionistas en la misma Farmacia):** El sistema intercepta el pedido (sea hecho por la Farmacia o por el Admin) y obliga mediante un Modal a establecer manualmente el porcentaje de reparto (Ej. 50-50, 70-30) entre los profesionales asociados a ese local para ese pedido en concreto.

### 4.4. Compensación por Desplazamiento (Kilometraje)

El sistema debe llevar un registro automático del desgaste por desplazamiento para su posterior compensación económica extra-plataforma.

* **Atributo Relacional:** La distancia (en kilómetros) se define de forma única para cada par `[Nutricionista ↔ Farmacia]`. El Administrador debe especificar este valor numérico en el momento de asignar una farmacia al perfil de la nutricionista.
* **Cálculo de Acumulación Mensual:** Cada vez que una nutricionista registra un turno (consulta) con estado `CONFIRMADA` en una farmacia, el sistema computa un "Viaje" (Ida y Vuelta).
* **Visibilidad:** El "Resumen Operativo" de la Nutricionista debe mostrar el Total de Kilómetros Acumulados en el mes en curso, calculado como: `Σ (Consultas Confirmadas en Farmacia X * Distancia a Farmacia X)`. La aplicación no calcula euros por gasolina, solo acumula la métrica de distancia bruta.

---

## 5. DISEÑO UI/UX Y BRANDING CORPORATIVO

El sistema abandona los colores genéricos de las librerías CSS para adoptar una identidad visual 100% alineada con la marca NutriPharma. La implementación se realiza inyectando los valores hexadecimales exactos en la capa de presentación (vía Tailwind CSS).

### 5.1. Paleta de Colores Base (Por orden de jerarquía)

* **Color Principal (Acción/Primario):** `#367933` (Verde oscuro)
  * *Uso:* Botones de acción principal, elementos activos del menú lateral, barras de progreso y textos clave de éxito. Transmite salud y crecimiento.
* **Color Secundario (Institucional/Contraste):** `#062e3a` (Azul oscuro)
  * *Uso:* Encabezados (Header/Sidebar), títulos principales (H1/H2), tipografía de alto contraste y fondos de tarjetas de control. Aporta seriedad, profesionalidad y peso visual.
* **Color Terciario (Acento/Resalte):** `#b1cb0c` (Verde claro/Lima)
  * *Uso:* Combinado frecuentemente con opacidad (ej. fondos al 20%) para destacar selecciones, iluminar iconos clave, notificaciones y elementos que requieren atención rápida sin ser agresivos.
* **Color Cuaternario (Apoyo/Neutro):** `#342c1e` (Gris oscuro/Pardo)
  * *Uso:* Tipografía secundaria, subtítulos, etiquetas (labels) de formularios y bordes estructurales.

### 5.2. Gradientes Corporativos

Se utilizan exclusivamente en zonas de alto impacto (Hero Cards) para romper la monotonía visual y resaltar métricas financieras o de rendimiento, logrando armonía con la estructura de la aplicación:

* **Gradiente Claro:** De `#bed000` a `#85ac1c`
* **Gradiente Oscuro:** De `#006633` a `#68b54e`