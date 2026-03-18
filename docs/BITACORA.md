# 📓 Bitácora de Desarrollo: NutriPharma (MVP)

> **Propósito de este documento:**
> Esta bitácora documenta paso a paso la construcción de la arquitectura base de NutriPharma. Está escrita desde la perspectiva de un desarrollador en formación con el apoyo de mentoría Senior. Su objetivo es explicar el *porqué* de cada pieza de código, las decisiones de negocio y permitir reconstruir el proyecto desde cero si fuera necesario.

---

## 🏗️ CAPÍTULO 1: Origen, Negocio y Filosofía del Proyecto

### 1.1. De un Sistema Legacy a un MVP Moderno
NutriPharma no nace de la nada. El proyecto se basa en un código y un modelo de negocio anterior. Para entender las reglas del juego (entidades, consultas nutricionales, alertas de grasa, cálculo de IMC), **hicimos ingeniería inversa analizando las relaciones de las tablas SQL del sistema antiguo.** Sin embargo, adoptamos una mentalidad de **Desarrollo Iterativo (Producto Mínimo Viable - MVP)**:
* No copiamos el sistema antiguo a ciegas. Usamos el SQL como mapa mental inicial para construir nuestra API.
* Sabemos que en el futuro modificaremos partes del negocio, añadiremos campos (ej. alergias) y borraremos entidades que ya no aporten valor.
* Hemos creado un "chasis" sólido sobre el cual podemos construir y deshacer sin que la aplicación se rompa.

### 1.2. Arquitectura Desacoplada (Frontend vs Backend)
En lugar de crear una aplicación monolítica, optamos por separar las responsabilidades:
* **Backend (API REST en Java/Spring Boot):** Es el "motor" y el "cerebro". Protege la base de datos, realiza los cálculos médicos y valida la seguridad.
* **Frontend (React/Vite):** Es la "carrocería". Consume la API y se encarga única y exclusivamente de la experiencia visual del usuario (UI/UX).
* **El Beneficio:** Si el negocio evoluciona y necesitamos una App Móvil, el Backend no se toca; solo construimos otra interfaz que consuma los mismos datos.

---

## 🛠️ CAPÍTULO 2: Entorno de Trabajo y Herramientas

En el desarrollo profesional, usar la herramienta adecuada para el lenguaje adecuado dispara la productividad. Decidimos usar dos IDEs distintos:

### 2.1. Backend: IntelliJ IDEA Community
* Lo usamos exclusivamente para Java.
* Su motor de indexación, autocompletado y refactorización para lenguajes fuertemente tipados como Java no tiene rival. Nos asegura que las Entidades y las relaciones de base de datos estén robustas.

### 2.2. Frontend: Visual Studio Code (VS Code)
* Lo usamos exclusivamente para JavaScript/React. Es el rey indiscutible del Frontend por su ligereza.
* **Configuración Clave Aplicada:**
  * Instalamos extensiones vitales: *ES7+ React/Redux snippets* (para crear componentes en 1 segundo con `rfce`), *Tailwind CSS IntelliSense* (para autocompletado de clases) y *Prettier / ESLint* (para formateo automático y detección de errores).
  * Activamos el "Format on Save" para que el código se ordene automáticamente al pulsar `Ctrl + S`, liberando carga mental.

---

## ⚙️ CAPÍTULO 3: Configuración del Frontend desde Cero

### 3.1. Inicialización con Vite
Descartamos herramientas antiguas y pesadas en favor de Vite.
* **Comando:** `npm create vite@latest nutripharma-ui -- --template react`
* **Por qué:** Arranca el servidor local (`npm run dev`) en milisegundos y actualiza el navegador en tiempo real sin recargar la página entera (HMR).
* **Limpieza:** Borramos `App.css` y limpiamos `App.jsx` para tener un lienzo en blanco, evitando arrastrar estilos por defecto que interfieran con nuestro diseño.

