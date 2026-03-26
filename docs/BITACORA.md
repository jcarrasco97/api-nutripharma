# 📓 Bitácora de Desarrollo: NutriPharma (MVP)

> **Propósito de este documento:**
> Esta bitácora documenta paso a paso la construcción de la arquitectura base de NutriPharma. Está escrita desde la
> perspectiva de un desarrollador en formación con el apoyo de mentoría Senior. Su objetivo es explicar el *porqué* de
> cada pieza de código, las decisiones de negocio y permitir reconstruir el proyecto desde cero si fuera necesario.

---

## 🏗️ CAPÍTULO 1: Origen, Negocio y Filosofía del Proyecto

### 1.1. De un Sistema Legacy a un MVP Moderno

NutriPharma no nace de la nada. El proyecto se basa en un código y un modelo de negocio anterior. Para entender las
reglas del juego (entidades, consultas nutricionales, alertas de grasa, cálculo de IMC), **hicimos ingeniería inversa
analizando las relaciones de las tablas SQL del sistema antiguo.** Sin embargo, adoptamos una mentalidad de **Desarrollo
Iterativo (Producto Mínimo Viable - MVP)**:

* No copiamos el sistema antiguo a ciegas. Usamos el SQL como mapa mental inicial para construir nuestra API.
* Sabemos que en el futuro modificaremos partes del negocio, añadiremos campos (ej. alergias) y borraremos entidades que
  ya no aporten valor.
* Hemos creado un "chasis" sólido sobre el cual podemos construir y deshacer sin que la aplicación se rompa.

### 1.2. Arquitectura Desacoplada (Frontend vs Backend)

En lugar de crear una aplicación monolítica, optamos por separar las responsabilidades:

* **Backend (API REST en Java/Spring Boot):** Es el "motor" y el "cerebro". Protege la base de datos, realiza los
  cálculos médicos y valida la seguridad.
* **Frontend (React/Vite):** Es la "carrocería". Consume la API y se encarga única y exclusivamente de la experiencia
  visual del usuario (UI/UX).
* **El Beneficio:** Si el negocio evoluciona y necesitamos una App Móvil, el Backend no se toca; solo construimos otra
  interfaz que consuma los mismos datos.

---

## 🛠️ CAPÍTULO 2: Entorno de Trabajo y Herramientas

En el desarrollo profesional, usar la herramienta adecuada para el lenguaje adecuado dispara la productividad. Decidimos
usar dos IDEs distintos:

### 2.1. Backend: IntelliJ IDEA Community

* Lo usamos exclusivamente para Java.
* Su motor de indexación, autocompletado y refactorización para lenguajes fuertemente tipados como Java no tiene rival.
  Nos asegura que las Entidades y las relaciones de base de datos estén robustas.

### 2.2. Frontend: Visual Studio Code (VS Code)

* Lo usamos exclusivamente para JavaScript/React. Es el rey indiscutible del Frontend por su ligereza.
* **Configuración Clave Aplicada:**
    * Instalamos extensiones vitales: *ES7+ React/Redux snippets* (para crear componentes en 1 segundo con `rfce`),
      *Tailwind CSS IntelliSense* (para autocompletado de clases) y *Prettier / ESLint* (para formateo automático y
      detección de errores).
    * Activamos el "Format on Save" para que el código se ordene automáticamente al pulsar `Ctrl + S`, liberando carga
      mental.

---

## ⚙️ CAPÍTULO 3: Configuración del Frontend desde Cero

### 3.1. Inicialización con Vite

Descartamos herramientas antiguas y pesadas en favor de Vite.

* **Comando:** `npm create vite@latest nutripharma-ui -- --template react`
* **Por qué:** Arranca el servidor local (`npm run dev`) en milisegundos y actualiza el navegador en tiempo real sin
  recargar la página entera (HMR).
* **Limpieza:** Borramos `App.css` y limpiamos `App.jsx` para tener un lienzo en blanco, evitando arrastrar estilos por
  defecto que interfieran con nuestro diseño.

### 3.2. La Odisea de Tailwind CSS v4

Instalamos Tailwind para manejar los estilos CSS mediante clases utilitarias (`className="bg-sky-500"`), lo que acelera
el diseño enormemente.

* Nos enfrentamos a un error de configuración porque **Tailwind v4** (la versión más vanguardista) cambió su forma de
  conectarse con PostCSS.
* **La Solución:** Instalamos el paquete unificador (`npm install @tailwindcss/postcss`) y actualizamos
  `postcss.config.js` explícitamente con `"@tailwindcss/postcss": {}`.
* En `index.css`, bastó con poner un limpio `@import "tailwindcss";`.

### 3.3. Ecosistema de Librerías

* **Lucide React:** Para iconos modernos, vectoriales y estilizables directamente con las clases de Tailwind.
* **React Router Dom:** Para la navegación interna. Convierte nuestra web en una SPA (Single Page Application) donde las
  pantallas cambian sin parpadeos.
* **Axios:** El cliente HTTP. Lo preferimos sobre el `fetch` nativo porque transforma los JSON automáticamente y es más
  fácil de configurar para enviar el Token de seguridad en el futuro.

---

## 🔌 CAPÍTULO 4: El Puente entre React y Spring Boot

Para que ambas aplicaciones se hablaran, tuvimos que configurar la red y la seguridad inicial.

### 4.1. Habilitando CORS en Java

Por defecto, los navegadores bloquean peticiones entre distintos puertos por seguridad.

* React corre en `http://localhost:5173` y Spring Boot en el `8080`.
* Creamos una clase `CorsConfig` en Spring Boot para indicarle explícitamente que las peticiones que lleguen desde el
  puerto 5173 son seguras y permitidas.

### 4.2. DTOs Modernos con Records

En el `AuthController` de Java, en lugar de crear clases largas con Getters y Setters, usamos **Records** (ej.
`record LoginRequest(String username, String password) {}`). Es la forma más limpia en Java moderno de transportar datos
inmutables desde React hasta el servidor.

### 4.3. El Mock de Seguridad (Prueba de Concepto)

Como aún no tenemos la base de datos conectada a la seguridad, aplicamos el principio de "Divide y Vencerás".

* Creamos un "Simulacro" en el `AuthService` de Java. Si React enviaba el usuario `admin` y la contraseña `1234`, Java
  devolvía un Token de éxito simulado.
* **El objetivo cumplido:** Esto nos permitió confirmar que la comunicación HTTP y la arquitectura de red funcionan
  perfectamente, aislando la complejidad de la base de datos para una fase posterior.

---

## 🖥️ CAPÍTULO 5: Desarrollo de Componentes y Lógica Visual

### 5.1. El Servicio Base (`authService.js`)

* Separamos las peticiones HTTP de la interfaz visual creando la carpeta `services/`.
* Aquí configuramos Axios y la URL base de la API (`http://localhost:8080/api`). Si el servidor cambia en producción,
  solo hay que tocar esta línea.

### 5.2. El Componente de Login (`Login.jsx`)

Construimos un panel de acceso profesional y seguro:

1. **Estado Controlado:** Usamos `useState` para guardar lo que el usuario teclea en tiempo real.
2. **Manejo de Errores y Carga:** Implementamos estados de `error` y `cargando`. Al enviar la petición, los inputs y el
   botón se bloquean (`disabled`) para evitar doble envíos, y se muestra un icono giratorio de carga.
3. **Captura del Token:** Tras recibir el JSON del backend, guardamos el Token en el `localStorage` del navegador.

