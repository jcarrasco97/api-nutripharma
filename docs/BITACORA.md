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

*Estado actual: Flujo de Autenticación JWT completo y Dashboard Dinámico basado en roles finalizado. Siguiente paso: Desarrollo de la lógica de negocio (Backend Controllers).*