### 3.2. La Odisea de Tailwind CSS v4
Instalamos Tailwind para manejar los estilos CSS mediante clases utilitarias (`className="bg-sky-500"`), lo que acelera el diseño enormemente.
* Nos enfrentamos a un error de configuración porque **Tailwind v4** (la versión más vanguardista) cambió su forma de conectarse con PostCSS.
* **La Solución:** Instalamos el paquete unificador (`npm install @tailwindcss/postcss`) y actualizamos `postcss.config.js` explícitamente con `"@tailwindcss/postcss": {}`.
* En `index.css`, bastó con poner un limpio `@import "tailwindcss";`.

### 3.3. Ecosistema de Librerías
* **Lucide React:** Para iconos modernos, vectoriales y estilizables directamente con las clases de Tailwind.
* **React Router Dom:** Para la navegación interna. Convierte nuestra web en una SPA (Single Page Application) donde las pantallas cambian sin parpadeos.
* **Axios:** El cliente HTTP. Lo preferimos sobre el `fetch` nativo porque transforma los JSON automáticamente y es más fácil de configurar para enviar el Token de seguridad en el futuro.

---

## 🔌 CAPÍTULO 4: El Puente entre React y Spring Boot

Para que ambas aplicaciones se hablaran, tuvimos que configurar la red y la seguridad inicial.

### 4.1. Habilitando CORS en Java
Por defecto, los navegadores bloquean peticiones entre distintos puertos por seguridad.
* React corre en `http://localhost:5173` y Spring Boot en el `8080`.
* Creamos una clase `CorsConfig` en Spring Boot para indicarle explícitamente que las peticiones que lleguen desde el puerto 5173 son seguras y permitidas.

### 4.2. DTOs Modernos con Records
En el `AuthController` de Java, en lugar de crear clases largas con Getters y Setters, usamos **Records** (ej. `record LoginRequest(String username, String password) {}`). Es la forma más limpia en Java moderno de transportar datos inmutables desde React hasta el servidor.

### 4.3. El Mock de Seguridad (Prueba de Concepto)
Como aún no tenemos la base de datos conectada a la seguridad, aplicamos el principio de "Divide y Vencerás".
* Creamos un "Simulacro" en el `AuthService` de Java. Si React enviaba el usuario `admin` y la contraseña `1234`, Java devolvía un Token de éxito simulado.
* **El objetivo cumplido:** Esto nos permitió confirmar que la comunicación HTTP y la arquitectura de red funcionan perfectamente, aislando la complejidad de la base de datos para una fase posterior.

---

## 🖥️ CAPÍTULO 5: Desarrollo de Componentes y Lógica Visual

### 5.1. El Servicio Base (`authService.js`)
* Separamos las peticiones HTTP de la interfaz visual creando la carpeta `services/`.
* Aquí configuramos Axios y la URL base de la API (`http://localhost:8080/api`). Si el servidor cambia en producción, solo hay que tocar esta línea.

### 5.2. El Componente de Login (`Login.jsx`)
Construimos un panel de acceso profesional y seguro:
1.  **Estado Controlado:** Usamos `useState` para guardar lo que el usuario teclea en tiempo real.
2.  **Manejo de Errores y Carga:** Implementamos estados de `error` y `cargando`. Al enviar la petición, los inputs y el botón se bloquean (`disabled`) para evitar doble envíos, y se muestra un icono giratorio de carga.
3.  **Captura del Token:** Tras recibir el JSON del backend, guardamos el Token en el `localStorage` del navegador.

### 5.3. Sistema de Rutas (`App.jsx` y `Dashboard.jsx`)
* Creamos un componente básico `Dashboard.jsx` como panel principal, incluyendo un botón de "Cerrar Sesión" que borra el Token del LocalStorage.
* En `App.jsx`, envolvimos la aplicación en un `<BrowserRouter>` y definimos dos "carreteras": la `/` para el Login y la `/dashboard` para el panel.
* En el Login, usamos el hook `useNavigate` para teletransportar al usuario automáticamente al Dashboard justo después de validar las credenciales con éxito.

