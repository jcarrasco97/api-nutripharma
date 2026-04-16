# 📝 Notas y Tareas de Desarrollo - 9 de Abril 2026
*Archivo temporal de volcado de ideas, bugs y features pendientes.*

## 🗣️ Reunión y Dudas para Nacho (Lunes 13/04)
- [X] **Aclarar "Agendas":** ¿Las consultas están relacionadas con las agendas? (Confirmar si con "agendas" se refieren simplemente a las fotos de las consultas).
- [X] **Facturas de Nutricionistas:** Definir dónde y cómo se deben subir las facturas de las nutricionistas (kilometraje/gastos).

## 🐛 Bugs Urgentes a Corregir
- [X] **Centro de Validaciones:** Las tarjetas se han bugueado después de la reestructuración en VSCode.
- [X] **Historial de Pedidos:** Actualmente roto, necesita revisión.
- [X] **Informe Detallado (Admin):** La descripción de los productos aparece como `undefined` cuando hay varios productos.

## ⚙️ Backend, DevOps y Testing
- [ ] **Logs en Producción:** Configurar `logback-spring.xml` para la creación del archivo de logs cuando se despliegue en Linux.
- [ ] **Migración Google Drive:** - Pasar del Drive personal al Drive de la empresa.
    - Implementar creación de carpetas dinámicas (por usuario/función) en lugar de volcarlo todo en la raíz.
    - Comprobar límites de seguridad y cuotas de la API.
- [ ] **Deuda Técnica (Clean Code):** RECOMENTAR todas las clases y scripts tanto en Backend como en Frontend.
- [ ] **Testing:** Implementar Testing Enterprise (Unitario y de Integración) en Backend (JUnit5/Mockito) y Frontend.

## 🛡️ Trazabilidad, Notificaciones y Logs
- [X] **Doble Sello de Tiempo (Consultas):** Registrar y mostrar visualmente a ambos perfiles el momento exacto en el que se *sube* la consulta (independiente de la fecha en la que se *realizó* el turno y de la subida de la foto).
- [ ] **Visor de Logs Admin:** Evaluar si se implementa un apartado exclusivo para el Superadmin donde lea los logs del sistema desde la UI.
- [ ] **Bandeja de Notificaciones:** Estudiar viabilidad e implementación de una campanita (arriba a la derecha) para avisos de acciones pendientes e incidencias bidireccionales.
- [ ] **Buscadores:** Mejorar la trazabilidad visual en los historiales y optimizar la búsqueda global con filtros más precisos (buscar por identificador y nombre asociado).

## 📦 Lógica de Negocio (Pedidos, Productos y Comisiones)
- [X] **Algoritmo de Bonificaciones:** Revisar fórmula matemática (Ej: Si compras 50, ¿tienes 5 bonificadas o 12?). Evitar que la fórmula falle.
- [X] **Claridad en el Carrito/Envío:** Reflejar de manera impecable al usuario el desglose final: Total = Comprados + Bonificados + Liquidados/Regalados.

## 💻 Frontend - Interfaz Nutricionistas
- [X] **Mis Consultas (Turnos):** Limitar horas seleccionables a horario laboral. Hacer que la selección de un turno preseleccione el rango de horas automáticamente y viceversa.
- [X] **Resumen (Rendimiento):** - Evaluar creación de pestaña/ventana de rendimiento histórico por meses (solo lectura).
- [ ] **Resumen (Rendimiento):** - Evaluar visualización del rendimiento anual total.- [ ]

## 🖥️ Frontend - Interfaz Administrador
- [ ] **Dashboard (Calendario):** Hacer interactivo el calendario de Pedidos y Consultas para que redirijan al detalle al hacer clic.
- [ ] **Dashboard (Gráficos):** - Mejora visual del Gráfico de Facturación Global.
    - Añadir súper-filtros: Separar Consultas vs Venta de Productos.
    - Sub-filtros: Por producto individual, por nutricionista (rendimiento global vs local) y por farmacia.
    - Enlace rápido desde los elementos del gráfico hacia el "Pop-up de Detalles" de la sección Administración.
- [X] **Gestión de Empleados:**
    - Implementar filtros avanzados para Nutricionistas, Farmacias y Productos.
- [X] **Ordenación de Productos:** "Nachón con el látigo" -> Definir la lógica/criterio para ordenar la forma en la que les aparecen los productos a las nutris y farmacias.


## 🎨 UI/UX General
- [ ] Mejorar el diseño visual global de la aplicación.
- [ ] Revisar y estandarizar la lógica y simbología de los componentes interactivos (botones, switches, modales).