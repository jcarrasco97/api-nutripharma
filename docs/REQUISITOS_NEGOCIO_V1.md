# 📋 Especificación de Requisitos de Negocio (PRD) - NutriPharma MVP

**Versión:** 3.0 (Roadmap Profesional a Producción e Integridad de Evidencias)
**Objetivo:** Servir de fuente de verdad absoluta para el desarrollo, justificando el porqué de las decisiones técnicas y de negocio (alineado con BITACORA.md).

---

## 1. ARQUITECTURA DE ENTIDADES Y ACCESOS

### 1.1. Relación Base del Negocio
El sistema adopta una arquitectura Bidireccional (N:M) entre Nutricionistas y Farmacias, incluyendo atributos de relación como el kilometraje.
- **Asignación Manual:** El Administrador gestiona el vínculo.
- **Aislamiento:** El Nutricionista solo opera con farmacias vinculadas a su perfil.

### 1.2. Matriz de Roles y Vistas (RBAC)
Interfaz adaptativa según el JWT del usuario:
- **ADMIN:** Visión global, Gatekeeper y gestión de entidades.
- **NUTRICIONISTA:** Registro operativo y KPIs individuales.
- **FARMACIA:** Consulta de saldo virtual y auditoría local de ventas.

### 1.3. Gestión de Datos Históricos (Soft Delete)
Implementación de Borrado Lógico en todas las entidades. Se preserva la integridad referencial para auditorías financieras. El `SUPERADMIN` es el único con capacidad de restauración de cuentas.

---

## 2. MÓDULOS OPERATIVOS (Features)

### 2.1. Módulo: Administración y Gatekeeper (Control de Flujo)
- **Centro de Validaciones:** Filtro manual de Admin antes de impactar en finanzas.
- **CRUD Maestro:** Gestión de porcentajes de comisión individuales por farmacia.

### 2.2. Módulo: Turnos y Consultas (Motor de Datos Médicos)
- **Estructura:** Turnos de Mañana/Tarde con KPIs (Nuevas, Revisiones, Promociones).
- **Certificación de Pruebas:** Sellado de tiempo obligatorio al subir fotos de agenda para evitar reportes extemporáneos.

### 2.3. Módulo: Suministros y Material corporativo
- Catálogo con lógica anti-spam (ocultación de ítems ya solicitados).

---

## 3. MÓDULO COMERCIAL Y PEDIDOS B2B

### 3.1. Delegación Administrativa (Pedidos Proxy)
Trazabilidad de autoría para pedidos realizados por la central en nombre de la farmacia.

### 3.2. Política de Precios Geográfica
- **Almería:** Tarifa P.V.F.
- **Resto de España:** Tarifa P.V.P.

### 3.3. Regla de los 80€ (Cesta Doble)
- **Cesta 1:** Pago real (mínimo 80€ para desbloquear monedero).
- **Cesta 2:** Pago con Saldo Virtual (especie).

---

## 4. MODELO FINANCIERO Y COMISIONES

### 4.1. Comisiones Variables
Porcentaje de retorno a la farmacia configurable individualmente (20%, 30%, etc.).

### 4.2. Sistema de Incentivos (Bonus)
Tramos OB1, OB2 y OB3 basados en facturación computable y cumplimiento de objetivos de producto.

---

## 5. DISEÑO UI/UX Y BRANDING
Identidad visual corporativa basada en Tailwind CSS:
- **Primario:** `#367933` | **Secundario:** `#062e3a` | **Acento:** `#b1cb0c`

---

## 6. COMUNICACIONES Y AUDITORÍA

### 6.1. Notificaciones Asíncronas
Envío de facturas PDF y confirmaciones mediante arquitectura event-driven (no bloqueante).

### 6.2. Auditoría Forense (El Notario Digital)
Registro inmutable de cambios en base de datos (Envers) y trazabilidad de red (IPs/User-Agents) para blindaje legal.

---

## 7. ANEXO: CALIDAD PROFESIONAL Y DESPLIEGUE (ROADMAP) 🆕

### 7.1. Estándares de Código y Documentación
- **Refactorización:** Aplicación de patrones Clean Code y principios SOLID.
- **Documentación Técnica:** Comentado exhaustivo de clases, generación de Javadoc en Backend y documentación de componentes en Frontend.

### 7.2. Aseguramiento de la Calidad (Testing)
- **Unit Testing (Backend):** Implementación de JUnit 5 y Mockito para validar la lógica de cálculo de comisiones y estados de negocio.
- **Component Testing (Frontend):** Pruebas de renderizado y flujos de usuario críticos (Login, Carrito, Validación).

### 7.3. Estrategia de Despliegue y DevOps
- **VPS Personal:** Despliegue inicial en servidor Linux (Ubuntu) propio para pruebas de integración en entorno real.
- **Securización:** Configuración de SSL/TLS, Nginx como Proxy Inverso y blindaje de puertos.
- **Migración a Producción:** Traspaso de infraestructura y apuntamiento de dominio corporativo (`erp.nutripharma.es`) para operación final.