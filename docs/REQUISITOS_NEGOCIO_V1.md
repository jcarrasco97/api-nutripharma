# 📋 Especificación de Requisitos de Negocio (PRD) - NutriPharma MVP

**Versión:** 2.0 (Consolidada: Arquitectura N:M, Gatekeeper, RBAC y Reglas Geográficas)
**Objetivo:** Servir de fuente de verdad absoluta para el desarrollo, justificando el porqué de las decisiones técnicas y de negocio (alineado con BITACORA.md).

---

## 1. ARQUITECTURA DE ENTIDADES Y ACCESOS

### 1.1. Relación Base del Negocio 🆕 [NUEVO 23/03/2026]

El sistema abandona la relación 1:N simple para adoptar una arquitectura Bidireccional (N:M) entre Nutricionistas y Farmacias. La relación N:M incluye atributos propios, como la distancia en Kilómetros entre la residencia del empleado y el local comercial.

- **Asignación Manual:** El Administrador asigna explícitamente en qué Farmacia(s) opera cada Nutricionista.
- **Aislamiento de Datos:** Un Nutricionista solo puede interactuar (pedidos, consultas) con las farmacias que tenga en su perfil.
- **Caso de Uso Contemplado:** Aunque es raro, una misma farmacia puede tener asociadas a dos o más nutricionistas simultáneamente, lo que impacta en el motor de comisiones (ver sección 4.3).

### 1.2. Matriz de Roles y Vistas (RBAC)

El menú y los componentes de React mutan dinámicamente según el JWT del usuario.

| Módulo / Funcionalidad    |             Rol: ADMIN             |      Rol: NUTRICIONISTA      |         Rol: FARMACIA          |
| :------------------------ | :--------------------------------: | :--------------------------: | :----------------------------: |
| **Dashboard (Resumen)**   | ✅ Gráficas Generales y Calendario | ✅ KPIs, Bonus y Bolsa Horas |   ✅ Saldo Virtual y Compras   |
| **Consultas (Registro)**  |  ❌ (Solo lectura en Gatekeeper)   |    ✅ Registro y Edición     |          ❌ Bloqueado          |
| **Consultas (Historial)** |       ✅ Acceso Total Global       |     ✅ Historial Propio      | ✅ Historial Local (Auditoría) |
| **Pedidos (Catálogo)**    |   ✅ Proxy (En nombre de otros)    | ✅ Selecciona Farmacia (N:M) |     ✅ Automático (Propia)     |
| **Suministros**           |     ✅ Aprobación (Gatekeeper)     |         ✅ Solicitud         |          ❌ Bloqueado          |
| **Documentación**         |    ✅ Subida y Borrado (Drive)     |    ✅ Lectura / Descarga     |     ✅ Lectura / Descarga      |
| **Admin Maestro (CRUD)**  |          ✅ Gestión Total          |         ❌ Bloqueado         |          ❌ Bloqueado          |

---

## 2. MÓDULOS OPERATIVOS (Features)

### 2.1. Módulo: Administración y Gatekeeper (Control de Flujo)

- **El "Gatekeeper" (Centro de Validaciones):** Bandeja de entrada centralizada. Los pedidos y consultas no afectan a las finanzas ni a los objetivos hasta que el Admin los valida manualmente. Actúa como filtro antifraude y de calidad.
- **Administración Maestro:** CRUD completo para gestionar Farmacias (Fiscal, Dirección), Nutricionistas (Contratos) y Productos (Catálogo, PVP, PVF). El borrado debe ser atómico y en cascada para no dejar datos huérfanos.

### 2.2. Módulo: Turnos y Consultas (Motor de Datos Médicos)

- **Estructura Diaria:** Se permite registrar "Turno Mañana" y/o "Turno Tarde".
- **KPIs Recolectados:** Nuevas, Revisiones, Promo (Gratis), Personal Farmacia (Gratis).
- **Máquina de Estados:**
  1. **Borrador:** Editable por el creador.
  2. **Confirmada:** Bloqueada. Pasa al Gatekeeper del Admin.
  3. **Con Incidencia:** El nutricionista reporta un error; solo el Admin puede desbloquear/corregir.

### 2.3. Módulo: Suministros y Material corporativo

- Catálogo de consumibles (folletos, bolígrafos) con cantidades predefinidas por central.
- **Máquina de Estados:** `SOLICITADO` ➔ `APROBADO` (Admin) ➔ `CANCELADO`.
- **Regla Anti-Spam:** Si un ítem está "Solicitado", desaparece del catálogo del usuario hasta que el Admin resuelva la petición, evitando duplicidades.

---

## 3. MÓDULO COMERCIAL Y PEDIDOS B2B

### 3.1. Delegación Administrativa (Pedidos Proxy) 🆕 [NUEVO 23/03/2026]

