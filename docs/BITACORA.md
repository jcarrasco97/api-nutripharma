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

* **Comisiones Variables:** Eliminado el "hardcode" del 30% en el cálculo del Saldo Virtual. Implementada la columna
  `porcentaje_comision` en `Farmacia` y actualizado `ConsultaService` para calcular el reparto dinámicamente.
* **Módulo Personal Interno:** Creado `PersonalInternoService` y su controlador para que el administrador pueda dar de
  alta nuevas credenciales con el rol `ROLE_ADMIN` directamente desde el panel.
* **Evolución RBAC (Control de Accesos):** Introducido el rol `ROLE_SUPERADMIN` en el DataSeeder (asignado a Paco) para
  crear la jerarquía que blindará la eliminación de otros administradores.
* **Implementación de Borrado Lógico (Soft Delete):** * Añadidas anotaciones `@SQLDelete` y
  `@SQLRestriction("activo = true")` en `Producto`, `Usuario`, `Farmacia` y `Nutricionista`.
    * Los comandos `repository.delete()` ahora ejecutan un `UPDATE` en segundo plano, manteniendo la integridad
      referencial de los datos históricos (pedidos y consultas antiguas).

### 💻 Frontend (React)

* **Formularios Dinámicos:** Integrado el campo "Porcentaje de Comisión" en el CRUD de Farmacias (
  `VistaAdministracion.jsx`).
* **Panel de Personal:** Habilitada la nueva pestaña de "Personal Interno" para la creación ágil de credenciales
  maestras.

---

> 💡 **RINCÓN ARQUITECTÓNICO Y APRENDIZAJES DE DISEÑO (ENTERPRISE)**
>
> * **Gestión del Almacenamiento (El mito de los 20GB):**
    >   * Las bases de datos relacionales (texto plano) ocupan muy poco espacio. Un millón de registros apenas suponen ~
    300MB. La saturación en servidores Legacy suele deberse a archivos físicos, logs sin rotación y backups acumulados.
    >
* *Solución aplicada:* Externalización total de archivos a Google Drive (la DB solo guarda el ID alfanumérico). Para
  datos a largo plazo (+5 años), se aplicará archivado en frío (Cold Storage) exportando los registros con
  `activo=false` a archivos CSV comprimidos antes de ejecutar un *Hard Delete* real.
>
> * **Borrado Lógico vs Borrado Físico:**
    >   * En software médico y financiero **nunca** se ejecuta un `DELETE` en SQL. Se utiliza el Soft Delete (
    `activo=false`) para aislar los datos visualmente en el Frontend sin destruir la trazabilidad del Backend. Así, un
    empleado despedido no desaparece de las auditorías de nóminas pasadas, y un producto descatalogado no rompe el
    historial de facturación de las farmacias.
>
> * **Asincronía, Concurrencia y el JWT (Stateless):**
    >   * El comportamiento de "sobrescribir sesiones" al abrir múltiples pestañas no es un límite del servidor, sino
    del `localStorage` del navegador, que es compartido por el mismo dominio. (Se testea abriendo modo incógnito o
    navegadores distintos).
    >
* *Escalabilidad del Despliegue:* El servidor Spring Boot (Tomcat) es multihilo. El uso de tokens JWT es *Stateless* (
  sin estado), lo que significa que el servidor no consume memoria RAM guardando la sesión de cada usuario; simplemente
  valida la firma criptográfica en milisegundos. Esta arquitectura soporta miles de peticiones simultáneas sin cuellos
  de botella.

## Fase: Sistema de Borrado Lógico y "Cementerios de Datos" (Trazabilidad Fase 1)

### 🛠️ Backend (Spring Boot)

* **Implementación de Soft Delete (Borrado Lógico):** Modificación de las entidades `Nutricionista`, `Farmacia`,
  `Producto` y `Administrador` añadiendo el atributo `Boolean activo = true`.
* **Anotaciones de Hibernate:** Uso de `@SQLDelete` para interceptar las peticiones de borrado y convertirlas en
  `UPDATE tabla SET activo = false`, y `@SQLRestriction("activo = true")` para que las consultas por defecto solo
  devuelvan registros activos.
* **Proyecciones SQL Anidadas:** Creación de interfaces anidadas (ej. `NutriInactivoProjection`,
  `AdminInactivoProjection`) dentro de los repositorios para mapear exclusivamente los datos necesarios del historial de
  bajas.
* **Consultas Nativas (Native Queries):** Implementación de métodos con `@Query(value = "...", nativeQuery = true)` para
  saltarse la restricción de Hibernate y poder rescatar los registros inactivos (`activo = false`).
* **Controladores y Seguridad:** Creación de endpoints `/bajas` protegidos con `@PreAuthorize` e integración del parche
  de CORS (`@CrossOrigin`).
* **Escudo Protector de SuperAdmin:** Modificación en `PersonalInternoService` para impedir el borrado de cualquier
  usuario con el rol `ROLE_SUPERADMIN`, lanzando una excepción `400 Bad Request` controlada.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA Y BUENAS PRÁCTICAS 💡</strong><br><br>

<strong>1. El "Hard Delete" está prohibido en Sistemas Enterprise:</strong> En aplicaciones del sector médico o
financiero, jamás se elimina una fila de la base de datos, ya que destruiría la integridad referencial (Foreign Keys) de
facturas, consultas o contratos pasados. El <em>Borrado Lógico</em> (Soft Delete) permite mantener el ID vivo en la
sombra.<br><br>

<strong>2. Gestión del "Root User" (Cuenta Break-Glass):</strong> El usuario fundador (SuperAdmin inyectado por el
DataSeeder) existe en la tabla base de <code>usuarios</code> (para loguearse) pero NO en la tabla derivada de <code>
administradores</code>. Esto es una excelente práctica de ciberseguridad corporativa: el Root User debe ser un "
fantasma" sin interfaz visual para evitar borrados accidentales o manipulaciones desde el propio panel de
control.<br><br>

<strong>3. El Misterio del "Falso 404":</strong> Cuando el Frontend recibe un error <code>404 Not Found</code> al llamar
a una API que SÍ existe, suele ser provocado por un fallo crítico (500) en la base de datos (como buscar una columna
inexistente). Spring Boot, al explotar, intenta redirigir el fallo a una ruta <code>/error</code> que no tenemos
configurada, lo que resulta en un 404 engañoso que enmascara el problema real.
</div>

### 🖥️ Frontend (React & Feature-Based Architecture)

* **Alineación Absoluta de URLs:** Estandarización de las constantes `API_URL` en los servicios (
  `nutricionistasService.js`, `farmaciaService.js`, `personalInternoService.js`) apuntando a rutas en plural para
  encajar con el Backend y evitar problemas de concatenación.
* **Carga Concurrente Tolerante a Fallos:** Modificación de las funciones `cargarDatos` utilizando `Promise.all`. Se ha
  implementado un bloque `.catch(() => [])` en las peticiones del historial de bajas para evitar que un error de red
  bloquee la renderización de la lista principal.
* **UI del "Cementerio de Datos":** Construcción de un toggle visual en `VistaAdministracion.jsx` y
  `VistaPersonalInterno.jsx` que despliega una lista secundaria de solo lectura. Se ha usado estilizado con Tailwind (
  `grayscale opacity-75 line-through`) para dar feedback visual claro de que son registros inactivos/borrados.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE FRONTEND Y FLUJO DE DATOS 💡</strong><br><br>

<strong>1. El Peligro de Promise.all:</strong> Si solicitas 5 endpoints al mismo tiempo usando <code>await
Promise.all([...])</code> y uno solo devuelve un error (ej. un 403 o 404), la promesa entera es rechazada y la vista no
carga absolutamente nada. Atar un <code>.catch()</code> individual a las peticiones no críticas (como las bajas) blinda
la experiencia del usuario.<br><br>