---

## 🛡️ CAPÍTULO 6: Control de Versiones (Buenas Prácticas)

En lugar de mezclar todo en un solo lugar, mantuvimos la "higiene" del código:
* Se inicializaron repositorios de Git independientes para el Backend (`api_nutripharma`) y el Frontend (`nutripharma-ui`).
* **Protección de dependencias:** Nos aseguramos de que el archivo `.gitignore` del frontend excluyera la carpeta `node_modules` para no subir gigas de dependencias descargables al servidor de GitHub.
* Hicimos commits atómicos y subimos el proyecto a un repositorio privado en GitHub como copia de seguridad y preparación para despliegues futuros.

---

## 🔐 CAPÍTULO 7: Seguridad Real y Bóveda del Backend

Al avanzar el MVP, reemplazamos el "simulacro" por un sistema de seguridad criptográfico real conectado a base de datos.

### 7.1. Blindaje en React (Route Guards)
Para evitar que un usuario sin loguearse accediera tipeando la URL, creamos `ProtectedRoute.jsx`. Este componente envuelve al `Dashboard` y verifica la existencia del Token. Si no hay token, usa `<Navigate to="/" replace />` para expulsar al usuario automáticamente.

### 7.2. Configuración de Entidades (Spring Security)
* Habilitamos que nuestra entidad `Usuario` implementara la interfaz `UserDetails`, convirtiéndola en el estándar que Spring Security necesita para autenticar.
* Creamos la entidad `Rol` y aplicamos una relación `@ManyToMany` para establecer el Control de Acceso Basado en Roles (RBAC).

### 7.3. Configuración del Ecosistema de Seguridad
Solucionamos varios retos arquitectónicos de inyección de dependencias (`UnsatisfiedDependencyException`):
1.  **`ApplicationConfig.java`**: Creamos la fábrica de Beans donde definimos el uso de `BCryptPasswordEncoder` y el `DaoAuthenticationProvider`. Adaptamos el código a **Spring Boot 4.0 / Java 25**, pasando el `UserDetailsService` directamente en el constructor del proveedor.
2.  **`JwtAuthenticationFilter.java`**: Creamos un filtro `OncePerRequestFilter` que intercepta todas las peticiones HTTP, extrae el token del Header `Authorization: Bearer`, lo valida y registra al usuario en el `SecurityContextHolder`.
3.  **`SecurityConfig.java`**: Configuramos las reglas globales, desactivamos CSRF (innecesario con JWT), hicimos la ruta de login pública y el resto de la aplicación privada y *stateless* (sin sesión en memoria).

### 7.4. Data Seeding y Decisión de Negocio (Registro Cerrado)
Decidimos utilizar un **Modelo de Registro Cerrado** (los Nutricionistas y Farmacias no se registran solos, el Admin crea sus cuentas).
Para el entorno de desarrollo, creamos `DataSeeder.java` (usando `CommandLineRunner`) para inyectar automáticamente al usuario `admin@nutripharma.com` con sus roles correspondientes (`ROLE_ADMIN`, `ROLE_NUTRICIONISTA`) cada vez que arranca la base de datos.

---

## 🎨 CAPÍTULO 8: Frontend Dinámico (RBAC en React)

Con el backend devolviendo un Token JWT real, preparamos la interfaz para reaccionar a los roles del usuario.

### 8.1. Decodificación del Token (`jwt-decode`)
Instalamos la librería `jwt-decode` para leer el contenido (Payload) del JWT directamente en el navegador, sin necesidad de hacer peticiones extra al backend.

### 8.2. El Dashboard Dinámico y "Lazy State Initialization"
Diseñamos un Dashboard inteligente que muestra u oculta módulos (Validaciones, Calendario Global, Mis Consultas, Realizar Pedido) dependiendo de si el usuario es Admin, Nutricionista o Farmacia.