### 5.3. Sistema de Rutas (`App.jsx` y `Dashboard.jsx`)

* Creamos un componente básico `Dashboard.jsx` como panel principal, incluyendo un botón de "Cerrar Sesión" que borra el
  Token del LocalStorage.
* En `App.jsx`, envolvimos la aplicación en un `<BrowserRouter>` y definimos dos "carreteras": la `/` para el Login y la
  `/dashboard` para el panel.
* En el Login, usamos el hook `useNavigate` para teletransportar al usuario automáticamente al Dashboard justo después
  de validar las credenciales con éxito.

---

## 🛡️ CAPÍTULO 6: Control de Versiones (Buenas Prácticas)

En lugar de mezclar todo en un solo lugar, mantuvimos la "higiene" del código:

* Se inicializaron repositorios de Git independientes para el Backend (`api_nutripharma`) y el Frontend (
  `nutripharma-ui`).
* **Protección de dependencias:** Nos aseguramos de que el archivo `.gitignore` del frontend excluyera la carpeta
  `node_modules` para no subir gigas de dependencias descargables al servidor de GitHub.
* Hicimos commits atómicos y subimos el proyecto a un repositorio privado en GitHub como copia de seguridad y
  preparación para despliegues futuros.

---

## 🔐 CAPÍTULO 7: Seguridad Real y Bóveda del Backend

Al avanzar el MVP, reemplazamos el "simulacro" por un sistema de seguridad criptográfico real conectado a base de datos.

### 7.1. Blindaje en React (Route Guards)

Para evitar que un usuario sin loguearse accediera tipeando la URL, creamos `ProtectedRoute.jsx`. Este componente
envuelve al `Dashboard` y verifica la existencia del Token. Si no hay token, usa `<Navigate to="/" replace />` para
expulsar al usuario automáticamente.

### 7.2. Configuración de Entidades (Spring Security)

* Habilitamos que nuestra entidad `Usuario` implementara la interfaz `UserDetails`, convirtiéndola en el estándar que
  Spring Security necesita para autenticar.
* Creamos la entidad `Rol` y aplicamos una relación `@ManyToMany` para establecer el Control de Acceso Basado en Roles (
  RBAC).

### 7.3. Configuración del Ecosistema de Seguridad

Solucionamos varios retos arquitectónicos de inyección de dependencias (`UnsatisfiedDependencyException`):

1. **`ApplicationConfig.java`**: Creamos la fábrica de Beans donde definimos el uso de `BCryptPasswordEncoder` y el
   `DaoAuthenticationProvider`. Adaptamos el código a **Spring Boot 4.0 / Java 25**, pasando el `UserDetailsService`
   directamente en el constructor del proveedor.
2. **`JwtAuthenticationFilter.java`**: Creamos un filtro `OncePerRequestFilter` que intercepta todas las peticiones
   HTTP, extrae el token del Header `Authorization: Bearer`, lo valida y registra al usuario en el
   `SecurityContextHolder`.
3. **`SecurityConfig.java`**: Configuramos las reglas globales, desactivamos CSRF (innecesario con JWT), hicimos la ruta
   de login pública y el resto de la aplicación privada y *stateless* (sin sesión en memoria).

### 7.4. Data Seeding y Decisión de Negocio (Registro Cerrado)

Decidimos utilizar un **Modelo de Registro Cerrado** (los Nutricionistas y Farmacias no se registran solos, el Admin
crea sus cuentas).
Para el entorno de desarrollo, creamos `DataSeeder.java` (usando `CommandLineRunner`) para inyectar automáticamente al
usuario `admin@nutripharma.com` con sus roles correspondientes (`ROLE_ADMIN`, `ROLE_NUTRICIONISTA`) cada vez que arranca
la base de datos.

---

## 🎨 CAPÍTULO 8: Frontend Dinámico (RBAC en React)

Con el backend devolviendo un Token JWT real, preparamos la interfaz para reaccionar a los roles del usuario.

### 8.1. Decodificación del Token (`jwt-decode`)

Instalamos la librería `jwt-decode` para leer el contenido (Payload) del JWT directamente en el navegador, sin necesidad
de hacer peticiones extra al backend.

### 8.2. El Dashboard Dinámico y "Lazy State Initialization"

Diseñamos un Dashboard inteligente que muestra u oculta módulos (Validaciones, Calendario Global, Mis Consultas,
Realizar Pedido) dependiendo de si el usuario es Admin, Nutricionista o Farmacia.

**Reto Técnico (Cascading Renders):**

* ESLint nos advirtió sobre el error de hacer un `setState` síncrono dentro de un `useEffect`, lo que causaba que React
  renderizara la pantalla dos veces (ineficiente).
* **Solución Senior:** Aplicamos **"Lazy State Initialization"** (Inicialización Perezosa). Pasamos una función
  directamente al `useState(() => { ... })` para decodificar el token en el instante exacto en que nace el componente,
  logrando un código limpio, rápido y sin warnings de ESLint.

---

## 🏗️ CAPÍTULO 9: Domain-Driven Design (DDD) y Reestructuración

Para asegurar la escalabilidad del proyecto, realizamos una profunda refactorización guiada por el **Lenguaje Ubicuo (
Ubiquitous Language)** del negocio.

### 9.1. Eliminación de Código Muerto

* **Decisión:** Eliminamos el paquete `clinical/pacientes`.
* **Razón:** Los datos médicos de los pacientes se gestionan mediante una app de terceros. Mantener código no
  utilizado (*Dead Code*) en el sistema genera deuda técnica. Nuestro enfoque se centra en la gestión empresarial (
  ERP/CRM B2B).

### 9.2. Separación entre Seguridad y Organización

* **Problema Común:** Sobrecargar la entidad `Usuario` con datos de negocio (direcciones, horas de contrato).
* **Solución (Arquitectura Limpia):** El paquete `security` queda ciego y aislado (solo maneja credenciales). Creamos el
  paquete `organization` con las entidades `Nutricionista` y `Farmacia`. Ambas se vinculan a sus credenciales mediante
  una relación `@OneToOne` con `Usuario`.

### 9.3. La Máquina de Estados (Módulo Consultas)

* **El Problema del "WhatsApp":** Los nutricionistas cometían errores al registrar turnos separados (mañana/tarde) y
  pedían correcciones manuales por chat.
* **Solución Técnica:** En el paquete `operations/consultas`, modelamos el Turno con tres estados (`EstadoConsulta`
  Enum):
    1. `BORRADOR`: Editable libremente por el nutricionista.
    2. `CONFIRMADA`: Bloqueada en el backend. Las horas cuentan para el resumen mensual.
    3. `CON_INCIDENCIA`: Si hay un error, el nutricionista abre incidencia, reporta el mensaje, y solo el Administrador
       puede editar/desbloquear.

---

## 💰 CAPÍTULO 10: Módulo Financiero (Ventas y Pedidos)

Implementamos la gestión de catálogo y ventas bajo el paquete `sales`.

### 10.1. Manejo de Dinero: La Regla de Oro (BigDecimal)

* **Decisión Técnica:** Prohibido el uso de `Double` o `Float` para precios, ya que introducen errores de precisión en
  coma flotante.
* **Implementación:** Utilizamos `BigDecimal` en la entidad `Producto` (para PVF, PVP e IVA al 10%) asegurando cálculos
  financieros exactos al céntimo.

### 10.2. Estructura de Pedidos (Cabecera y Líneas)