<strong>2. Renderizado Condicional de Trazabilidad:</strong> Al separar los estados (<code>admins</code> vs <code>
adminsBajas</code>), la UI se mantiene limpia. Los datos "zombis" no contaminan la tabla principal de trabajo, pero
están a un solo clic de distancia para propósitos de auditoría o futura restauración.
</div>

## Fase: Integridad Referencial y Resurrección de Datos (Trazabilidad Fase 2)

### 🛠️ Backend (Spring Boot)

* **Mecanismo de Resurrección Atómica:** Implementación de métodos de restauración en `NutricionistaRepository` y
  `FarmaciaRepository` utilizando consultas nativas.
* **Resolución del "Síndrome del Zombi":** Se ha diseñado una lógica de restauración en dos pasos (o vía Join) que
  reactiva simultáneamente la entidad de negocio (`Nutricionista`/`Farmacia`) y su entidad de seguridad asociada (
  `Usuario`). Esto garantiza que el sistema no sufra de punteros rotos o `EntityNotFoundException` al recargar datos.
* **Gestión de Vínculos en Cascada:** Refactorización de `FarmaciaService` para realizar una limpieza de la tabla
  intermedia `nutricionista_farmacia` al ejecutar un borrado lógico. Esto evita que las nutricionistas activas mantengan
  referencias a farmacias inactiva, eliminando errores de carga en el listado principal.
* **Robustez en Repositorios:** Introducción de `@Modifying` y `@Transactional` a nivel de query nativa para asegurar
  que los cambios en el estado `activo` persistan correctamente sin interferencia de la caché de Hibernate.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: EL ALMA Y EL CUERPO 💡</strong><br><br>

<strong>1. Atomicidad en la Restauración:</strong> En sistemas con seguridad desacoplada (Usuario vs. Perfil), la "
muerte" y la "resurrección" deben afectar a ambas tablas. Si resucitas el cuerpo (Perfil) pero dejas el alma muerta (
Usuario), Hibernate lanzará un error interno al intentar mapear la relación, lo que el frontend interpretará
erróneamente como un 404.<br><br>

<strong>2. Limpieza de Vínculos (Clean Sweep):</strong> Al realizar un borrado lógico de una entidad que es "hija" o "
parte de una relación" (como una Farmacia en una lista de asignaciones), es obligatorio limpiar las relaciones activas.
Mantener un vínculo vivo hacia un objeto inactivo es una bomba de relojería para las consultas JPA que utilizan filtros
de exclusión (<code>@SQLRestriction</code>).
</div>

### 🖥️ Frontend (React)

* **Funcionalidad de Restauración (Undelete):** Integración del botón de acción `handleRestaurar` en los cementerios de
  Administración.
* **Sincronización de Estado:** Implementación de recarga forzada tras la restauración para mover registros del
  cementerio a la lista operativa en tiempo real.
* **Iconografía de Trazabilidad:** Uso de `RefreshCw` para diferenciar claramente las acciones de recuperación de las de
  creación.

### 📅 Próximos Pasos (Hoja de Ruta)

* **Cierre de Simetría:** Aplicar el sistema de borrado lógico y cementerio a las secciones de **Productos** y *
  *Administradores** restantes.
* **Gestión de Restricciones Únicas:** Implementar lógica para manejar conflictos de DNI, CIF o Email cuando un nuevo
  registro intenta usar datos de un registro que está en el "cementerio".
* **Trazabilidad Profunda:** Añadir metadatos de auditoría (`borrado_por`, `fecha_baja`) para mostrar quién y cuándo
  ejecutó las acciones de baja.
* **Módulo de Resurrección Avanzada:** Habilitar la edición de campos críticos durante el proceso de restauración para
  actualizar contratos o condiciones comerciales.

## [25/03/2026] Fase: Single Source of Truth y Reglas de Negocio (Trazabilidad Fase 3)

### 🛠️ Backend (Spring Boot)

* **radares Anti-Zombis (Native Queries):** Creación de métodos `findBy...IgnorandoBajas` en los repositorios (
  `Usuario`, `Nutricionista`, `Farmacia`, `Producto`) utilizando consultas nativas puras para saltar la restricción
  global de Hibernate (`@SQLRestriction`).
* **Blindaje de Identidades Únicas:** Refactorización de las capas de Servicio para interceptar la creación de registros
  duplicados (Email, DNI, CIF, Referencia).
* **Prevención de Excepciones Fatales:** Al interceptar la duplicidad en la capa de negocio, evitamos que la base de
  datos lance un `DataIntegrityViolationException`, lo cual causaba errores 500/404 no controlados en el Frontend.
* **Nomenclatura Corporativa:** Transición del concepto visual de "Cementerio" a "Archivo Histórico" / "Productos
  Descatalogados" para mantener coherencia semántica.

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: SINGLE SOURCE OF TRUTH 💡</strong><br><br>

<strong>1. La trampa del Soft Delete:</strong> Ocultar registros inactivos con <code>@SQLRestriction</code> es útil para
listados, pero es peligroso para las validaciones. Si el sistema ignora a los inactivos al validar IDs únicos (como un
DNI), la base de datos colapsará al intentar insertar un duplicado físico.<br><br>

<strong>2. Lógica de Negocio vs Restricciones de BD:</strong> Las restricciones de Base de Datos (<code>UNIQUE</code>)
son la última línea de defensa, el muro final. Sin embargo, depender de ellas para la validación devuelve errores
genéricos (500/404). Un buen diseño Enterprise intercepta el problema en la Capa de Servicio, traduciéndolo en una
excepción de negocio (<code>IllegalArgumentException</code>) que el Frontend pueda mostrar como un mensaje útil al
usuario ("Este registro está descatalogado, restáurelo").<br><br>

<strong>3. Inmutabilidad de la Identidad:</strong> No se modifica la estructura de la base de datos para permitir DNI
duplicados. Una entidad del mundo real (una persona o un producto físico) equivale a una única fila inmutable. Si la
entidad regresa, se restaura su fila original, manteniendo intacto su historial (Single Source of Truth).
</div>

## Fase: Auditoría de Datos y Control de Trazabilidad (Fase 4)

### 🛠️ Backend (Spring Boot)

* **Metadatos de Auditoría:** Inyección de los campos `fecha_baja` y `borrado_por` en las entidades del negocio (
  `Producto`, `Nutricionista`, `Farmacia`).
* **Interceptación del Usuario:** Uso de `SecurityContextHolder` en la capa de Servicio para extraer el email del
  usuario autenticado en tiempo real mediante el token JWT.
* **Transición de Soft Delete:** Eliminación de la anotación estática `@SQLDelete` en favor de un guardado manual (
  `repository.save()`) con el estado modificado. Esto permite inyectar dinámicamente los datos de auditoría antes de la
  persistencia.
* **Limpieza de Expediente:** Modificación de las consultas nativas de resurrección (`reactivar...`) para incluir
  `fecha_baja = NULL` y `borrado_por = NULL`, reseteando el historial del registro al volver a la vida operativa.

### 🖥️ Frontend (React)

* **Badges de Auditoría:** Implementación de etiquetas visuales en el Archivo Histórico para mostrar al instante quién
  ejecutó la acción de borrado y en qué fecha (`new Date().toLocaleDateString()`).

<div style="background-color: #e6f7ff; color: #0050b3; padding: 15px; border-left: 5px solid #1890ff; border-radius: 5px; margin: 20px 0;">
<strong>💡 LECCIONES DE ARQUITECTURA SENIOR: AUDITORÍA ESTÁTICA VS DINÁMICA 💡</strong><br><br>