**Reto Técnico (Cascading Renders):**
* ESLint nos advirtió sobre el error de hacer un `setState` síncrono dentro de un `useEffect`, lo que causaba que React renderizara la pantalla dos veces (ineficiente).
* **Solución Senior:** Aplicamos **"Lazy State Initialization"** (Inicialización Perezosa). Pasamos una función directamente al `useState(() => { ... })` para decodificar el token en el instante exacto en que nace el componente, logrando un código limpio, rápido y sin warnings de ESLint.

---

## 🏗️ CAPÍTULO 9: Domain-Driven Design (DDD) y Reestructuración

Para asegurar la escalabilidad del proyecto, realizamos una profunda refactorización guiada por el **Lenguaje Ubicuo (Ubiquitous Language)** del negocio.

### 9.1. Eliminación de Código Muerto
* **Decisión:** Eliminamos el paquete `clinical/pacientes`.
* **Razón:** Los datos médicos de los pacientes se gestionan mediante una app de terceros. Mantener código no utilizado (*Dead Code*) en el sistema genera deuda técnica. Nuestro enfoque se centra en la gestión empresarial (ERP/CRM B2B).

### 9.2. Separación entre Seguridad y Organización
* **Problema Común:** Sobrecargar la entidad `Usuario` con datos de negocio (direcciones, horas de contrato).
* **Solución (Arquitectura Limpia):** El paquete `security` queda ciego y aislado (solo maneja credenciales). Creamos el paquete `organization` con las entidades `Nutricionista` y `Farmacia`. Ambas se vinculan a sus credenciales mediante una relación `@OneToOne` con `Usuario`.

### 9.3. La Máquina de Estados (Módulo Consultas)
* **El Problema del "WhatsApp":** Los nutricionistas cometían errores al registrar turnos separados (mañana/tarde) y pedían correcciones manuales por chat.
* **Solución Técnica:** En el paquete `operations/consultas`, modelamos el Turno con tres estados (`EstadoConsulta` Enum):
  1. `BORRADOR`: Editable libremente por el nutricionista.
  2. `CONFIRMADA`: Bloqueada en el backend. Las horas cuentan para el resumen mensual.
  3. `CON_INCIDENCIA`: Si hay un error, el nutricionista abre incidencia, reporta el mensaje, y solo el Administrador puede editar/desbloquear.

---

## 💰 CAPÍTULO 10: Módulo Financiero (Ventas y Pedidos)

Implementamos la gestión de catálogo y ventas bajo el paquete `sales`.

### 10.1. Manejo de Dinero: La Regla de Oro (BigDecimal)
* **Decisión Técnica:** Prohibido el uso de `Double` o `Float` para precios, ya que introducen errores de precisión en coma flotante.
* **Implementación:** Utilizamos `BigDecimal` en la entidad `Producto` (para PVF, PVP e IVA al 10%) asegurando cálculos financieros exactos al céntimo.

### 10.2. Estructura de Pedidos (Cabecera y Líneas)
* Diseñamos el Pedido separando la "Cabecera" (`Pedido` con `@ManyToOne` a Farmacia y Nutricionista) de los "Detalles" (`LineaPedido`).
* Se "congela" el precio en el momento exacto de la compra guardándolo en `precioAplicado`, protegiendo el historial frente a futuros cambios de precio en el Catálogo.

### 10.3. Regla de Negocio: Umbral de Liquidación
* Implementamos una validación dura en el `PedidoService`: Un pedido en estado `PENDIENTE_LIQUIDAR` no puede pasar a `LIQUIDADO` si la suma total de las líneas (ignorando productos bonificados) es menor a **80€**. De intentarlo, el backend lanza una `IllegalStateException` abortando la transacción.

## 🌐 CAPÍTULO 11: La Conexión Total y la Batalla del CORS

Al intentar conectar nuestro Frontend autenticado (con JWT) con el Backend, nos topamos con el clásico error de desarrollo web: **CORS (Cross-Origin Resource Sharing)**.