* Diseñamos el Pedido separando la "Cabecera" (`Pedido` con `@ManyToOne` a Farmacia y Nutricionista) de los "Detalles" (
  `LineaPedido`).
* Se "congela" el precio en el momento exacto de la compra guardándolo en `precioAplicado`, protegiendo el historial
  frente a futuros cambios de precio en el Catálogo.

### 10.3. Regla de Negocio: Umbral de Liquidación

* Implementamos una validación dura en el `PedidoService`: Un pedido en estado `PENDIENTE_LIQUIDAR` no puede pasar a
  `LIQUIDADO` si la suma total de las líneas (ignorando productos bonificados) es menor a **80€**. De intentarlo, el
  backend lanza una `IllegalStateException` abortando la transacción.

## 🌐 CAPÍTULO 11: La Conexión Total y la Batalla del CORS

Al intentar conectar nuestro Frontend autenticado (con JWT) con el Backend, nos topamos con el clásico error de
desarrollo web: **CORS (Cross-Origin Resource Sharing)**.

### 11.1. El problema de la petición "Fantasma" (Preflight)

* **El Síntoma:** El login funcionaba, pero al intentar cargar el Dashboard, React lanzaba un error
  `ERR_CONNECTION_REFUSED` o `CORS error` en la consola.
* **El Diagnóstico:** Al añadir el Token (`Authorization: Bearer...`) a las cabeceras HTTP, el navegador (por seguridad)
  envía primero una petición invisible de tipo `OPTIONS` para pedir permiso al servidor. Nuestro filtro de Spring
  Security (`SecurityConfig.java`) estaba bloqueando esa petición `OPTIONS` porque no llevaba Token.
* **La Solución:** Unificamos la configuración CORS dentro de la cadena de Spring Security y añadimos explícitamente
  `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()`. Esto permitió que el "saludo" del navegador pasara y la
  comunicación fluyera perfectamente.

---

## 🖥️ CAPÍTULO 12: Frontend - Vistas Core y Experiencia de Usuario (UX)

Transformamos el `Dashboard.jsx` de una simple cuadrícula a un **Layout Profesional con Sidebar Dinámico**. El menú se
genera al vuelo dependiendo de si el token pertenece a un Administrador, un Nutricionista o una Farmacia.

### 12.1. Vista Resumen y "Empty States"

* Creamos `VistaResumen.jsx` para mostrar los cálculos matemáticos procesados por el backend.
* **Decisión de Diseño (UX):** Implementamos un manejo de "Empty States" (Estados Vacíos). Si el usuario no tiene horas
  ni ventas registradas, en lugar de mostrar contadores a 0 que parezcan un error del sistema, mostramos un panel
  amarillo amigable explicando que los turnos en "Borrador" no computan.

### 12.2. Vista Consultas (Máquina de Estados + Modal)

* Conectamos la interfaz gráfica con nuestra máquina de estados del backend (`BORRADOR` -> `CONFIRMADA` ->
  `CON_INCIDENCIA`).
* **Decisión de Negocio (El Pop-Up):** Para evitar que a los nutricionistas se les olvidara confirmar los turnos,
  cambiamos el flujo. En lugar de guardar silenciosamente, al pulsar "Registrar" se abre un **Modal de Confirmación**
  flotante. Al aceptar, React envía dos peticiones asíncronas consecutivas: crea el turno y lo confirma al instante.

### 12.3. Vista Pedidos y Carrito de la Compra

* Diseñamos un módulo de ventas B2B con catálogo a la izquierda y carrito dinámico a la derecha.
* **Reglas de Negocio en Tiempo Real:** 1. El carrito diferencia entre cantidad normal (de pago) y bonificada (gratis).
    2. Implementamos un aviso visual condicional: Si el importe del carrito es `< 80€`, aparece un aviso amarillo de que
       no podrá liquidarse. Al superar los 80€, cambia a verde.
    3. **Auto-liquidación:** Añadimos un botón en el historial de pedidos para que el nutricionista/farmacia pueda
       liquidar sus propios pedidos, siempre y cuando superen el umbral de los 80€ y se encuentren en estado
       `PENDIENTE_LIQUIDAR`.

## 🎭 CAPÍTULO 13: Autenticación Dinámica y RBAC Avanzado

Hasta ahora, la aplicación funcionaba con "ruedines" (usábamos un `NUTRICIONISTA_ID = 1` fijo en el código para hacer
pruebas). Ha llegado el momento de conectar la identidad real del usuario con sus acciones.

### 13.1. Identidad a través del Token (`/perfil/me`)

* **El Problema:** El Frontend necesita saber quién es exactamente el usuario conectado para no mostrarle datos de
  otros.
* **La Solución (Backend):** Creamos endpoints específicos en `NutricionistaController` y `FarmaciaController` llamados
  `/perfil/me`. Usando `java.security.Principal`, el backend extrae el email del token interceptado, busca el perfil
  exacto en la base de datos (`findByUsuarioEmail`) y lo devuelve al Frontend.
* **Seguridad:** Protegimos meticulosamente cada ruta con `@PreAuthorize("hasRole('...')")` para que una Farmacia no
  pueda acceder a rutas de Nutricionista (provocando los famosos errores `403 Forbidden` como medida de defensa activa).

### 13.2. Bifurcación de Vistas por Rol

Modificamos el `Dashboard.jsx` para que no solo oculte botones en el menú, sino que cambie los componentes que se
renderizan.

* **Creamos VistaResumenFarmacia.jsx:** Un panel de bienvenida diseñado específicamente para el rol `FARMACIA`. Al
  contrario que el nutricionista (que ve gráficos de rendimiento), la farmacia ve en números gigantes su Saldo Virtual
  disponible y un botón directo para comprar, reduciendo la fricción (UX).

---

## 🛒 CAPÍTULO 14: La "Doble Cesta" y Lógica Financiera Compleja

El módulo de Pedidos (`VistaPedidos.jsx`) ha evolucionado para convertirse en el componente más complejo de nuestra
aplicación, manejando múltiples reglas de negocio en tiempo real.

### 14.1. El Algoritmo de la Doble Cesta

* **Requisito de Negocio:** Una farmacia puede pagar un mismo pedido usando dinero real y saldo virtual (generado por
  las comisiones del nutricionista).
* **Solución en React:** Modificamos el carrito de compras para que cada línea de pedido tenga un flag booleano
  `pagadoConSaldo`. El algoritmo separa visualmente y matemáticamente el carrito en dos:
    * **Total Real:** Suma los productos normales.
    * **Total Virtual:** Suma los productos marcados para pagar con monedero.

### 14.2. Bloqueos y Umbrales en Tiempo Real

Aplicamos las mismas reglas de negocio del Backend en el Frontend para dar feedback inmediato al usuario:

* **Umbral de 80€:** Bloqueamos los botones de "Pagar con Saldo" hasta que la suma del "Total Real" alcance los 80€.
  Mostramos una barra de progreso visual (UX) indicando cuánto dinero real falta para desbloquear el monedero.
* **Protección de Saldo:** Evitamos que el usuario añada productos virtuales si superan el saldo que tiene disponible en
  ese momento.

### 14.3. Pedidos Contextuales

Hicimos que la vista de Pedidos fuera inteligente:

* Si entra una **Nutricionista**, le aparece un desplegable para elegir a qué farmacia le está haciendo el pedido.
* Si entra una **Farmacia**, el desplegable desaparece automáticamente. El sistema asume su identidad en base a su
  perfil y oculta la complejidad.