<strong>1. Los límites de @SQLDelete:</strong> Las anotaciones de Hibernate son cómodas, pero estáticas. Si necesitas
registrar <em>quién</em> y <em>cuándo</em> se borró algo, <code>@SQLDelete</code> se queda corto porque no puede recibir
variables dinámicas del contexto de seguridad. Es necesario pasar a un control manual en la capa de Servicio.<br><br>

<strong>2. Seguridad sin fricción:</strong> Gracias a la arquitectura Stateless con JWT, no necesitamos pasar el ID del
usuario desde React en cada petición de borrado (lo cual sería vulnerable a manipulaciones). El Backend lee la identidad
firmada e inmutable directamente del token a través del <code>SecurityContextHolder</code>.
</div>

## Fase: Lógica Comercial B2B, Resiliencia y Pedidos Proxy

### 🛠️ Backend (Spring Boot)

* **Gestión de Stock Dinámico:** Implementado endpoint `PATCH` para alternar la disponibilidad de un producto (
  `hay_existencias`) sin necesidad de descatalogarlo.
* **Rediseño del Manejador de Excepciones:** Destrucción del "agujero negro" de excepciones. Mapeo explícito de
  `AccessDeniedException` (403), `IllegalArgumentException` (400) y `RuntimeException` (500) para un debugeo
  transparente.
* **Auditoría B2B (Pedidos Proxy):** Sustitución del flag booleano `creadoPorAdmin` por el campo `creado_por` (String)
  en la entidad `Pedido`, inyectando automáticamente el email del creador (Admin, Farmacia o Nutricionista) desde el
  token JWT.
* **Integridad Referencial en Soft Deletes (Escudo Anti-500):** Aplicación de la anotación
  `@NotFound(action = NotFoundAction.IGNORE)` en las relaciones de `Pedido`, `LineaPedido` y `RepartoPedido`. Permite
  cargar el historial de ventas intacto aunque los productos, farmacias o nutricionistas hayan sido descatalogados.
* **Regla de Negocio (Bloqueo de Borrado):** Implementada validación en `ProductoService` que impide descatalogar un
  producto si existen pedidos en estado `PENDIENTE_ENVIO` que lo contengan.

### 🖥️ Frontend (React)

* **Escudo de Promesas (Resiliencia UI):** Blindaje de los `Promise.all` en los Dashboards de Resumen utilizando
  `.catch(() => null)`. Evita la temida "pantalla en blanco" cuando un usuario intenta cargar widgets para los que no
  tiene permisos (403).
* **UI de Pedidos Proxy:** Adaptación de `VistaPedidos` para que los administradores puedan actuar como teleoperadores,
  seleccionando cualquier farmacia destino y generando pedidos en su nombre.
* **Motor de Precios Dinámico en Front:** El catálogo ajusta automáticamente la visualización de PVF o PVP dependiendo
  de la propiedad `esProvinciaLocal` de la farmacia destino seleccionada.
* **Limpieza del Virtual DOM:** Corrección del renderizado del menú lateral en `Dashboard.jsx` para evitar colisiones de
  `keys` en usuarios con roles múltiples.

### [26/03/2026]

#### 🖥️ Frontend (React)

* **Adopción de Feature-Sliced Design (FSD):** Refactorización masiva de las vistas monolíticas hacia una Arquitectura
  basada en Funcionalidades. Separación estricta de responsabilidades dividiendo el código en hooks (Cerebro/Lógica de
  negocio), components (Órganos visuales) y views (Orquestadores).
* **Dominio de Administración:** Renombrado estratégico del módulo `entidades` a `administracion` para reflejar con
  precisión el contexto de negocio. Desacoplamiento total del Centro de Mando (`VistaPersonalInterno`).
* **Módulo de Pedidos y Finanzas:** Extracción de la compleja lógica de carrito, monedero virtual, bonificaciones y "
  Modo Proxy" hacia el hook `usePedidos.js`. Fragmentación de la UI en componentes aislados (`TarjetaMonedero`,
  `CatalogoProductos`, `CestaPedidos`).
* **Dashboards y Analytics:** Refactorización profunda de los tres paneles principales (`VistaResumen`,
  `VistaResumenAdmin`, `VistaResumenFarmacia`). Resolución de advertencias de renderizado en gráficos de Recharts (
  `ResponsiveContainer`) asegurando contenedores con altura fija.
* **Protección de Renderizado Asíncrono:** Implementación de encadenamiento opcional (`?.map`) y fallback de arrays
  vacíos (`|| []`) en historiales y listas para evitar caídas de la aplicación (crashes) durante la resolución de
  promesas en perfiles complejos como el Nutricionista.
* **Módulos de Soporte (Auth, Docs, Suministros):** Limpieza de las vistas de inicio de sesión (`Login`,
  `ResetPassword`), subida de archivos (`VistaDocumentacion`) y peticiones de material (`VistaSuministros`), delegando
  el control de formularios y tokens a hooks dedicados.
* **Desacoplamiento del Layout Principal:** Limpieza extrema de `Dashboard.jsx`, externalizando la lógica de roles, el
  menú lateral (`Sidebar.jsx`) y el estado de la navegación, convirtiéndolo en un enrutador puro.

## [27/03/2026] - Arquitectura de Identidad: El Caso de Paco (Admin + Nutricionista)

### 🧑‍💼 Contexto y Decisión

Paco, dueño de la empresa, necesita operar en el sistema con dos contextos completamente distintos: como **Administrador
** (gestión del negocio) y como **Nutricionista** (trabajo clínico diario). Se evaluaron dos opciones:

1. Un único usuario con selector de rol al login + botón de cambio de rol en sesión activa.
2. Dos cuentas separadas, una por contexto.

**Decisión tomada: Dos cuentas separadas. No se implementa ningún código adicional.**

---

### 🏗️ Estructura de Datos Resultante

```
usuarios
├── paco.admin@nutripharma.com   → ROLE_ADMIN
│     └── sin fila en tabla `administradores` (igual que el SuperAdmin de Nacho)
└── paco@nutripharma.com         → ROLE_NUTRICIONISTA
      └── tabla `nutricionistas` (relación @OneToOne con Usuario)
```

La cuenta administrativa de Paco es intencionalmente un "fantasma de negocio": existe en la tabla `usuarios` para
autenticarse, pero no tiene perfil operativo. Esto es exactamente el mismo patrón que ya aplicamos con el SuperAdmin de
Nacho.

---

### ❌ Por qué se descartó el Modal de Selección de Rol

La opción del modal ("¿Entras como Admin o como Nutricionista?") es un **antipatrón** por varias razones:

* **Complejidad técnica encubierta:** Cambiar de rol en mitad de una sesión activa implica reemplazar el JWT, limpiar
  todo el estado de React y redirigir al usuario. Técnicamente es un logout/login disfrazado de botón.
* **Fuente de confusión para el usuario:** Obliga a Paco a tomar una decisión activa cada vez que se loguea. Si se
  equivoca de rol, tiene que salir y volver a entrar. Para alguien ajeno a la programación, eso es fricción innecesaria.
* **Auditoría ambigua:** Los logs de acceso no pueden distinguir en qué contexto actuó Paco. Una única sesión birol
  registra "Paco hizo algo", pero no si actuó como gestor o como clínico.

---

### ✅ Por qué Dos Cuentas es la Decisión Correcta

#### 1. Seguridad — Principio de Mínimo Privilegio (Least Privilege)

Un token JWT tiene un alcance fijo e inamovible durante su vida útil. Si Paco está logueado como Nutricionista y alguien
compromete su sesión, el atacante solo accede a datos clínicos. No puede tocar configuración del sistema, saldos ni
gestión de usuarios. Con el modal de cambio de rol, un token comprometido potencialmente da acceso a todo el sistema.

Este principio está recogido en los frameworks de seguridad enterprise más importantes: **ISO 27001**, **NIST** y las
guías de auditoría de sistemas ERP como SAP, Oracle EBS y Microsoft Dynamics.