### 11.1. El problema de la petición "Fantasma" (Preflight)
* **El Síntoma:** El login funcionaba, pero al intentar cargar el Dashboard, React lanzaba un error `ERR_CONNECTION_REFUSED` o `CORS error` en la consola.
* **El Diagnóstico:** Al añadir el Token (`Authorization: Bearer...`) a las cabeceras HTTP, el navegador (por seguridad) envía primero una petición invisible de tipo `OPTIONS` para pedir permiso al servidor. Nuestro filtro de Spring Security (`SecurityConfig.java`) estaba bloqueando esa petición `OPTIONS` porque no llevaba Token.
* **La Solución:** Unificamos la configuración CORS dentro de la cadena de Spring Security y añadimos explícitamente `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()`. Esto permitió que el "saludo" del navegador pasara y la comunicación fluyera perfectamente.

---

## 🖥️ CAPÍTULO 12: Frontend - Vistas Core y Experiencia de Usuario (UX)

Transformamos el `Dashboard.jsx` de una simple cuadrícula a un **Layout Profesional con Sidebar Dinámico**. El menú se genera al vuelo dependiendo de si el token pertenece a un Administrador, un Nutricionista o una Farmacia.

### 12.1. Vista Resumen y "Empty States"
* Creamos `VistaResumen.jsx` para mostrar los cálculos matemáticos procesados por el backend.
* **Decisión de Diseño (UX):** Implementamos un manejo de "Empty States" (Estados Vacíos). Si el usuario no tiene horas ni ventas registradas, en lugar de mostrar contadores a 0 que parezcan un error del sistema, mostramos un panel amarillo amigable explicando que los turnos en "Borrador" no computan.

### 12.2. Vista Consultas (Máquina de Estados + Modal)
* Conectamos la interfaz gráfica con nuestra máquina de estados del backend (`BORRADOR` -> `CONFIRMADA` -> `CON_INCIDENCIA`).
* **Decisión de Negocio (El Pop-Up):** Para evitar que a los nutricionistas se les olvidara confirmar los turnos, cambiamos el flujo. En lugar de guardar silenciosamente, al pulsar "Registrar" se abre un **Modal de Confirmación** flotante. Al aceptar, React envía dos peticiones asíncronas consecutivas: crea el turno y lo confirma al instante.

### 12.3. Vista Pedidos y Carrito de la Compra
* Diseñamos un módulo de ventas B2B con catálogo a la izquierda y carrito dinámico a la derecha.
* **Reglas de Negocio en Tiempo Real:** 1. El carrito diferencia entre cantidad normal (de pago) y bonificada (gratis).
  2. Implementamos un aviso visual condicional: Si el importe del carrito es `< 80€`, aparece un aviso amarillo de que no podrá liquidarse. Al superar los 80€, cambia a verde.
  3. **Auto-liquidación:** Añadimos un botón en el historial de pedidos para que el nutricionista/farmacia pueda liquidar sus propios pedidos, siempre y cuando superen el umbral de los 80€ y se encuentren en estado `PENDIENTE_LIQUIDAR`.
## 🎭 CAPÍTULO 13: Autenticación Dinámica y RBAC Avanzado

Hasta ahora, la aplicación funcionaba con "ruedines" (usábamos un `NUTRICIONISTA_ID = 1` fijo en el código para hacer pruebas). Ha llegado el momento de conectar la identidad real del usuario con sus acciones.

### 13.1. Identidad a través del Token (`/perfil/me`)

* **El Problema:** El Frontend necesita saber quién es exactamente el usuario conectado para no mostrarle datos de otros.
* **La Solución (Backend):** Creamos endpoints específicos en `NutricionistaController` y `FarmaciaController` llamados `/perfil/me`. Usando `java.security.Principal`, el backend extrae el email del token interceptado, busca el perfil exacto en la base de datos (`findByUsuarioEmail`) y lo devuelve al Frontend.
* **Seguridad:** Protegimos meticulosamente cada ruta con `@PreAuthorize("hasRole('...')")` para que una Farmacia no pueda acceder a rutas de Nutricionista (provocando los famosos errores `403 Forbidden` como medida de defensa activa).