- El Administrador puede suplantar la acción de compra realizando pedidos telefónicos en nombre de una Farmacia.
- **Trazabilidad:** La Base de Datos registra la autoría real (`creadoPorAdmin: true/false`). Las comisiones generadas por este pedido proxy van igualmente destinadas a las nutricionistas de esa farmacia.

### 3.2. Política de Precios Geográfica 🆕 [NUEVO 23/03/2026]

- **PVF vs PVP:** Los productos tienen dos tarifas. El sistema decide cuál aplicar en el carrito en tiempo real basándose en la ubicación de la Farmacia.
- **Regla:** Farmacias ubicadas en "Almería" ➔ Aplica **P.V.F.**. Farmacias fuera de Almería ➔ Aplica **P.V.P.**

### 3.3. La "Doble Cesta" y Regla de los 80€ (Legalidad Andaluza)

Por normativa, NutriPharma (Servicio Externo) no puede transferir comisiones en efectivo a la Farmacia, sino en especie (Saldo Virtual).

1. **Cesta Principal (Pago Real):** Productos pagados en euros. Solo estos computan para el bonus de la nutricionista.
2. **Desbloqueo (Umbral Mínimo):** Si la Cesta Principal es `< 80€`, el sistema bloquea el uso del monedero. Al superar los 80€, se habilita la segunda cesta.
3. **Cesta de Liquidación (Pago con Saldo):** Productos adquiridos gratis descontando su valor del "Saldo Virtual" de la farmacia. Estos no suman bonus a la nutricionista.

### 3.4. Regla Comercial de Unidades Bonificadas

Algoritmo automático en la Cesta Principal para proteger márgenes (sobrescribible por el Admin):

- 100 uds ➔ 20 gratis | 20 uds ➔ 5 gratis | 10 uds ➔ 2 gratis | 6 uds ➔ 1 gratis.

---

## 4. MODELO FINANCIERO Y COMISIONES (Repartos y Nóminas)

### 4.1. Generación Económica en Consultas

El servicio médico a pacientes genera dinero directo a repartir:

- **Tarifario:** Consulta Nueva (25€), Revisión (20€).
- **Modelo 70/30:** 70% íntegro para NutriPharma. 30% se transforma en Saldo Virtual para la Farmacia por cesión de espacio.

### 4.2. Sistema de Incentivos de Nutricionistas (Bonus)

El salario se complementa mediante cálculos basados en una jornada estándar de 40h (se aplica un multiplicador según horas reales de contrato).

- **Facturación Computable:** (Consultas Nuevas + Revisiones) + Ventas B2B de Cesta Principal.
- **Tramos de Bonus (Base 40h):**
  - **OB1:** Meta 5.000€ (Mín. Prod 800€) ➔ Bono 200€
  - **OB2:** Meta 6.800€ (Mín. Prod 1.000€) ➔ Bono 400€ + 5% del exceso.
  - **OB3:** Meta 8.700€ (Mín. Prod 1.200€) ➔ Bono 600€ + 10% del exceso.

### 4.3. Motor de Comisiones por Ventas B2B 🆕 [NUEVO 23/03/2026]

Cuando una Farmacia (o el Admin como Proxy) compra productos (Cesta Principal), se genera una comisión para el Nutricionista.

- **Escenario Normal (1 Nutricionista):** El 100% de la comisión asignada a esa farmacia se imputa automáticamente al nutricionista vinculado.
- **Escenario Complejo (2+ Nutricionistas en la misma Farmacia):** El sistema intercepta el pedido (sea hecho por la Farmacia o por el Admin) y obliga mediante un Modal a establecer manualmente el porcentaje de reparto (Ej. 50-50, 70-30) entre los profesionales asociados a ese local para ese pedido en concreto.

### 4.4. Compensación por Desplazamiento (Kilometraje) 🆕 [NUEVO]

El sistema debe llevar un registro automático del desgaste por desplazamiento para su posterior compensación económica extra-plataforma.

- **Atributo Relacional:** La distancia (en kilómetros) se define de forma única para cada par `[Nutricionista ↔ Farmacia]`. El Administrador debe especificar este valor numérico en el momento de asignar una farmacia al perfil de la nutricionista.
- **Cálculo de Acumulación Mensual:** Cada vez que una nutricionista registra un turno (consulta) con estado `CONFIRMADA` en una farmacia, el sistema computa un "Viaje" (Ida y Vuelta).
- **Visibilidad:** El "Resumen Operativo" de la Nutricionista debe mostrar el Total de Kilómetros Acumulados en el mes en curso, calculado como: `Σ (Consultas Confirmadas en Farmacia X * Distancia a Farmacia X)`. La aplicación no calcula euros por gasolina, solo acumula la métrica de distancia bruta.