#### 2. User-Friendly — El Hábito vs. La Decisión

Paradójicamente, dos cuentas bien nombradas son más simples que un selector de rol. Paco aprende un hábito una sola vez:

* Tablet para pasar consulta → `paco@nutripharma.com`
* Portátil para gestionar la empresa → `paco.admin@nutripharma.com`

Un hábito no requiere pensar. Una decisión en un modal, sí.

#### 3. Buenas Prácticas — Identity Segregation en Sistemas ERP

El término técnico exacto para esta práctica es **Identity Segregation**, derivado del principio de **Separation of
Concerns (SoC)** aplicado a la capa de identidad. Es un estándar en todos los sistemas ERP enterprise.

En SAP, por ejemplo, el administrador técnico del sistema (BASIS) tiene una cuenta técnica completamente separada de su
cuenta funcional de negocio, aunque sea la misma persona física. **Nacho es el BASIS de NutriPharma. Paco es el usuario
funcional.**

#### 4. Trazabilidad y Auditoría

Cuando se revisen los logs en producción (ahora o dentro de dos años), cada acción queda firmada con una identidad
inequívoca y su contexto es inmediato:

* `paco.admin@` en los logs → acción de gestión empresarial.
* `paco@` en los logs → consulta clínica registrada.

Esto es especialmente crítico en sistemas que manejan datos financieros y sanitarios, donde una auditoría puede exigir
reconstruir exactamente qué decisión tomó quién y bajo qué rol.

---

### 💡 Lección de Arquitectura

> La complejidad que no se justifica con un requisito real es deuda técnica. El modal de selección de rol hubiera
> añadido código nuevo, estado adicional en React, lógica de reemplazo de JWT y surface de ataque extra, todo para
> resolver un problema que dos cuentas resuelven sin escribir una sola línea. En ingeniería de software, la mejor solución
> suele ser la que aprovecha lo que ya existe.

### [27/03/2026] - Arquitectura de Datos: Derecho al Olvido, Trazabilidad y Ciberseguridad en Producción

#### 🛡️ El Dilema del Borrado en Bases de Datos

En sistemas empresariales (ERPs, aplicaciones clínicas y financieras), borrar un registro físicamente (`DELETE` en SQL)
rompe la integridad referencial. Si se elimina a un nutricionista, los pedidos o consultas históricas asociados a él
quedan huérfanos o corrompen los cálculos financieros de años anteriores.

* **Solución operativa:** Borrado Lógico (*Soft Delete*), marcando el registro como `activo = false`.
* **El problema legal:** El *Soft Delete* choca frontalmente con el Reglamento General de Protección de Datos (RGPD) y
  el "Derecho al Olvido", ya que los datos personales (nombre, DNI, email) siguen intactos en la base de datos, aunque
  estén ocultos en la interfaz.

#### ✅ La Decisión Arquitectónica: Anonimización (Seudonimización)

Para cumplir con la ley y mantener la integridad financiera del sistema simultáneamente, no se borra la fila, se *
*anonimiza**.

* Se conserva el `id` original (los pedidos y facturas históricas siguen cuadrando perfectamente).
* Se sobrescriben los datos sensibles con valores ficticios o hashes unidireccionales:
    * Nombre → "Usuario Eliminado"
    * Email → `deleted_hash123@nutripharma.local`
    * DNI → `00000000A`

De esta forma, la persona física desaparece a efectos legales, pero la entidad operativa perdura a efectos de auditoría.

#### ❌ La Regla de Oro: Prohibido ejecutar lógica de negocio desde el gestor SQL

La aplicación del "Derecho al Olvido" (o cualquier modificación crítica de datos de negocio) jamás debe hacerse
ejecutando sentencias manuales (`UPDATE` o `DELETE`) directamente en la base de datos de producción por un
administrador.

**¿Por qué todo debe pasar por el Backend (Capa de Aplicación)?**

* **Lógica en Cascada:** Al invocar un endpoint (`/api/usuarios/{id}/anonimizar`), el backend no solo actualiza la base
  de datos, sino que orquesta acciones periféricas críticas: eliminar archivos personales en Google Drive, invalidar
  tokens JWT activos, y enviar correos legales de confirmación de borrado.
* **Trazabilidad Inmutable:** La aplicación escribe en los logs y en tablas de auditoría exactamente *quién* pulsó el
  botón y *cuándo*, dejando un rastro criptográfico legal. Si se hace por SQL, se puentea toda la seguridad y no hay
  registro auditable de la operación.

#### 🔐 Arquitectura de Despliegue y Ciberseguridad (Zero Trust)

Para llevar el sistema a producción bajo estándares *enterprise*, la base de datos se blinda siguiendo el Principio de
Mínimo Privilegio (*Least Privilege*):

1. **Aislamiento de Red (Private Subnet):** La base de datos no tiene salida ni entrada a Internet. No se puede acceder
   a ella desde el exterior con clientes SQL (como DBeaver o DataGrip).
2. **El Backend es el único VIP:** La única máquina autorizada a nivel de red para hablar con el puerto SQL es el
   servidor donde se ejecuta la API en Java. Las credenciales que usa la API solo tienen permisos de lectura/escritura
   de datos (DML), nunca permisos estructurales (`DROP TABLE`, `ALTER`).
3. **Protocolo "Break Glass" (Romper el cristal):** Si ocurre un desastre y un ingeniero necesita entrar al SQL
   manualmente para arreglar datos corrompidos, no usa una contraseña estática.

* Se conecta vía VPN a un servidor puente aislado (*Bastion Host*).
* Solicita credenciales temporales (*Just-In-Time Access*) que caducan automáticamente en 2 horas.
* Todas sus consultas quedan grabadas para auditoría.

**Conclusión:** La base de datos es "muda y tonta"; solo almacena lo que la aplicación (que es inteligente y auditable)
le ordena guardar.

### [27/03/2026] - Refinamiento de Datos, UX y Definición de Arquitectura Anti-Fraude

#### 🧠 Decisiones de Negocio y Compliance

* **Derecho al Olvido vs Interés Legítimo:** Se determinó que, al operar en un entorno B2B, las Farmacias no están
  sujetas al derecho al olvido (son entidades jurídicas). Los teléfonos y correos de las nutricionistas se consideran
  herramientas corporativas, por lo que prima el Interés Legítimo de la empresa para conservar la trazabilidad de
  operaciones frente a posibles auditorías o disputas legales.
* **Minimización de Datos:** Se eliminó el campo `DNI` de toda la arquitectura (Backend y Frontend), sustituyéndolo por
  `teléfono` corporativo (no único). Esto reduce el "surface area" de datos sensibles que la aplicación almacena,
  mitigando riesgos de ciberseguridad.

#### 🖥️ Mejoras UX en Administración (React)

* **Visualización de Relaciones (N:M):** Implementación de un Modal de vista rápida (`ModalVerAsignaciones.jsx`) para
  consultar las farmacias asignadas a una nutricionista (y viceversa) sin necesidad de entrar al modo edición,
  reduciendo la fricción cognitiva del Administrador.
* **Ordenación Inteligente (Smart Sorting):** En el modal de edición, las farmacias que ya están asignadas a la
  nutricionista "flotan" automáticamente a la parte superior de la lista, evitando el scroll innecesario.
* **Badges y Microinteracciones:** Inclusión de etiquetas visuales rápidas (horas de contrato, porcentaje de comisión)
  en el listado general y sustitución del botón de stock por un interruptor (Switch estilo iOS) para mayor claridad del
  estado binario del producto.

#### 🏗️ Roadmap Técnico Definido (Alta Integridad)

Se validó la arquitectura para las siguientes fases críticas del proyecto:

