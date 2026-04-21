# 📋 Especificación de Requisitos de Negocio (PRD) - NutriPharma MVP

**Versión:** 4.1 (Consolidada: Arquitectura Domain-Driven Modular, Sistema de Diseño Shadcn, Auditoría, UI/UX y Roadmap a Producción)
**Objetivo:** Servir de fuente de verdad absoluta para el desarrollo, justificando el porqué de las decisiones técnicas y de negocio (alineado con BITACORA.md).

---

## 1. ARQUITECTURA DE ENTIDADES Y ACCESOS

### 1.1. Relación Base del Negocio
* El sistema abandona la relación 1:N simple para adoptar una arquitectura **Bidireccional (N:M)** entre Nutricionistas y Farmacias.
* La relación N:M incluye atributos propios, como la distancia en Kilómetros entre la residencia del empleado y el local comercial.
* **Asignación Manual:** El Administrador asigna explícitamente en qué Farmacia(s) opera cada Nutricionista.
* **Aislamiento de Datos:** Un Nutricionista solo puede interactuar (pedidos, consultas) con las farmacias que tenga en su perfil.
* **Caso de Uso Contemplado:** Aunque es raro, una misma farmacia puede tener asociadas a dos o más nutricionistas simultáneamente, lo que impacta en el motor de comisiones.

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

### 1.3. Jerarquía Extendida y Gestión de Datos Históricos (Soft Delete)
Para preservar la integridad de las auditorías y la trazabilidad (facturas, consultas y pedidos pasados), el sistema implementa un **Borrado Lógico (Soft Delete)** en todas las entidades principales. Nunca se hace un `DELETE` físico en la base de datos.

* **Rol SUPERADMIN:** Único que puede ver, crear, suspender o restaurar a otros Administradores.
* **Rol ADMIN:** Visibilidad de historial completo de Farmacias, Nutricionistas y Productos para fines de auditoría. No controla a otros Admins.
* **Roles Operativos:** Solo visualizan entidades `activas` (farmacias vigentes, nutricionistas en plantilla y productos catalogados).

---

## 2. MÓDULOS OPERATIVOS (Features)

### 2.1. Módulo: Administración y Gatekeeper (Control de Flujo)
* **El "Gatekeeper" (Centro de Validaciones):** Bandeja de entrada centralizada. Los pedidos y consultas no afectan a las finanzas ni a los objetivos hasta que el Admin los valida manualmente.
* **Administración Maestro:** CRUD completo para gestionar Farmacias, Nutricionistas (Contratos) y Productos (PVP, PVF).
* **Gestión de Personal Interno:** Capacidad exclusiva del SUPERADMIN para gestionar usuarios con rol ADMIN.

### 2.2. Módulo: Turnos y Consultas (Motor de Datos Médicos)
* **Estructura:** Registro de "Turno Mañana" y/o "Turno Tarde".
* **KPIs:** Nuevas, Revisiones, Promo, Personal Farmacia.
* **Certificación de Pruebas:** Sellado de tiempo obligatorio en fotos de agenda para evitar reportes extemporáneos.
* **Estados:** `Borrador` (Editable) ➔ `Pendiente Validación` (Enviada a Central) ➔ `Validada` (Aprobada operativamente) ➔ `Liquidada` (Cierre de caja completado) o `Con Incidencia` (Error reportado).

### 2.3. Módulo: Suministros y Material corporativo
* Catálogo de consumibles con cantidades predefinidas.
* **Regla Anti-Spam:** Si un ítem está "Solicitado", desaparece del catálogo hasta que el Admin resuelva la petición.

---

## 3. MÓDULO COMERCIAL Y PEDIDOS B2B

### 3.1. Delegación Administrativa (Pedidos Proxy)
El Admin puede realizar pedidos en nombre de una Farmacia. La DB registra la autoría real (`creadoPorAdmin` y `creadoPorNombre`).

### 3.2. Política de Precios Geográfica
* **Almería:** Aplica tarifa **P.V.F.** (Punto de Venta Farmacia).
* **Resto de España:** Aplica tarifa **P.V.P.**

### 3.3. La "Doble Cesta" y Regla de los 80€
1.  **Cesta Principal (Pago Real):** Solo computa para el bonus de la nutricionista.
2.  **Desbloqueo:** Si la Cesta Principal es `< 80€`, el sistema bloquea el uso del monedero (Saldo Virtual).
3.  **Cesta de Liquidación (Pago con Saldo):** Productos adquiridos en especie. No suman bonus.

### 3.4. Regla Comercial de Unidades Bonificadas
Algoritmo automático: 100 uds ➔ 20 gratis | 20 uds ➔ 5 gratis | 10 uds ➔ 2 gratis | 6 uds ➔ 1 gratis.