---

## 📅 CAPÍTULO 15: Refinamiento del Módulo de Consultas (Turnos)

Las consultas nutricionales son el núcleo de la recolección de datos, por lo que blindamos su registro y visualización.

### 15.1. Personalización de la Agenda

* Sustituimos la llamada genérica `listarTodas()` por `obtenerMisConsultas()`.
* **Beneficio:** Alivianamos la carga de la base de datos y garantizamos la privacidad. La Nutricionista "A" jamás verá
  el historial de turnos de la Nutricionista "B".

### 15.2. Preservación de Métricas Clave

* Mantuvimos intacto el formulario de registro que captura los KPIs vitales del negocio: **Nuevas, Revisiones,
  Promociones y Personal de Farmacia**.
* Implementamos una validación en React que bloquea el envío si el usuario intenta registrar un turno en una fecha
  futura, protegiendo la integridad temporal de la base de datos.

### 15.3. Herramientas de Productividad (Buscador y Filtros)

Enriquecimos el Historial de Turnos de la Nutricionista:

* Añadimos un **Buscador en tiempo real** (barra de búsqueda) para filtrar los turnos por el nombre de la farmacia.
* Implementamos un **Filtro de Meses dinámico**: React lee todas las fechas de los turnos descargados, extrae los meses
  únicos usando un `Set`, y genera un menú desplegable para aislar el historial por meses.
* Añadimos **ordenación cronológica** (Más recientes / Más antiguos).
* ## ☁️ CAPÍTULO 16: Integración con la Nube (Google Drive API)

Para evitar saturar nuestro servidor con archivos físicos (PDFs, imágenes), decidimos delegar el almacenamiento en *
*Google Drive**, utilizando nuestro servidor como un "puente seguro".

### 16.1. El Puente OAuth2 y el Refresh Token

- **Desafío:** Las APIs de Google requieren tokens que caducan cada hora. No podemos pedirle al Admin que se loguee en
  Google cada vez que quiera subir un archivo.
- **Solución:** Implementamos un flujo de **Refresh Token**. Configuramos el proyecto en Google Cloud Console, obtuvimos
  las credenciales y usamos el *OAuth2 Playground* para generar una "llave maestra" eterna.
- **Decisión Técnica:** Pasamos la aplicación a estado de **"Producción"** en Google Cloud para evitar que el token
  caducara a los 7 días (restricción de las apps en modo testing).

### 16.2. GoogleDriveService: El Gestor de Bytes

- Creamos un servicio especializado que utiliza `GoogleNetHttpTransport` y `GsonFactory`.
- El servicio no guarda archivos en el disco duro del servidor; recibe el `MultipartFile` de React, lo sube directamente
  a una carpeta específica de Drive y nos devuelve un **ID único**. Este ID es lo único que guardamos en nuestra base de
  datos MySQL.

---

## 🗄️ CAPÍTULO 17: Refinamiento de Datos y Selección de Usuarios

### 17.1. La Limpieza del Esquema (Drop & Recreate)

- **Problema:** Al simplificar el modelo de documentos (eliminando campos como título y descripción), Spring Boot no
  borraba las columnas antiguas de MySQL por seguridad, causando errores de integridad (404/500).
- **Aprendizaje:** En fase de desarrollo, la forma más sana de sincronizar cambios estructurales es ejecutar un **DROP
  TABLE** y dejar que Hibernate genere la tabla de nuevo desde cero, asegurando que los campos coincidan al 100% con el
  código Java.

### 17.2. Consultas Nativas y Proyecciones

- **El Reto del Desplegable:** Necesitábamos que el Admin pudiera elegir a qué usuario enviar un documento privado, pero
  los nombres están repartidos en tres tablas (`usuarios`, `nutricionistas`, `farmacias`).
- **Solución SQL:** Implementamos una **Native Query** en el repositorio usando la función `COALESCE`. Esta función
  intenta buscar el nombre en la tabla de nutricionistas, si no existe busca en la de farmacias, y si no, devuelve un
  valor por defecto. Usamos una **Proyección (Interface)** para mapear este resultado mixto de forma elegante en Java.

---

## 📈 CAPÍTULO 18: Historial y Analítica para Farmacias

Decidimos dotar a la Farmacia de una herramienta de transparencia para que pueda auditar el trabajo de los
nutricionistas en su local.

### 18.1. El Endpoint de Historial Contextual

- Creamos una ruta `/api/consultas/historial-farmacia` protegida.
- El servidor utiliza el **Principal** (el usuario autenticado) para filtrar automáticamente las consultas. La farmacia
  solo recibe datos de su propio local, garantizando la privacidad entre establecimientos.

### 18.2. Cálculo de Comisiones en Tiempo Real

- **Regla de Negocio:** La farmacia se queda con el **30%** de lo generado en cada jornada (**25€** por consulta nueva,
  **20€** por revisión).
- **Implementación UX:** En el Frontend, la `VistaHistorialFarmacia.jsx` calcula y muestra este beneficio en cada
  tarjeta. Esto refuerza el valor del servicio de NutriPharma ante el dueño de la farmacia.

---

## 🛡️ CAPÍTULO 19: Triage de Seguridad (La Batalla del 403)

Durante el desarrollo del módulo de borrado, nos enfrentamos a bloqueos de seguridad que nos obligaron a profundizar en
la configuración de Spring Security.

### 19.1. CORS para Métodos Peligrosos

- **Síntoma:** El `GET` y el `POST` funcionaban, pero el `DELETE` devolvía un **403 Forbidden**.
- **Causa:** Nuestra configuración de CORS no incluía explícitamente el método `DELETE`. Los navegadores modernos
  bloquean cualquier intento de borrado desde un origen distinto si no hay un permiso explícito en las cabeceras.
- **Solución:** Actualizamos la configuración global de CORS para permitir la lista completa:
  `GET, POST, PUT, DELETE, OPTIONS`.

### 19.2. Borrado Atómico (Nube + Local)

- Implementamos un flujo de borrado en dos pasos: primero ordenamos a la API de Google eliminar el archivo físico y,
  solo si eso tiene éxito, borramos el registro de la base de datos local. Esto evita tener "archivos huérfanos" en el
  Drive que consuman espacio inútilmente.

---

## 🎯 CAPÍTULO 20: Pivotaje Estratégico del Rol Administrador

Tras analizar el flujo de Paco (el jefe), detectamos que el rol Admin estaba saturado de funciones operativas. Decidimos
separar el **Modo Nutricionista** (pasar consulta) del **Modo Gerente** (gestionar la empresa).

### 20.1. El Nuevo Dashboard Administrativo

Redefinimos las prioridades de la vista Admin:

- **Resumen Gráfico:** Implementamos la necesidad de una analítica visual de facturación global mes a mes.
- **Calendario Operativo:** Un centro visual para monitorizar todas las consultas y pedidos de la red de nutricionistas
  a través de una interfaz de calendario interactiva con pop-ups de información.
- **Flujo de Validación (The Gatekeeper):** Introdujimos un estado intermedio de validación. Los pedidos y consultas no
  afectan al saldo financiero ni a los objetivos hasta que el Admin los valida manualmente, actuando como un filtro de
  calidad y control de fraude.

---

> 💡 **Lección del día:**
> La arquitectura no es algo estático; evoluciona con el uso. Pasar de una app "que hace cosas" a una app "que gestiona
> un negocio" requiere saber cuándo separar las herramientas de trabajo de las herramientas de control.