1. **Pruebas Periciales (Evidencias):** Vinculación de fotos de agendas físicas directamente a la entidad `Consulta`
   mediante una máquina de estados estricta.
2. **Notificaciones (Event-Driven):** Uso de eventos asíncronos en Spring Boot para confirmar pedidos y consultas sin
   bloquear el hilo principal.
3. **Auditoría Inmutable:** Implementación futura de **Hibernate Envers** para registrar cada `INSERT`, `UPDATE` y
   `DELETE`, garantizando trazabilidad absoluta ante posibles juicios por fraude.
4. **Registro de Accesos:** Interceptores de seguridad para guardar la IP y el User-Agent de cada login y petición API.

## [07/04/2026] 📸 Sistema de Evidencias Fotográficas ("Prueba de Vida")

### Integración Cloud Segura

Implementación de subida/descarga de archivos binarios (`multipart/form-data`) conectados directamente a la API de
Google Drive desde Spring Boot, aislando el almacenamiento pesado de la base de datos principal.

### Mutabilidad por Estados (Smart Lock)

Creación de una máquina de estados para la interfaz. El nutricionista tiene libertad para adjuntar, visualizar (mediante
previsualizaciones generadas en memoria RAM con `URL.createObjectURL`) y sustituir la foto libremente mientras la
consulta esté en `BORRADOR`.

### Sellado de Auditoría

Al pasar la consulta a estado `VALIDADA`, la interfaz aplica un bloqueo inmutable (candado) sobre la evidencia. El
archivo queda sellado criptográficamente para auditorías de nóminas y comisiones.

### Flujo de Desbloqueo (Unlock-by-Admin)

El Administrador dispone de un visor inmersivo de evidencias en su Centro de Validaciones con capacidad destructiva. Si
la evidencia es ilegible, el admin ejecuta un borrado físico en Drive que reabre la consulta automáticamente para que el
nutricionista enmiende el error.

## [08/04/2026] 📧 Sistema de Notificaciones Transaccionales (Event-Driven)

### Arquitectura Asíncrona (Pub/Sub)

Implementación del patrón Publisher-Subscriber mediante eventos de Spring (`@TransactionalEventListener` y `@Async`). El
envío de correos se delega a un hilo secundario estrictamente tras el `COMMIT` de la base de datos, garantizando tiempos
de respuesta instantáneos en el frontend de React.

### Entornos y Mocking Seguros (Sandbox)

Configuración de Mailtrap como servidor SMTP Sandbox para el entorno de desarrollo. Esto aísla los envíos, permitiendo
pruebas reales de formato y adjuntos sin el riesgo de enviar correos accidentales a clientes reales.

### Motor de Plantillas y Branding (Thymeleaf)

Diseño de correos B2B en HTML compatible con clientes corporativos. Las variables (totales, nombres) y enlaces dinámicos
al VPS se inyectan desde el backend. Se implementó la incrustación del logo corporativo mediante Content-ID (CID inline)
para garantizar su visualización en Outlook y Gmail.

### Infraestructura PDF (OpenPDF)

Integración de OpenPDF como generador de documentos y facturas directamente en memoria RAM (`ByteArrayOutputStream`). El
documento se genera al vuelo y se adjunta automáticamente al correo, evitando la persistencia temporal en el disco duro
del servidor por razones de seguridad y rendimiento.

## [08/04/2026] 🛡️ Arquitectura de Seguridad Empresarial: El "Gran Hermano" y Notario Digital

Se ha diseñado e implementado una arquitectura de auditoría Zero-Trust (Cero Confianza) dividida en tres capas
independientes para garantizar la trazabilidad absoluta de operaciones, datos y red, cumpliendo con los estándares
legales B2B.

### Nivel 1: Trazabilidad de Accesos y Red (Seguridad Event-Driven)

Se ha implementado un registro inmutable de inicios de sesión para detectar patrones de acceso anómalos o suplantación
de identidad.

* **Mecanismo:** Patrón Pub/Sub (`@EventListener` asíncrono). El hilo principal de autenticación (JWT) delega el
  guardado del log a un hilo secundario para mantener latencias <50ms.
* **Datos Capturados:** Email del usuario, Dirección IP real (resolviendo cabeceras `X-Forwarded-For` de proxys
  inversos) y Dispositivo/Navegador (`User-Agent`).
* **Auditoría Forense (SQL):**
  ```sql
  SELECT ip_address, user_agent, fecha_acceso 
  FROM registro_accesos 
  WHERE usuario_email = 'sospechoso@nutripharma.es' 
  ORDER BY fecha_acceso DESC;

### Nivel 2: El Notario Digital (Hibernate Envers)

Para blindar financieramente el sistema (comisiones y liquidaciones), se ha integrado Hibernate Envers configurado con
una Entidad de Revisión Personalizada (`AuditoriaRevisionEntity`).

* **Mecanismo:** Envers intercepta de forma nativa a nivel de ORM cualquier `INSERT`, `UPDATE` o `DELETE` sobre las
  entidades críticas marcadas con `@Audited` (Consultas, Pedidos, Productos, Farmacias, etc.).
* **Inyección de Identidad:** Mediante un `RevisionListener`, el motor lee el token JWT del contexto de seguridad de
  Spring (`SecurityContextHolder`) y estampa el correo del autor en cada mutación de datos.
* **Control de Telarañas de Entidades:** Se aplicó el modo de ignorado (`@NotAudited`) a catálogos estáticos como la
  tabla de Roles, evitando cuellos de botella en la generación de tablas espejo (`_aud`).
    * **Auditoría Forense (SQL):**

  ```sql
      -- Ejemplo para rastrear manipulaciones en Consultas Médicas
      SELECT 
      r.id AS id_revision,
      FROM_UNIXTIME(r.timestamp / 1000) AS fecha_del_cambio,
      r.usuario_email AS culpable,
      c.id AS consulta_modificada,
      CASE c.revtype 
          WHEN 0 THEN 'CREACIÓN'
          WHEN 1 THEN 'MODIFICACIÓN'
          WHEN 2 THEN 'BORRADO'
      END AS accion_realizada,
      c.estado,
      c.nuevas,
      c.revisiones
      FROM auditoria_revisiones r
      JOIN consultas_aud c ON r.id = c.rev
      WHERE r.usuario_email = 'sospechoso@nutripharma.es'
      ORDER BY r.timestamp DESC;

### Nivel 3: Trazabilidad de API (Interceptor HTTP)

Capa de monitoreo perimetral para registrar la actividad transaccional general de la API (excluyendo cargas útiles
pesadas por privacidad).

* **Mecanismo:** Implementación nativa de `HandlerInterceptor` de Spring MVC (`ApiAuditInterceptor`), evaluando el ciclo
  `afterCompletion`.
* **Filtro de Acción:** Solo procesa peticiones de mutación (`POST`, `PUT`, `DELETE`, `PATCH`), ignorando consultas de
  lectura (`GET`) para evitar saturar los discos de almacenamiento.
* **Salida de Logs:** Actualmente emite los registros de auditoría por salida estándar (Consola IDE). Preparado para
  volcado a disco (`/var/log/nutripharma/api.log`) en el futuro entorno de Producción (VPS Linux) mediante perfiles de
  Logback


## [09/04/2026] 🏁 Hito de Trazabilidad y Definición de Ruta a Producción

Hoy se ha cerrado el ciclo de **Certificación de Evidencias**. Se ha implementado con éxito el sellado de tiempo (`timestamping`) para las pruebas gráficas de los nutricionistas.

* **Funcionalidad:** Tanto el Administrador como el Nutricionista disponen ahora de un panel de "Trazabilidad de la Prueba" en los visores de imágenes.
* **Integridad:** El sistema cruza la fecha de la jornada con el momento exacto de la subida a Drive, permitiendo detectar desfases temporales en los reportes.
* **Estado del Proyecto:** El Backend se considera **Feature-Complete** en un 90%. Entramos en fase de pulido y blindaje de calidad.