---

## 4. MODELO FINANCIERO Y COMISIONES

### 4.1. Generación Económica en Consultas
* **Tarifario:** Consulta Nueva (25€), Revisión (20€).
* **Comisión Variable:** El porcentaje que va a la Farmacia es configurable individualmente (ej. 20%, 30%).

### 4.2. Sistema de Incentivos de Nutricionistas (Bonus)
Basado en jornada de 40h (escalable por contrato):
* **OB1:** Meta 5.000€ (Mín. Prod 800€) ➔ Bono 200€
* **OB2:** Meta 6.800€ (Mín. Prod 1.000€) ➔ Bono 400€ + 5% del exceso.
* **OB3:** Meta 8.700€ (Mín. Prod 1.200€) ➔ Bono 600€ + 10% del exceso.

### 4.3. Motor de Comisiones por Ventas B2B
Si hay 2+ nutricionistas en la misma Farmacia, el sistema obliga mediante un Modal a establecer el porcentaje de reparto para ese pedido.

### 4.4. Compensación por Desplazamiento (Kilometraje)
Registro de distancia única por par `[Nutricionista ↔ Farmacia]`. Se computa viaje de ida y vuelta por cada turno `CONFIRMADO`.

### 4.5. Cierre de Caja (Liquidaciones)
* Interfaz de contabilidad dedicada a la consolidación financiera de jornadas.
* Permite la selección múltiple (Batch Processing) de consultas en estado `VALIDADA`, calculando en tiempo real el volumen económico a liquidar mediante filtros por nutricionista y mes.
* **Edición Retroactiva (Compensación):** El Administrador posee capacidad de edición forzada sobre consultas ya validadas (antes de su liquidación) para corregir errores humanos. La operación queda trazada de forma inmutable en la tabla de auditoría (`consultas_aud`).

### 4.6. Generador de Informes
* Herramienta de extracción y visualización de métricas.
* Basado en los datos consolidados del resumen financiero, permitirá la exportación de rendimiento operativo (consultas, comisiones, liquidaciones) para justificación contable interna.

---

## 5. DISEÑO UI/UX Y SISTEMA DE COMPONENTES 🆕

* **Paleta Corporativa Estricta:**
    * **Primario:** `#367933` (Acción/Éxito)
    * **Secundario:** `#062e3a` (Institucional)
    * **Acento:** `#b1cb0c` (Resalte con opacidad)
    * **Gris:** `#342c1e` (Texto/Bordes)
* **Sistema de Diseño (Shadcn + Radix UI):** La interfaz se construye íntegramente sobre una librería de componentes agnóstica a la lógica de negocio ubicada en `src/shared/ui/`.
* **Prohibición de Estilos en Línea:** Queda restringido el uso de colores hexadecimales duros o estilos en línea. Toda la UI debe nutrirse de las variables de `index.css` a través de clases de Tailwind v4.

---

## 6. COMUNICACIONES Y AUDITORÍA DE DATOS

* **Asincronía:** Arquitectura Event-Driven para envío de correos y PDFs (Thymeleaf + OpenPDF).
* **Notario Digital:** Historial de cambios inmutable (Hibernate Envers) con sellado de tiempo y autoría.
* **Trazabilidad:** Registro de IPs y User-Agents en cada login.

---

## 7. ARQUITECTURA FRONTEND Y ROADMAP A PRODUCCIÓN 🆕

### 7.1. Domain-Driven Modular Architecture (Frontend)
Para garantizar la escalabilidad y simetría con el backend en Spring Boot, el frontend adopta una estructura modular por dominios de negocio:
* **`modules/`:** Carpeta raíz que contiene subdirectorios idénticos al backend (`security`, `operations`, `organization`, `sales`, `documents`). Cada módulo es auto-contenido (posee sus propios componentes, hooks, servicios y vistas).
* **API Pública por Módulo:** Para prevenir el código espagueti, los módulos solo pueden comunicarse entre sí importando desde los archivos `index.js` expuestos en la raíz de cada módulo.
* **Separación Global:** La configuración estructural (`app/`), el enrutamiento centralizado y el kit de UI (`shared/`) viven estrictamente fuera de la lógica de negocio.

### 7.2. Aseguramiento de la Calidad (Testing)
* **Backend:** JUnit 5 y Mockito para lógica financiera.
* **Frontend:** Vitest para componentes y Playwright para flujos críticos (E2E).

### 7.3. Estrategia de Despliegue
* **Contenerización:** Docker Compose para orquestar App, DB y Nginx.
* **Infraestructura:** VPS Ubuntu Server, SSL (Let's Encrypt), Proxy Inverso y blindaje UFW.
* **Objetivo de Producción:** 15 de Mayo.