## 🛡️ CAPÍTULO 21: El "Gatekeeper" Definitivo y la Lógica Front-End

Tras definir el flujo de validación, nos dimos cuenta de que tener al administrador saltando entre múltiples pantallas
para aprobar el trabajo diario era ineficiente. Decidimos unificar el control.

### 21.1. Centro de Validaciones (VistaValidaciones.jsx)

* Creamos una "Bandeja de Entrada" centralizada con sistema de pestañas (Tabs) para gestionar **Consultas, Pedidos y
  Suministros**.
* **Separación de Estados:** La pantalla se divide dinámicamente en lo que "Requiere Atención" (Bandeja de pendientes) y
  el "Historial" (Auditoría de lo ya procesado).
* **El Pop-up de Auditoría:** En lugar de saturar la tabla del historial con columnas infinitas, limpiamos la interfaz e
  implementamos un Modal Universal. Al pulsar "Detalles", se abre un informe completo cuyo diseño cambia adaptándose al
  tipo de entidad seleccionada.

### 21.2. Inteligencia en el Frontend (Agrupación y Matemáticas)

Para el desglose de los Pedidos, el Backend nos devolvía una lista plana de líneas de compra. En lugar de sobrecargar el
servidor con cálculos de visualización, le dimos inteligencia a React:

* Creamos la función `agruparLineasPorProducto()` que unifica productos con el mismo nombre y desglosa automáticamente
  si fueron pagados con **Dinero Real, Saldo Virtual o si son Bonificados (Gratis)**.
* **Lección de Arquitectura:** El backend entrega los datos crudos y exactos; el frontend se encarga de "masticarlos" y
  agruparlos para la experiencia del usuario (UX).

### 21.3. Blindaje contra la "Pantalla Blanca de la Muerte"

* Nos enfrentamos a caídas de React al intentar renderizar listas vacías o datos corruptos provenientes de pruebas
  antiguas en la base de datos.
* **Solución (Defensive Programming):** Implementamos *Optional Chaining* (`?.`) en todos los mapeos de arrays (ej.
  `materiales?.map(...)`) y asignamos parámetros por defecto `(lineas = [])`. Así, si el backend falla o envía un
  `null`, React simplemente pinta un bloque vacío en lugar de colapsar la aplicación entera.

---

## ⚙️ CAPÍTULO 22: Panel de Administración y Operaciones CRUD Completas

Para gestionar la plataforma en producción, el Administrador necesitaba poder corregir errores humanos: editar nombres,
corregir precios o dar de baja entidades.

### 22.1. VistaAdministracion.jsx (Evolución de VistaEmpleados)

* Transformamos el antiguo formulario de registro en un Panel de Administración completo dividido en pestañas:
  Nutricionistas, Farmacias y Catálogo de Productos.
* Integramos la tabla de visualización con botones flotantes de edición y borrado que aparecen suavemente al pasar el
  ratón (Hover effects en Tailwind).

### 22.2. Completando el CRUD en el Backend (Capa DTO y Controladores)

Hasta ahora, nuestra API solo permitía Crear (POST) y Leer (GET). Abrimos las puertas a la Actualización (PUT) y
Borrado (DELETE):

* **Nuevos DTOs (`*UpdateRequest`):** En lugar de reutilizar el objeto de creación, creamos *Records* específicos para
  la actualización. Esto es vital por seguridad: evitamos que un administrador pueda sobrescribir accidentalmente la
  contraseña o el saldo virtual de una farmacia al editar su dirección.
* **Borrado Atómico:** Implementamos la lógica de eliminación en cascada de forma manual en los servicios. Al borrar una
  Farmacia o Nutricionista, el backend elimina primero su perfil laboral y luego destruuye sus credenciales de acceso en
  la tabla `usuarios`.
* **Escudo de Integridad:** Nos apoyamos en la base de datos relacional. Si se intenta borrar un Producto que ya está
  presente en una línea de pedido histórico, MySQL bloquea la transacción (por clave foránea) y el Frontend captura el
  error elegantemente, avisando al usuario de que la entidad tiene datos asociados y no puede ser borrada.

---

## 📊 CAPÍTULO 23: Formateo de Datos y Detalles UX

### 23.1. Recharts y la manipulación del Tooltip

* En el Dashboard General, la gráfica de facturación mostraba números "crudos" al pasar el ratón, lo cual carecía de
  contexto financiero.
* **Solución:** Descubrimos y utilizamos la propiedad `formatter` del componente `<Tooltip />` de Recharts. Pasando una
  *Arrow Function* `(value) => [\`${value} €\`]`, logramos interceptar el dato de React antes de dibujarlo e inyectarle
  el símbolo de la moneda, mejorando drásticamente la percepción del usuario final sin alterar la base de datos.

> 💡 **Lección del día:**
> Una buena interfaz de usuario (UI) perdona los errores del servidor, protege al usuario de acciones destructivas y da
> contexto visual a datos que, de otro modo, serían simples números en una base de datos.

## 🔐 CAPÍTULO 24: Experiencia de Usuario (UX) y Seguridad Avanzada en Autenticación

En un entorno B2B, la gestión de contraseñas olvidadas no puede depender exclusivamente del Administrador (cuello de
botella). Decidimos implementar un flujo de **Autoservicio Seguro**, pero con trucos de desarrollo para no bloquearnos
configurando servidores de correo reales.

### 24.1. El Híbrido de Desarrollo (Consola + Frontend)

* **Backend:** Creamos los endpoints `/forgot-password` y `/reset-password`. El sistema genera un Token (UUID) con 15
  minutos de caducidad y lo asocia al usuario. En lugar de enviar un email real, el backend imprime el enlace de
  recuperación estructurado directamente en la consola del servidor (IntelliJ), permitiéndonos hacer clic y probar la
  interfaz en el acto.
* **Preparación para Producción:** Cuando la app se despliegue, solo habrá que sustituir ese `System.out.println` por el
  servicio `JavaMailSender` de Spring Boot. El resto del flujo es 100% real.

### 24.2. Blindaje Visual y Funcional (React)

Para la pantalla de Nueva Contraseña (`ResetPassword.jsx`), aplicamos prácticas Senior de UX/UI:

1. **Doble Check y Anti-Pegado:** Obligamos al usuario a teclear la contraseña dos veces y validamos que coincidan.
   Además, usando `onPaste={(e) => e.preventDefault()}`, bloqueamos el portapapeles en el campo de confirmación para
   forzar la escritura consciente.
2. **El "Ojo" Visor:** Implementamos los iconos `Eye` y `EyeOff` (cambiando dinámicamente el `type` del input entre
   `"password"` y `"text"`) para que el usuario pueda verificar lo que ha escrito antes de enviarlo.
3. **Redirección con Destrucción de Historial:** Al cambiar la contraseña con éxito, no dejamos al usuario en la misma
   pantalla (lo que podría causar reenvíos accidentales si recarga la página). Usamos
   `Maps("/", { replace: true, state: { resetExitoso: true } })`. Esto teletransporta al usuario al Login, **borra la
   pantalla de reseteo del historial del navegador** (el botón "Atrás" ya no funciona) y pasa un estado invisible para
   mostrar un mensaje verde de éxito en el login.

---

## 🏗️ CAPÍTULO 25: Escalabilidad Frontend (Feature-Based Architecture)

A medida que el MVP superó las 10 vistas y múltiples servicios, la estructura "plana" tradicional (agrupar todos los
servicios en una carpeta y todas las vistas en otra) comenzó a generar fricción cognitiva.