---

## 🚀 Plan Maestro: De Alpha a Despliegue Operativo (The "TFG" Roadmap)

Se define la ruta crítica para transformar este desarrollo en un producto de nivel Enterprise. El enfoque se divide en cuatro pilares de calidad profesional:

### Fase 1: Finalización de Features y Refactorización Estética
* **UI/UX Pro:** Implementación de notificaciones asíncronas (Toasts) para reflejar la naturaleza no bloqueante del envío de correos y PDFs.
* **Flujo de Incidencias:** Cierre del ciclo de vida de la consulta (Rechazo con motivo y reapertura de subida de fotos).
* **Código Limpio:** Refactorización de clases y scripts bajo principios SOLID. Comentado exhaustivo y generación de Javadoc para facilitar el mantenimiento por terceros.

### Fase 2: Blindaje mediante Testing (QA)
* **Backend (JUnit5 + Mockito):** Cobertura de pruebas unitarias sobre los servicios críticos (Cálculo de comisiones, lógica de saldos y reglas de negocio geográficas).
* **Frontend (Vitest / Cypress):** Pruebas de integración de componentes para asegurar que la navegación y los modales no sufran regresiones.

### Fase 3: Infraestructura y Despliegue (DevOps)
* **Provisionamiento:** Alquiler y configuración de un VPS Linux (Ubuntu Server) independiente para pruebas finales.
* **Entorno de Producción:** Configuración de Nginx como Proxy Inverso, Dockerización (opcional) y securización de la base de datos MySQL.
* **Migración de Dominio:** Implementación de subdominios (`erp.nutripharma.es`) y traspaso de propiedad de infraestructura a la empresa una vez validada la estabilidad.

### Fase 4: Entrega y Documentación Técnica
* Generación del manual de arquitectura y diccionario de datos.
* Traspaso de credenciales y claves API (Google Drive, Mailtrap/SMTP).


## [10/04/2026] 🛠️ Unificación UX y Auditoría de Identidad en Pedidos

Se ha llevado a cabo una refactorización crítica en el módulo de Pedidos para unificar la experiencia de usuario (UX) y garantizar la trazabilidad de la autoría.

* **El Súper-Modal Inteligente (Facturación Clara):** Se ha rediseñado completamente el `ModalDetallePedido` y la vista de pedidos en el `ModalDetalleValidacion`. El sistema ahora incluye un motor interno (vía `useMemo`) que agrupa dinámicamente las líneas de pedido.
  * *Mejora UX:* Las unidades compradas, bonificadas (regalo) y pagadas con saldo virtual se agrupan bajo un único producto con un desglose natural.
  * *Operativa de Almacén:* Se ha priorizado visualmente el número total de unidades por artículo (cajas grandes y oscuras) para facilitar la preparación física de los paquetes sin errores.

* **Auditoría "Identity-Aware" (Resolución del Bug Proxy):**
  Se ha solucionado un fallo de suplantación visual donde los pedidos realizados por el Administrador (Proxy) aparecían firmados por la Farmacia.
  * *Backend:* El `PedidoService` ahora cruza el email almacenado en el registro de auditoría (`creado_por`) con los repositorios de perfiles (`AdministradorRepository`, `NutricionistaRepository`, `FarmaciaRepository`) para inyectar el Nombre y Apellidos reales del operador en la respuesta JSON (`creadoPorNombre`).

* **Próximos Pasos (Roadmap Lunes):**
  Implementar el flujo de revisión obligatoria en el Gatekeeper. Bloquear las acciones directas de "Enviar/Aprobar" en las tarjetas externas, obligando al Admin a abrir el Modal de Detalles para revisar la mercancía antes de ejecutar la acción.

## [14/04/2026] 🏗️ Refactorización de Núcleo y Arquitectura de Resúmenes Inteligentes

Se ha realizado una pausa estratégica para pagar deuda técnica y unificar la lógica visual de los resúmenes de rendimiento.

* **Historial de Turnos Inteligente:** Rediseño total de `HistorialTurnos.jsx`.
  * *Lógica de Filtrado:* El componente ahora se encarga de su propio estado (meses automáticos, ordenación por pesos "Pendientes primero"). Se ha eliminado la lógica redundante del Hook para aligerar el renderizado.
  * *UX de Evidencias:* Se ha blindado la lógica de subida. Ahora, si una consulta no tiene foto (por borrado del admin o falta de subida inicial), el sistema permite re-subirla independientemente del estado "Validada".
  * *Fix Crítico de Memoria:* Se ha corregido un bug en el visor de evidencias que revocaba la URL del Blob prematuramente, impidiendo previsualizar la foto del formulario más de una vez.

* **Seguridad en Alta de Personal:** Mejora en `FormularioAdmin.jsx` y `usePersonalInterno.js`.
  * Implementación de validación de doble coincidencia de contraseña.
  * Bloqueo de "Paste" en el campo de confirmación para asegurar la verificación manual del usuario.
  * Unificación estética de iconos Lucide en todos los campos del perfil.

* **Panel de Control Nutricionista (V2):** Rediseño del `PanelCabeceraNutri`.
  * *Layout Horizontal:* Se ha pasado de una rejilla de columnas a filas apiladas para evitar el solapamiento de cifras de facturación altas (miles de euros).
  * *Selector de Historial:* Conexión con el nuevo motor de meses del Hook para permitir la consulta de rendimientos pasados, recalculando comisiones y objetivos en tiempo real.

---

## 🏁 PLAN ESTRATÉGICO: Camino a Producción (15 de Mayo)

Tras auditar el estado del proyecto (Alpha Tardía), se acuerdan las siguientes decisiones de ingeniería para cumplir los plazos de entrega del 15 de mayo.

### 🛠️ Decisiones de Infraestructura y Metodología
1.  **Metodología Spec-Driven Development (SDD):** Se utilizará un "Orquestador IA" (Gemini) para definir especificaciones técnicas en archivos `.md`, que serán ejecutadas por un "Agente de Código" (Cursor/Copilot) para asegurar la integridad del sistema.
2.  **Centralización de Estilos:** Creación de un `tailwind.config.js` corporativo y componentes atómicos en `core/components` para reducir el tamaño de los scripts (evitar componentes de +400 líneas).
3.  **Control de Versiones Estricto:** Abandono del desarrollo directo en `main`. Creación de ramas por Feature (`refactor-ui`, `feature-tests`, `deploy-config`).

### 📅 Cronograma de Hitos (Roadmap Semanal)

* **Semana 1 (14-21 Abril) - Limpieza de Cimientos:** * Refactorización del Frontend a componentes reutilizables (Botones, Inputs, Cards).
  * Unificación de la paleta de colores corporativa.
* **Semana 2 (22-28 Abril) - Optimización y Lógica:** * Afine de buscadores globales.
  * Cierre de bugs lógicos en cálculos de comisiones de farmacia.
* **Semana 3 (29 Abril - 05 Mayo) - Garantía de Calidad (QA):** * **Back:** Unit Tests con JUnit 5 y Mockito.
  * **Front:** Vitest para lógica y Playwright para el flujo crítico de usuario (End-to-End).
* **Semana 4 (06-15 Mayo) - Despliegue y Cierre:** * Dockerización del ecosistema (Spring Boot + PostgreSQL + React).
  * Configuración de VPS Linux (Ubuntu) con Nginx y SSL.
  * Documentación final para entrega (15 Mayo: App Operativa).

## [15/04/2026] 🧾 Fase 1: Cimientos del Sistema de Facturación y Gastos