### 13.2. Bifurcación de Vistas por Rol

Modificamos el `Dashboard.jsx` para que no solo oculte botones en el menú, sino que cambie los componentes que se renderizan.

* **Creamos VistaResumenFarmacia.jsx:** Un panel de bienvenida diseñado específicamente para el rol `FARMACIA`. Al contrario que el nutricionista (que ve gráficos de rendimiento), la farmacia ve en números gigantes su Saldo Virtual disponible y un botón directo para comprar, reduciendo la fricción (UX).

---

## 🛒 CAPÍTULO 14: La "Doble Cesta" y Lógica Financiera Compleja

El módulo de Pedidos (`VistaPedidos.jsx`) ha evolucionado para convertirse en el componente más complejo de nuestra aplicación, manejando múltiples reglas de negocio en tiempo real.

### 14.1. El Algoritmo de la Doble Cesta

* **Requisito de Negocio:** Una farmacia puede pagar un mismo pedido usando dinero real y saldo virtual (generado por las comisiones del nutricionista).
* **Solución en React:** Modificamos el carrito de compras para que cada línea de pedido tenga un flag booleano `pagadoConSaldo`. El algoritmo separa visualmente y matemáticamente el carrito en dos:
  * **Total Real:** Suma los productos normales.
  * **Total Virtual:** Suma los productos marcados para pagar con monedero.

### 14.2. Bloqueos y Umbrales en Tiempo Real

Aplicamos las mismas reglas de negocio del Backend en el Frontend para dar feedback inmediato al usuario:

* **Umbral de 80€:** Bloqueamos los botones de "Pagar con Saldo" hasta que la suma del "Total Real" alcance los 80€. Mostramos una barra de progreso visual (UX) indicando cuánto dinero real falta para desbloquear el monedero.
* **Protección de Saldo:** Evitamos que el usuario añada productos virtuales si superan el saldo que tiene disponible en ese momento.

### 14.3. Pedidos Contextuales

Hicimos que la vista de Pedidos fuera inteligente:

* Si entra una **Nutricionista**, le aparece un desplegable para elegir a qué farmacia le está haciendo el pedido.
* Si entra una **Farmacia**, el desplegable desaparece automáticamente. El sistema asume su identidad en base a su perfil y oculta la complejidad.

---

## 📅 CAPÍTULO 15: Refinamiento del Módulo de Consultas (Turnos)

Las consultas nutricionales son el núcleo de la recolección de datos, por lo que blindamos su registro y visualización.

### 15.1. Personalización de la Agenda

* Sustituimos la llamada genérica `listarTodas()` por `obtenerMisConsultas()`.
* **Beneficio:** Alivianamos la carga de la base de datos y garantizamos la privacidad. La Nutricionista "A" jamás verá el historial de turnos de la Nutricionista "B".

### 15.2. Preservación de Métricas Clave

* Mantuvimos intacto el formulario de registro que captura los KPIs vitales del negocio: **Nuevas, Revisiones, Promociones y Personal de Farmacia**.
* Implementamos una validación en React que bloquea el envío si el usuario intenta registrar un turno en una fecha futura, protegiendo la integridad temporal de la base de datos.

### 15.3. Herramientas de Productividad (Buscador y Filtros)

Enriquecimos el Historial de Turnos de la Nutricionista:

* Añadimos un **Buscador en tiempo real** (barra de búsqueda) para filtrar los turnos por el nombre de la farmacia.
* Implementamos un **Filtro de Meses dinámico**: React lee todas las fechas de los turnos descargados, extrae los meses únicos usando un `Set`, y genera un menú desplegable para aislar el historial por meses.
* Añadimos **ordenación cronológica** (Más recientes / Más antiguos).