### 25.1. Alineando el Frontend con el DDD del Backend

Decidimos abandonar la organización por "Tipo de Archivo" para adoptar una **Feature-Based Architecture** (Arquitectura
basada en Funcionalidades), fuertemente inspirada en el *Feature-Sliced Design*. Organizamos el código de React por su *
*dominio de negocio**.

### 25.2. La Nueva Estructura

Migramos el código a dos grandes bloques:

* **`/core` (Transversal):** Elementos genéricos que afectan a toda la aplicación, independientemente de la
  funcionalidad. Aquí ubicamos el enrutador de seguridad (`ProtectedRoute.jsx` en `/core/routes/`) y preparamos
  `/core/components/` para futuros botones o modales reutilizables.
* **`/features` (Dominios de Negocio):** Agrupamos vistas y servicios bajo su contexto.
    * `/auth` (Login, ResetPassword, authService)
    * `/dashboard` (Dashboard principal y los Resúmenes según el rol)
    * `/consultas`, `/pedidos`, `/suministros`, `/documentacion` y `/admin`.

### 25.3. El Beneficio Inmediato

Si en el futuro hay que modificar el cálculo de un carrito de compras, el desarrollador entra a `src/features/pedidos/`.
Allí tiene su `VistaPedidos.jsx` y su `pedidosService.js` uno al lado del otro. Esta alta cohesión modular reduce el
tiempo de búsqueda de archivos, facilita el mantenimiento y prepara el terreno por si en el futuro se quiere extraer un
módulo entero hacia otra aplicación.

## [23/03/2026] - Sistema de Kilometraje y Arquitectura Multicapa de Comisiones

### 🏗️ Capítulo 26: Decisiones Arquitectónicas

* **Separation of Concerns (SoC) en Pedidos:** Se ha decidido por lógica de negocio que las Farmacias no deben gestionar
  el reparto interno de comisiones de las nutricionistas al realizar un pedido. Esta responsabilidad se ha trasladado en
  exclusiva al Administrador (Centro de Validaciones), simplificando la UX del cliente.

### ⚙️ Backend (Spring Boot)

* **Refactorización Entidad `Pedido`:** Eliminada la relación 1:1 con `Nutricionista` (columna `nutricionista_id`).
* **Nueva Entidad `RepartoPedido`:** Creada tabla intermedia `pedido_reparto` para soportar el modelo multicapa,
  permitiendo que múltiples nutricionistas compartan porcentajes de comisión (sumando 100%) sobre un mismo pedido.
* **Actualización de Repositorios y Servicios:** * `PedidoRepository` ahora utiliza queries nativas para navegar por los
  repartos (`findByRepartosNutricionistaEmail`).
    * `PedidoService` actualizado para recibir la lista de repartos al liquidar/enviar un pedido, validando
      matemáticamente que la suma sea exactamente el 100%.
    * `DashboardService` adaptado para calcular el volumen de ventas y bonus aplicando el porcentaje de comisión exacto
      asignado a cada nutricionista.

### 💻 Frontend (React)

* **Gestión de Kilometraje:** * Añadido input en `VistaAdministracion.jsx` para que el Admin asigne los "Kilómetros
  totales (Ida y Vuelta)" al vincular una nutricionista a una farmacia.
    * Añadida tarjeta "Distancia" en `VistaResumen.jsx` (Dashboard Nutricionista). El sistema ahora calcula
      automáticamente la distancia acumulada basándose **únicamente** en las consultas que el Admin ha marcado como
      `VALIDADA` (sistema Anti-Trampas).
* **Corrección de Mapeo de Datos:** Solucionado bug visual donde las nutricionistas no veían sus farmacias asignadas
  debido al cambio de estructura JSON del backend (de `farmaciasIds` a `asignaciones`).
* **Centro de Validaciones (Admin):** * Implementado algoritmo de intercepción en `VistaValidaciones.jsx`. Si el Admin
  valida un pedido de una farmacia con múltiples nutricionistas, salta un Modal de Reparto.
    * Diseñado e integrado componente de "Sliders Inteligentes" vinculados matemáticamente para ajustar visualmente los
      porcentajes de comisión antes del envío.
* **UI/UX:** El menú lateral de "Gestión Empleados" ha sido renombrado a "Administración".

### 🗄️ Base de Datos

* Purgado y recreación de esquemas (Drop/Create) para consolidar la tabla `pedido_reparto`.


## [24/03/2026] - Comisiones Dinámicas, Soft Delete, RBAC Avanzado y Rendimiento

### ⚙️ Backend (Spring Boot)
* **Comisiones Variables:** Eliminado el "hardcode" del 30% en el cálculo del Saldo Virtual. Implementada la columna `porcentaje_comision` en `Farmacia` y actualizado `ConsultaService` para calcular el reparto dinámicamente.
* **Módulo Personal Interno:** Creado `PersonalInternoService` y su controlador para que el administrador pueda dar de alta nuevas credenciales con el rol `ROLE_ADMIN` directamente desde el panel.
* **Evolución RBAC (Control de Accesos):** Introducido el rol `ROLE_SUPERADMIN` en el DataSeeder (asignado a Paco) para crear la jerarquía que blindará la eliminación de otros administradores.
* **Implementación de Borrado Lógico (Soft Delete):** * Añadidas anotaciones `@SQLDelete` y `@SQLRestriction("activo = true")` en `Producto`, `Usuario`, `Farmacia` y `Nutricionista`.
  * Los comandos `repository.delete()` ahora ejecutan un `UPDATE` en segundo plano, manteniendo la integridad referencial de los datos históricos (pedidos y consultas antiguas).

### 💻 Frontend (React)
* **Formularios Dinámicos:** Integrado el campo "Porcentaje de Comisión" en el CRUD de Farmacias (`VistaAdministracion.jsx`).
* **Panel de Personal:** Habilitada la nueva pestaña de "Personal Interno" para la creación ágil de credenciales maestras.

---

> 💡 **RINCÓN ARQUITECTÓNICO Y APRENDIZAJES DE DISEÑO (ENTERPRISE)**
>
> * **Gestión del Almacenamiento (El mito de los 20GB):**
    >   * Las bases de datos relacionales (texto plano) ocupan muy poco espacio. Un millón de registros apenas suponen ~300MB. La saturación en servidores Legacy suele deberse a archivos físicos, logs sin rotación y backups acumulados.
>   * *Solución aplicada:* Externalización total de archivos a Google Drive (la DB solo guarda el ID alfanumérico). Para datos a largo plazo (+5 años), se aplicará archivado en frío (Cold Storage) exportando los registros con `activo=false` a archivos CSV comprimidos antes de ejecutar un *Hard Delete* real.
>
> * **Borrado Lógico vs Borrado Físico:**
    >   * En software médico y financiero **nunca** se ejecuta un `DELETE` en SQL. Se utiliza el Soft Delete (`activo=false`) para aislar los datos visualmente en el Frontend sin destruir la trazabilidad del Backend. Así, un empleado despedido no desaparece de las auditorías de nóminas pasadas, y un producto descatalogado no rompe el historial de facturación de las farmacias.
>
> * **Asincronía, Concurrencia y el JWT (Stateless):**
    >   * El comportamiento de "sobrescribir sesiones" al abrir múltiples pestañas no es un límite del servidor, sino del `localStorage` del navegador, que es compartido por el mismo dominio. (Se testea abriendo modo incógnito o navegadores distintos).