Se ha implementado desde cero la primera fase del módulo de "Gastos y Kilometraje", separando conceptual y arquitectónicamente las facturas de la entidad genérica de documentos para optimizar futuras consultas SQL.

* **Arquitectura Backend (Subida Automática):** * Creación de la entidad `FacturaGasto` vinculada directamente a la `Nutricionista`.
  * Integración con `GoogleDriveService` para implementar un renombrado automático estandarizado (`dd-MM-yyyy_Km_Nombre_Apellidos.ext`), evitando errores humanos en la nomenclatura de archivos.
* **Interfaz de Usuario y Seguridad (Frontend):**
  * Creación de un servicio independiente (`facturasService.js`) y vistas de subida modularizadas.
  * Implementación de desplegables paramétricos para seleccionar el mes correspondiente, evitando errores tipográficos ("Abril" vs "abril").
  * Integración de un sistema de borrado para el Administrador protegido por una barrera de seguridad nativa (`window.confirm`) para prevenir eliminaciones accidentales en la nube.

---

## [16/04/2026] 📅 Fase 2 y 3: Calendario Escalable y Súper-Dashboard de Auditoría

Se ha abordado la refactorización visual y lógica del módulo de Inteligencia de Negocio del Administrador, aplicando patrones de diseño de ERPs modernos para gestionar la densidad de información.

* **Calendario Operativo (Divulgación Progresiva):**
  * *Rediseño Responsive:* Se han configurado 3 escenarios de visualización mediante Tailwind (Desktop con badges completos, Tablet con vista compacta, y Móvil con indicadores de puntos tipo iOS).
  * *Navegación por Capas (Progressive Disclosure):* Eliminación de la saturación visual. El flujo ahora es: **Nivel 1** (Mes global) -> **Nivel 2** (Modal de Resumen Diario con listas limpias) -> **Nivel 3** (Modal de Detalle Completo).
  * *Prevención de "Data Starvation" (Fetch on Demand):* Refactorización crítica de la conexión Back-Front. Los Niveles 1 y 2 cargan DTOs ultraligeros, ejecutando peticiones asíncronas de datos profundos (`dashboardService.obtenerConsultaDetalle`) *solo* al acceder al Nivel 3.
  * *Reutilización de Componentes:* Adaptación de `ModalDetalleValidacion.jsx` para inyectarle una prop `modoLectura`, permitiendo reutilizar la vista desde el calendario bloqueando cualquier edición accidental.

* **Panel de Auditoría Avanzada (Súper-Dashboard):**
  * *Separación de Responsabilidades:* Creación de `AdminAuditoriaPanel.jsx`. Se ha separado la validación operativa (Documentación) del análisis métrico (Dashboard) por petición expresa de negocio.
  * *Cruce de Kilometraje:* Implementación de una tarjeta analítica premium que cruza de forma automática las "Consultas Validadas" con los "Km Totales" calculados por el sistema, listo para contrastar con la factura de la Fase 1.
  * *Filtro Temporal Dinámico:* Creación de un endpoint puente en el backend (`/auditoria/{id}/meses`) que informa al frontend de los meses exactos en los que ha trabajado una nutricionista, evitando que el administrador busque datos en meses vacíos.
  * *Fix Visual:* Corrección del renderizado condicional de strings para ocultar los paréntesis vacíos `()` en nutricionistas sin email configurado.
  ## [2026-04-17] - Refactorización de Arquitectura e-commerce, Filtros en Cascada y UX de Validaciones

### 🛒 E-commerce: Catálogo y Ordenación Avanzada
* **Arquitectura de Merchandising:** Se rediseñó la lógica de ordenación del catálogo para separar estrictamente el **Orden por Defecto** (curación manual y estratégica del negocio) de los **Recomendados** (motor heurístico de personalización basado en el volumen histórico de compras de cada farmacia).
* **Filtros Extendidos:** Se añadieron nuevos filtros al frontend (`CatalogoProductos.jsx` y `usePedidos.js`): Top Ventas Global (mediante consulta de agregación SQL en el Backend), Alfabético (A-Z, Z-A) y ordenación por Precio.
* **Hotfix UI:** Corrección de un error de renderizado en el contador de unidades de las tarjetas de producto (ajuste de clases Tailwind de `1px` a `10px`).

### 📊 Dashboard Admin: Filtros Dependientes (Cascada)
* **UX/UI en Gráficas:** Implementación de lógica reactiva (`useMemo`) en `useResumenAdmin.js` para los selectores de facturación. Al seleccionar una Nutricionista, el listado de Farmacias se auto-filtra para mostrar únicamente las de su ruta, y viceversa, evitando cruces de datos inválidos sin sobrecargar el servidor.

### ⚙️ Panel de Administración: Modo Edición Absoluta
* **Control Total (Modo Dios):** Reescritura del `ModalEdicionAdministracion.jsx` para replicar el formulario de creación. Se actualizaron los DTOs y Servicios en el Backend (`NutricionistaService`, `FarmaciaService`, `ProductoService`) para permitir la edición de campos críticos post-creación, como correos electrónicos, contraseñas, comisiones y referencias/SKU.

### 🕵️‍♀️ Validaciones: Separación de Concerns (QA vs Contabilidad)
* **Refactorización de Flujo de Trabajo:** Tras un análisis de negocio, se separó el concepto de "Validación" en dos procesos distintos: Control de Calidad visual y Cuadre Contable.
* **Modo Enfoque (Mazo de Cartas):** Desarrollo de un sistema de navegación secuencial en `ModalDetalleValidacion.jsx` y `useValidaciones.js`. Permite iterar entre las tarjetas pendientes de validación con auto-avance, evitando abrir y cerrar modales constantemente.
* **Motor Contable Backend:** Adición del nuevo estado `LIQUIDADA` en la entidad `Consulta` y creación de un endpoint de actualización en lote (`Bulk Update`) para gestionar el cierre de caja.

### ⏭️ Próximos Pasos (Siguiente Sesión)
* Creación e integración de `PanelLiquidacion.jsx`: Interfaz de pantalla dividida (Tabla de selección múltiple + Carrito de cobro dinámico) para procesar las consultas en estado `VALIDADA`.

## [20/04/2026] - Cierre de Caja, Trazabilidad Histórica y Edición Retroactiva

### 💰 Módulo Financiero: Panel de Liquidación (Cierre de Caja)
* **Creación de `PanelLiquidacion.jsx`:** Implementación de una interfaz de pantalla dividida orientada a contabilidad. A la izquierda, una tabla interactiva con selección múltiple de consultas en estado `VALIDADA`. A la derecha, un panel de resumen dinámico que calcula en tiempo real el importe a liquidar según las tarifas base (Nuevas y Revisiones).
* **Filtros y Ordenación:** Adición de selectores reactivos por Nutricionista, Mes y un motor de ordenación cuádruple (Fechas y Volumen de Comisión) para facilitar el arqueo de caja.
* **Integración de Sub-pestañas:** Reestructuración de `VistaValidaciones.jsx` para bifurcar las operaciones de validación (Control de Calidad) y liquidación (Cierre Financiero).

### 🗄️ Base de Datos y Trazabilidad (Single Source of Truth)
* **Resolución de Esquema SQL:** Solución al error de truncamiento (`Data truncated for column 'estado'`) ejecutando un `ALTER TABLE` para ampliar la columna `estado` a `VARCHAR(50)` en la tabla principal `consultas` y en la tabla de auditoría `consultas_aud` (Hibernate Envers).
* **Historial Absoluto:** Modificación de `useValidaciones.js` para cargar la totalidad de los registros operativos en la vista de Historial (eliminando el filtrado restrictivo previo), centralizando así la auditoría global de la plataforma.
* **Filtros de Historial:** Incorporación de parámetros de ordenación para los estados "Liquidados" y "Pendientes", y aplicación de la paleta corporativa (verde/lima) a los badges de estado final.

### 🛡️ Seguridad: Edición Forzada Retroactiva
* **Manejo de Errores Consolidados:** Implementación de un "Modo Edición Avanzada" en `ModalDetalleValidacion.jsx`. Permite al Administrador corregir métricas financieras de consultas que ya han sido validadas antes de ejecutar la liquidación final. La acción requiere confirmación explícita (alerta de seguridad) y queda registrada inmutablemente mediante el sistema de auditoría de Envers.

### ⏭️ Próximos Pasos (Siguiente Sesión)
* Desarrollo del **Generador de Informes**: Sistema de exportación y visualización de métricas estructuradas utilizando los datos consolidados en los resúmenes operativos.

## [21/04/2026] 🏗️ Sistema de Diseño Global y Arquitectura Domain-Driven Modular

### 🎨 Construcción del "Kit" de UI (Shadcn + Tailwind v4)
* **Estandarización Visual:** Se ha finalizado la construcción de una librería de componentes UI propios en `src/shared/components/ui/`, basada en las primitivas de Radix UI y Shadcn.
* **Componentes Aislados:** Se tradujeron e integraron componentes complejos (DataTables, Selects nativos y custom, Sliders, Toggle Groups, Toasters) asegurando que consumen estrictamente las variables CSS corporativas (`--color-primary`, `--color-surface`, etc.) sin inyectar estilos ajenos o colores hardcodeados.
* **Playground de Pruebas:** Creación de un `Playground.jsx` (Sandbox) para validar el comportamiento, accesibilidad y diseño de todos los componentes de forma aislada antes de inyectarlos en la aplicación principal.

### 🏛️ Debate Arquitectónico: FSD vs Domain-Driven Modular
* **El Problema:** El frontend había crecido orgánicamente hasta convertirse en un "Frankenstein", mezclando llamadas a la API, lógica de negocio y renderizado UI en las mismas carpetas. Se necesitaba una arquitectura para escalar el ERP como SaaS/VPS.
* **Evaluación de Feature-Sliced Design (FSD):** Se planteó migrar al estándar FSD (separando por *entities*, *features*, *widgets* y *pages*). Sin embargo, se detectó que esta abstracción creaba un "Impedance Mismatch" (desajuste de modelos) con el backend.
* **La Decisión (Domain-Driven Modular):** Se descartó el FSD puro en favor de una **Arquitectura Modular orientada al Dominio**. El frontend ahora será un espejo exacto del backend en Spring Boot (`security`, `operations`, `organization`, `sales`, `documents`).
* **Reglas de Seguridad Implementadas:** 1. **Alta Cohesión:** Servicios, hooks, componentes y páginas de un mismo dominio vivirán juntos en su respectiva carpeta modular.
  2. **API Pública (Indexación):** Para evitar el código espagueti y las dependencias circulares, los módulos solo se comunicarán entre sí exponiendo estrictamente lo necesario a través de archivos `index.js`.
  3. **Router Centralizado:** La navegación no estará dispersa; se orquestará globalmente desde la capa `app/`.

### ⏭️ Próximos Pasos (Siguiente Sesión)
* Creación de las carpetas modulares vacías en el frontend.
* Reubicación progresiva de la lógica existente (`services` y `hooks`) a sus nuevos módulos.
* Inicio del rediseño quirúrgico UI asistido por IA, sustituyendo etiquetas HTML estándar por los nuevos componentes atómicos de `shared/ui/` sin alterar la lógica de negocio subyacente.

# [24/04/2026] 🎨 Cirugía UI: Migración a Shadcn, Tailwind v4 y "La Vacuna" Global

Con la arquitectura modular ya definida, nos centramos en modernizar la capa visual utilizando nuestro propio kit de componentes basado en Shadcn y Radix UI, enfrentándonos a los retos de compatibilidad con Tailwind v4.

---

## 🛠️ El Desafío del Sidebar y Tailwind v4

### El Problema del Móvil
El panel lateral en su versión móvil (`SheetContent`) se resistía a adoptar las variables CSS corporativas y sus dimensiones colapsadas, debido a que Tailwind v4 procesa la precedencia de clases de forma distinta a la v3.

### Solución Quirúrgica
<!-- "Se forzaron las reglas CSS directamente en las clases para someter al componente interno" -->
Se aplicaron reglas CSS con mayor especificidad directamente sobre las clases (`!bg-background !w-[var(--sidebar-width)]`) para alinear el comportamiento del componente en móvil y escritorio.

### Restauración de Animaciones
Al eliminar plugins antiguos de Tailwind, las animaciones nativas de Radix (modales, cajones deslizables) dejaron de funcionar. Se resolvió inyectando `@keyframes` puros (`slide-in-left`, `dialog-in`) directamente en el `@layer utilities` del `index.css`.

---

## 💉 La "Vacuna" Global CSS

<!-- "Para evitar tener que refactorizar las animaciones componente a componente" -->
Con el objetivo de no tener que intervenir en cada componente individualmente (Dropdowns, Selects, Accordions, Dialogs), se implementó una solución global en el `index.css`.

### Mecanismo
El CSS intercepta globalmente los atributos `[data-state="open"]` y `[data-state="closed"]` de Radix UI, inyectando las animaciones de transición (`pop-in`, `fade-in`) de forma automática a todo el ecosistema de componentes.

> ✅ Esto validó con éxito todo el **Nivel Verde** de componentes en el `Playground.jsx`.

---

## 🤖 Metodología de Migración UI — El "Master Prompt"

Para agilizar la migración visual de las vistas sin comprometer los hooks, estados ni peticiones Axios, se diseñó una metodología de ingeniería asistida por IA:

<!-- "Se estableció un Master Prompt con reglas inquebrantables de Cero Lógica" -->
Se definió un prompt de referencia con una regla central: **ninguna lógica de negocio debe ser modificada**.

### Objetivo
Realizar una sustitución uno-a-uno de etiquetas HTML estándar (`<div>`, `<input>`, `<button>`) por los componentes atómicos del ecosistema (`<Card>`, `<Field>`, `<Input>`, `<Button>`), manteniendo intactos los `onSubmit`, `onChange` y validaciones.

---

## 🔐 Refactorización del Módulo de Seguridad (Auth)

Se aplicó la migración UI a `Login.jsx` y `ResetPassword.jsx`.

### Branding
Sustitución de fondos neutros por el gradiente corporativo e inyección del logotipo en un contenedor tipo `<Card>` elevado.

### El Bug del "Ojo Fantasma"
Al usar el `<Input type="password">` de Shadcn, los navegadores (Edge, Chrome) inyectaban su propio botón nativo de revelado de contraseña al teclear, duplicándose con el icono de Lucide.

### Solución CSS
<!-- "Se añadió una regla global para destruir la interfaz nativa del navegador" -->
Se añadió una regla CSS global que suprime los controles nativos del navegador para campos de contraseña:

```css
::-ms-reveal,
::-webkit-credentials-auto-fill-button {
  display: none !important;
}
```

Esto delega el control visual al 100% al componente de React.

---

## 🗂️ Migración del Dominio de Documentos

Aplicación exitosa del **Master Prompt v2** (orientado a vistas complejas) en el módulo de facturación y gastos.

### Componentes Migrados
- `DocumentacionPage` y sus Anexos
- `PanelSubidaDocumentos`
- `SubidaFacturas`
- `ListadoFacturas`

### Componentes Complejos
Integración de los `<Select>` paramétricos anidados y estilización avanzada de los inputs de subida de archivos (`<Input type="file">`) utilizando pseudo-clases de Tailwind (`file:bg-secondary`).

> ✅ La lógica de `FormData` y `window.confirm` permaneció inalterada, demostrando la eficacia de la **Separación de Responsabilidades (SoC)**.