>   * *Escalabilidad del Despliegue:* El servidor Spring Boot (Tomcat) es multihilo. El uso de tokens JWT es *Stateless* (sin estado), lo que significa que el servidor no consume memoria RAM guardando la sesión de cada usuario; simplemente valida la firma criptográfica en milisegundos. Esta arquitectura soporta miles de peticiones simultáneas sin cuellos de botella.

## Fase: Sistema de Borrado Lógico y "Cementerios de Datos" (Trazabilidad Fase 1)

### 🛠️ Backend (Spring Boot)
* **Implementación de Soft Delete (Borrado Lógico):** Modificación de las entidades `Nutricionista`, `Farmacia`, `Producto` y `Administrador` añadiendo el atributo `Boolean activo = true`.
* **Anotaciones de Hibernate:** Uso de `@SQLDelete` para interceptar las peticiones de borrado y convertirlas en `UPDATE tabla SET activo = false`, y `@SQLRestriction("activo = true")` para que las consultas por defecto solo devuelvan registros activos.
* **Proyecciones SQL Anidadas:** Creación de interfaces anidadas (ej. `NutriInactivoProjection`, `AdminInactivoProjection`) dentro de los repositorios para mapear exclusivamente los datos necesarios del historial de bajas.
* **Consultas Nativas (Native Queries):** Implementación de métodos con `@Query(value = "...", nativeQuery = true)` para saltarse la restricción de Hibernate y poder rescatar los registros inactivos (`activo = false`).
* **Controladores y Seguridad:** Creación de endpoints `/bajas` protegidos con `@PreAuthorize` e integración del parche de CORS (`@CrossOrigin`).
* **Escudo Protector de SuperAdmin:** Modificación en `PersonalInternoService` para impedir el borrado de cualquier usuario con el rol `ROLE_SUPERADMIN`, lanzando una excepción `400 Bad Request` controlada.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA Y BUENAS PRÁCTICAS 💡</strong><br><br>

<strong>1. El "Hard Delete" está prohibido en Sistemas Enterprise:</strong> En aplicaciones del sector médico o financiero, jamás se elimina una fila de la base de datos, ya que destruiría la integridad referencial (Foreign Keys) de facturas, consultas o contratos pasados. El <em>Borrado Lógico</em> (Soft Delete) permite mantener el ID vivo en la sombra.<br><br>

<strong>2. Gestión del "Root User" (Cuenta Break-Glass):</strong> El usuario fundador (SuperAdmin inyectado por el DataSeeder) existe en la tabla base de <code>usuarios</code> (para loguearse) pero NO en la tabla derivada de <code>administradores</code>. Esto es una excelente práctica de ciberseguridad corporativa: el Root User debe ser un "fantasma" sin interfaz visual para evitar borrados accidentales o manipulaciones desde el propio panel de control.<br><br>

<strong>3. El Misterio del "Falso 404":</strong> Cuando el Frontend recibe un error <code>404 Not Found</code> al llamar a una API que SÍ existe, suele ser provocado por un fallo crítico (500) en la base de datos (como buscar una columna inexistente). Spring Boot, al explotar, intenta redirigir el fallo a una ruta <code>/error</code> que no tenemos configurada, lo que resulta en un 404 engañoso que enmascara el problema real.
</div>

### 🖥️ Frontend (React & Feature-Based Architecture)
* **Alineación Absoluta de URLs:** Estandarización de las constantes `API_URL` en los servicios (`nutricionistasService.js`, `farmaciaService.js`, `personalInternoService.js`) apuntando a rutas en plural para encajar con el Backend y evitar problemas de concatenación.
* **Carga Concurrente Tolerante a Fallos:** Modificación de las funciones `cargarDatos` utilizando `Promise.all`. Se ha implementado un bloque `.catch(() => [])` en las peticiones del historial de bajas para evitar que un error de red bloquee la renderización de la lista principal.
* **UI del "Cementerio de Datos":** Construcción de un toggle visual en `VistaAdministracion.jsx` y `VistaPersonalInterno.jsx` que despliega una lista secundaria de solo lectura. Se ha usado estilizado con Tailwind (`grayscale opacity-75 line-through`) para dar feedback visual claro de que son registros inactivos/borrados.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE FRONTEND Y FLUJO DE DATOS 💡</strong><br><br>

<strong>1. El Peligro de Promise.all:</strong> Si solicitas 5 endpoints al mismo tiempo usando <code>await Promise.all([...])</code> y uno solo devuelve un error (ej. un 403 o 404), la promesa entera es rechazada y la vista no carga absolutamente nada. Atar un <code>.catch()</code> individual a las peticiones no críticas (como las bajas) blinda la experiencia del usuario.<br><br>

<strong>2. Renderizado Condicional de Trazabilidad:</strong> Al separar los estados (<code>admins</code> vs <code>adminsBajas</code>), la UI se mantiene limpia. Los datos "zombis" no contaminan la tabla principal de trabajo, pero están a un solo clic de distancia para propósitos de auditoría o futura restauración.
</div>

## Fase: Integridad Referencial y Resurrección de Datos (Trazabilidad Fase 2)

### 🛠️ Backend (Spring Boot)
* **Mecanismo de Resurrección Atómica:** Implementación de métodos de restauración en `NutricionistaRepository` y `FarmaciaRepository` utilizando consultas nativas.
* **Resolución del "Síndrome del Zombi":** Se ha diseñado una lógica de restauración en dos pasos (o vía Join) que reactiva simultáneamente la entidad de negocio (`Nutricionista`/`Farmacia`) y su entidad de seguridad asociada (`Usuario`). Esto garantiza que el sistema no sufra de punteros rotos o `EntityNotFoundException` al recargar datos.
* **Gestión de Vínculos en Cascada:** Refactorización de `FarmaciaService` para realizar una limpieza de la tabla intermedia `nutricionista_farmacia` al ejecutar un borrado lógico. Esto evita que las nutricionistas activas mantengan referencias a farmacias inactiva, eliminando errores de carga en el listado principal.
* **Robustez en Repositorios:** Introducción de `@Modifying` y `@Transactional` a nivel de query nativa para asegurar que los cambios en el estado `activo` persistan correctamente sin interferencia de la caché de Hibernate.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: EL ALMA Y EL CUERPO 💡</strong><br><br>

<strong>1. Atomicidad en la Restauración:</strong> En sistemas con seguridad desacoplada (Usuario vs. Perfil), la "muerte" y la "resurrección" deben afectar a ambas tablas. Si resucitas el cuerpo (Perfil) pero dejas el alma muerta (Usuario), Hibernate lanzará un error interno al intentar mapear la relación, lo que el frontend interpretará erróneamente como un 404.<br><br>

<strong>2. Limpieza de Vínculos (Clean Sweep):</strong> Al realizar un borrado lógico de una entidad que es "hija" o "parte de una relación" (como una Farmacia en una lista de asignaciones), es obligatorio limpiar las relaciones activas. Mantener un vínculo vivo hacia un objeto inactivo es una bomba de relojería para las consultas JPA que utilizan filtros de exclusión (<code>@SQLRestriction</code>).
</div>

### 🖥️ Frontend (React)
* **Funcionalidad de Restauración (Undelete):** Integración del botón de acción `handleRestaurar` en los cementerios de Administración.
* **Sincronización de Estado:** Implementación de recarga forzada tras la restauración para mover registros del cementerio a la lista operativa en tiempo real.
* **Iconografía de Trazabilidad:** Uso de `RefreshCw` para diferenciar claramente las acciones de recuperación de las de creación.

### 📅 Próximos Pasos (Hoja de Ruta)
* **Cierre de Simetría:** Aplicar el sistema de borrado lógico y cementerio a las secciones de **Productos** y **Administradores** restantes.
* **Gestión de Restricciones Únicas:** Implementar lógica para manejar conflictos de DNI, CIF o Email cuando un nuevo registro intenta usar datos de un registro que está en el "cementerio".
* **Trazabilidad Profunda:** Añadir metadatos de auditoría (`borrado_por`, `fecha_baja`) para mostrar quién y cuándo ejecutó las acciones de baja.
* **Módulo de Resurrección Avanzada:** Habilitar la edición de campos críticos durante el proceso de restauración para actualizar contratos o condiciones comerciales.

## [25/03/2026] Fase: Single Source of Truth y Reglas de Negocio (Trazabilidad Fase 3)

### 🛠️ Backend (Spring Boot)
* **radares Anti-Zombis (Native Queries):** Creación de métodos `findBy...IgnorandoBajas` en los repositorios (`Usuario`, `Nutricionista`, `Farmacia`, `Producto`) utilizando consultas nativas puras para saltar la restricción global de Hibernate (`@SQLRestriction`).
* **Blindaje de Identidades Únicas:** Refactorización de las capas de Servicio para interceptar la creación de registros duplicados (Email, DNI, CIF, Referencia).
* **Prevención de Excepciones Fatales:** Al interceptar la duplicidad en la capa de negocio, evitamos que la base de datos lance un `DataIntegrityViolationException`, lo cual causaba errores 500/404 no controlados en el Frontend.
* **Nomenclatura Corporativa:** Transición del concepto visual de "Cementerio" a "Archivo Histórico" / "Productos Descatalogados" para mantener coherencia semántica.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: SINGLE SOURCE OF TRUTH 💡</strong><br><br>

<strong>1. La trampa del Soft Delete:</strong> Ocultar registros inactivos con <code>@SQLRestriction</code> es útil para listados, pero es peligroso para las validaciones. Si el sistema ignora a los inactivos al validar IDs únicos (como un DNI), la base de datos colapsará al intentar insertar un duplicado físico.<br><br>

<strong>2. Lógica de Negocio vs Restricciones de BD:</strong> Las restricciones de Base de Datos (<code>UNIQUE</code>) son la última línea de defensa, el muro final. Sin embargo, depender de ellas para la validación devuelve errores genéricos (500/404). Un buen diseño Enterprise intercepta el problema en la Capa de Servicio, traduciéndolo en una excepción de negocio (<code>IllegalArgumentException</code>) que el Frontend pueda mostrar como un mensaje útil al usuario ("Este registro está descatalogado, restáurelo").<br><br>

<strong>3. Inmutabilidad de la Identidad:</strong> No se modifica la estructura de la base de datos para permitir DNI duplicados. Una entidad del mundo real (una persona o un producto físico) equivale a una única fila inmutable. Si la entidad regresa, se restaura su fila original, manteniendo intacto su historial (Single Source of Truth).
</div>

## Fase: Auditoría de Datos y Control de Trazabilidad (Fase 4)

### 🛠️ Backend (Spring Boot)
* **Metadatos de Auditoría:** Inyección de los campos `fecha_baja` y `borrado_por` en las entidades del negocio (`Producto`, `Nutricionista`, `Farmacia`).
* **Interceptación del Usuario:** Uso de `SecurityContextHolder` en la capa de Servicio para extraer el email del usuario autenticado en tiempo real mediante el token JWT.
* **Transición de Soft Delete:** Eliminación de la anotación estática `@SQLDelete` en favor de un guardado manual (`repository.save()`) con el estado modificado. Esto permite inyectar dinámicamente los datos de auditoría antes de la persistencia.
* **Limpieza de Expediente:** Modificación de las consultas nativas de resurrección (`reactivar...`) para incluir `fecha_baja = NULL` y `borrado_por = NULL`, reseteando el historial del registro al volver a la vida operativa.

### 🖥️ Frontend (React)
* **Badges de Auditoría:** Implementación de etiquetas visuales en el Archivo Histórico para mostrar al instante quién ejecutó la acción de borrado y en qué fecha (`new Date().toLocaleDateString()`).

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: AUDITORÍA ESTÁTICA VS DINÁMICA 💡</strong><br><br>

<strong>1. Los límites de @SQLDelete:</strong> Las anotaciones de Hibernate son cómodas, pero estáticas. Si necesitas registrar <em>quién</em> y <em>cuándo</em> se borró algo, <code>@SQLDelete</code> se queda corto porque no puede recibir variables dinámicas del contexto de seguridad. Es necesario pasar a un control manual en la capa de Servicio.<br><br>

<strong>2. Seguridad sin fricción:</strong> Gracias a la arquitectura Stateless con JWT, no necesitamos pasar el ID del usuario desde React en cada petición de borrado (lo cual sería vulnerable a manipulaciones). El Backend lee la identidad firmada e inmutable directamente del token a través del <code>SecurityContextHolder</code>.
</div>


## Fase: Lógica Comercial B2B, Resiliencia y Pedidos Proxy

### 🛠️ Backend (Spring Boot)
* **Gestión de Stock Dinámico:** Implementado endpoint `PATCH` para alternar la disponibilidad de un producto (`hay_existencias`) sin necesidad de descatalogarlo.
* **Rediseño del Manejador de Excepciones:** Destrucción del "agujero negro" de excepciones. Mapeo explícito de `AccessDeniedException` (403), `IllegalArgumentException` (400) y `RuntimeException` (500) para un debugeo transparente.
* **Auditoría B2B (Pedidos Proxy):** Sustitución del flag booleano `creadoPorAdmin` por el campo `creado_por` (String) en la entidad `Pedido`, inyectando automáticamente el email del creador (Admin, Farmacia o Nutricionista) desde el token JWT.
* **Integridad Referencial en Soft Deletes (Escudo Anti-500):** Aplicación de la anotación `@NotFound(action = NotFoundAction.IGNORE)` en las relaciones de `Pedido`, `LineaPedido` y `RepartoPedido`. Permite cargar el historial de ventas intacto aunque los productos, farmacias o nutricionistas hayan sido descatalogados.
* **Regla de Negocio (Bloqueo de Borrado):** Implementada validación en `ProductoService` que impide descatalogar un producto si existen pedidos en estado `PENDIENTE_ENVIO` que lo contengan.

### 🖥️ Frontend (React)
* **Escudo de Promesas (Resiliencia UI):** Blindaje de los `Promise.all` en los Dashboards de Resumen utilizando `.catch(() => null)`. Evita la temida "pantalla en blanco" cuando un usuario intenta cargar widgets para los que no tiene permisos (403).
* **UI de Pedidos Proxy:** Adaptación de `VistaPedidos` para que los administradores puedan actuar como teleoperadores, seleccionando cualquier farmacia destino y generando pedidos en su nombre.
* **Motor de Precios Dinámico en Front:** El catálogo ajusta automáticamente la visualización de PVF o PVP dependiendo de la propiedad `esProvinciaLocal` de la farmacia destino seleccionada.
* **Limpieza del Virtual DOM:** Corrección del renderizado del menú lateral en `Dashboard.jsx` para evitar colisiones de `keys` en usuarios con roles múltiples.
