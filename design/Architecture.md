# Arquitectura del Sistema - TrainHub

Este documento describe la arquitectura general del sistema TrainHub, incluyendo la estructura de capas, módulos, y flujos de comunicación, basado en el esquema real de `dbdiagram.sql`.

## 1. Arquitectura de Alto Nivel

### 1.1 Visión General

TrainHub sigue una **arquitectura monolítica modular** con separación clara entre frontend y backend:

```
┌─────────────────────────────────────────────────────────┐
│                Frontend (Angular 17 SPA)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ Features │  │ Shared   │  │ Services │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│       │              │              │                    │
│       └──────────────┼──────────────┘                    │
│                      │                                   │
│              ┌────────▼────────┐                         │
│              │  HttpClient     │                         │
│              │  WebSocket/RxJS │                         │
│              └────────┬────────┘                         │
└───────────────────────┼──────────────────────────────────┘
                        │
                        │ HTTP/REST + WebSocket
                        │
┌───────────────────────▼──────────────────────────────────┐
│              Backend (Spring Boot)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │Controller│  │ Service   │  │Repository│             │
│  └──────────┘  └──────────┘  └──────────┘             │
│       │              │              │                    │
│       └──────────────┼──────────────┘                    │
│                      │                                   │
│              ┌───────▼────────┐                          │
│              │  Spring Data   │                          │
│              │      JPA       │                          │
│              └───────┬────────┘                          │
└──────────────────────┼───────────────────────────────────┘
                       │
                       │ JDBC
                       │
┌──────────────────────▼───────────────────────────────────┐
│            PostgreSQL Database                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  Users   │  │  Posts    │  │ Messages │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└──────────────────────────────────────────────────────────┘
```

### 1.2 Tecnologías Principales

- **Frontend**: Angular 17 (standalone, SPA)
- **Estado**: Signals + servicios; sin NgRx (MVP)
- **Build**: Angular CLI con Vite/SSR, SCSS
- **Backend**: Spring Boot 3.5.5 (Java 17)
- **Base de Datos**: PostgreSQL 17
- **ORM**: Spring Data JPA (Hibernate)
- **Autenticación**: JWT (JSON Web Tokens)
- **Comunicación en Tiempo Real**: WebSockets (STOMP o JSON)
- **Documentación API**: SpringDoc OpenAPI (Swagger)

### 1.3 Patrón Arquitectónico

El sistema sigue el **patrón de arquitectura en capas (Layered Architecture)**:

1. **Capa de Presentación** (Frontend Angular)
   - Features/páginas standalone
   - Componentes shared
   - Estado con Signals en servicios
   - Llamadas a API vía HttpClient + interceptores

2. **Capa de Controladores** (Spring Boot Controllers)
   - Endpoints REST
   - Validación de entrada
   - Manejo de excepciones HTTP

3. **Capa de Servicios** (Business Logic)
   - Lógica de negocio
   - Validaciones de reglas de negocio
   - Orquestación de operaciones

4. **Capa de Persistencia** (Repositories)
   - Acceso a datos
   - Consultas a base de datos
   - Mapeo objeto-relacional

5. **Capa de Datos** (PostgreSQL)
   - Almacenamiento persistente
   - Integridad referencial
   - Transacciones

## 2. Estructura de Módulos Backend

### 2.1 Organización de Paquetes

```
com.trainhub.backend
│
├─ config/                    // Configuración Spring
│  ├─ SecurityConfig          // Spring Security, JWT filters
│  ├─ CorsConfig              // Configuración CORS
│  ├─ WebSocketConfig        // Configuración WebSocket
│  └─ JwtConfig               // Configuración JWT
│
├─ security/                  // Seguridad y autenticación
│  ├─ jwt/
│  │  ├─ JwtTokenProvider     // Generación y validación de tokens
│  │  └─ JwtAuthenticationFilter  // Filtro de autenticación
│  ├─ filters/
│  │  └─ JwtAuthenticationFilter
│  └─ services/
│     └─ UserDetailsServiceImpl
│
├─ controller/                 // REST Controllers
│  ├─ auth/
│  │  └─ AuthController       // Registro, login, refresh, logout
│  ├─ user/
│  │  └─ UserController        // CRUD de usuarios, perfil
│  ├─ post/
│  │  └─ PostController        // Publicaciones, feed, likes
│  ├─ chat/
│  │  └─ ChatController        // Mensajes REST
│  ├─ search/
│  │  └─ SearchController      // Búsqueda de usuarios
│  └─ friendship/
│     └─ FriendshipController  // Gestión de amistades
│
├─ service/                    // Lógica de negocio
│  ├─ auth/
│  │  ├─ AuthService          // Lógica de autenticación
│  │  └─ EmailVerificationService
│  ├─ user/
│  │  └─ UserService          // Gestión de usuarios
│  ├─ post/
│  │  └─ PostService          // Gestión de publicaciones
│  ├─ chat/
│  │  └─ ChatService          // Gestión de mensajes
│  └─ friendship/
│     └─ FriendshipService     // Gestión de amistades
│
├─ repository/                 // Spring Data JPA Repositories
│  ├─ UserRepository
│  ├─ PostRepository
│  ├─ MessageRepository
│  ├─ FriendshipRepository
│  ├─ LikeRepository          // PK compuesta (user_id, post_id)
│  └─ RefreshTokenRepository  // PK: token_hash
│
├─ model/                      // Entidades JPA (mapeo a BD)
│  ├─ User                     // password_hash, photo_url, registration_date
│  ├─ Post                     // Tiempos como columnas: r1_time...r8_time, w1_time...w8_time
│  ├─ Message                  // sender_id, receiver_id, read_at, status
│  ├─ Friendship               // user_a_id, user_b_id, status (FRIEND)
│  ├─ Like                     // PK compuesta (user_id, post_id)
│  └─ RefreshToken            // PK: token_hash
│
├─ dto/                        // Data Transfer Objects
│  ├─ request/
│  │  ├─ RegisterRequest
│  │  ├─ LoginRequest
│  │  ├─ CreatePostRequest
│  │  └─ SendMessageRequest
│  └─ response/
│     ├─ AuthResponse
│     ├─ UserResponse
│     ├─ PostResponse
│     └─ MessageResponse
│
└─ exception/                  // Manejo de excepciones
   ├─ GlobalExceptionHandler
   ├─ ResourceNotFoundException
   ├─ BadRequestException
   └─ UnauthorizedException
```

### 2.2 Módulos Funcionales

#### 2.2.1 Módulo de Autenticación (`auth`)
- **Responsabilidades**:
  - Registro de nuevos usuarios
  - Inicio de sesión con JWT
  - Refresco de tokens
  - Cierre de sesión
  - Verificación de email
- **Componentes principales**:
  - `AuthController`, `AuthService`
  - `JwtTokenProvider`, `JwtAuthenticationFilter`
  - `EmailVerificationService`
- **Entidades**: `User`, `RefreshToken` (PK: token_hash)

#### 2.2.2 Módulo de Usuario (`user`)
- **Responsabilidades**:
  - Gestión de perfiles de usuario
  - Edición de información personal
  - Subida/eliminación de avatares (photo_url)
  - Visualización de perfiles públicos
- **Componentes principales**:
  - `UserController`, `UserService`
  - `UserRepository`
- **Entidades**: `User` (password_hash, photo_url, registration_date, last_login)

#### 2.2.3 Módulo de Publicaciones (`post`)
- **Responsabilidades**:
  - Creación de publicaciones de entrenamiento
  - Feed de publicaciones de amigos
  - Sistema de likes
  - Eliminación de publicaciones
- **Componentes principales**:
  - `PostController`, `PostService`
  - `PostRepository`, `LikeRepository`
- **Entidades**: `Post` (tiempos como columnas: r1_time...r8_time, w1_time...w8_time, total_time), `Like` (PK compuesta)

#### 2.2.4 Módulo de Chat (`chat`)
- **Responsabilidades**:
  - Envío y recepción de mensajes
  - Gestión de conversaciones (derivadas de pares de usuarios)
  - Comunicación en tiempo real (WebSocket)
  - Estados de mensajes (enviado, entregado, leído)
- **Componentes principales**:
  - `ChatController`, `ChatService`
  - `MessageRepository`
  - `WebSocketConfig`, `ChatWebSocketHandler`
- **Entidades**: `Message` (sender_id, receiver_id, read_at, status)
- **Nota**: No hay tabla de conversaciones; se derivan agrupando mensajes por pares de usuarios

#### 2.2.5 Módulo de Amistades (`friendship`)
- **Responsabilidades**:
  - Envío de solicitudes de amistad
  - Aceptación/rechazo de solicitudes
  - Listado de amigos
  - Verificación de relaciones de amistad
- **Componentes principales**:
  - `FriendshipController`, `FriendshipService`
  - `FriendshipRepository`
- **Entidades**: `Friendship` (user_a_id, user_b_id, status: FRIEND/PENDING/BLOCKED, PK compuesta)

## 3. Flujos de Comunicación

### 3.1 Flujo de Autenticación

```
Usuario → Frontend → POST /api/auth/login
                          ↓
                    AuthController
                          ↓
                    AuthService (valida credenciales con password_hash)
                          ↓
                    JwtTokenProvider (genera tokens)
                          ↓
                    RefreshTokenRepository.save() (token_hash como PK)
                          ↓
                    Response: { accessToken, user }
                          ↓
                    Frontend almacena token en Context
                          ↓
                    Requests posteriores: Authorization: Bearer <token>
```

### 3.2 Flujo de Creación de Publicación

```
Usuario → CreatePostWizard (multi-paso)
              ↓
         Frontend valida datos
              ↓
         POST /api/posts
              ↓
         PostController
              ↓
         PostService (valida reglas de negocio)
              ↓
         PostRepository.save() (tiempos como columnas: r1_time...r8_time, w1_time...w8_time, total_time)
              ↓
         Response: PostResponse
              ↓
         Frontend redirige a Feed/Profile
```

### 3.3 Flujo de Chat en Tiempo Real

```
Usuario → ChatWindow
              ↓
         Conexión WebSocket: /ws/chat?token=<jwt>
              ↓
         WebSocketConfig autentica conexión
              ↓
         ChatWebSocketHandler maneja mensajes
              ↓
         ChatService procesa:
           - Persiste Message (sender_id, receiver_id)
           - Las conversaciones se derivan agrupando mensajes por pares de usuarios
              ↓
         Broadcast a destinatario (si está conectado)
              ↓
         Frontend actualiza UI en tiempo real
```

## 4. Separación de Responsabilidades

### 4.1 Controller Layer
- **Responsabilidades**:
  - Recibir requests HTTP
  - Validar formato de entrada (DTOs)
  - Invocar servicios
  - Retornar respuestas HTTP
  - Manejo básico de excepciones HTTP

### 4.2 Service Layer
- **Responsabilidades**:
  - Lógica de negocio
  - Validaciones de reglas de negocio
  - Orquestación de operaciones complejas
  - Transacciones
  - Invocación de múltiples repositorios

### 4.3 Repository Layer
- **Responsabilidades**:
  - Acceso a datos
  - Consultas a base de datos
  - Mapeo objeto-relacional
  - Operaciones CRUD básicas

## 5. Consideraciones de Escalabilidad

### 5.1 Monolito Modular
- El sistema está diseñado como un monolito modular, lo que permite:
  - Fácil desarrollo y despliegue inicial
  - Separación clara de responsabilidades
  - Posible migración futura a microservicios si es necesario

### 5.2 Optimizaciones Actuales
- **Diseño denormalizado en Posts**: Tiempos almacenados como columnas para evitar JOINs
- Índices estratégicos en base de datos para consultas frecuentes
- Índices compuestos para consultas complejas (feed, conversaciones)
- Paginación en endpoints que retornan listas
- Cache en frontend (React Context, opcionalmente React Query)
- Lazy loading de imágenes y contenido pesado

### 5.3 Posibles Mejoras Futuras
- Cache en backend (Redis) para sesiones y datos frecuentes
- CDN para imágenes y assets estáticos
- Separación de lectura/escritura en base de datos
- Microservicios para funcionalidades específicas (chat, notificaciones)
- Particionamiento de tablas grandes (posts, messages) por fecha

## 6. Integración con Base de Datos

### 6.1 Estrategia de DDL
- **Modo**: `spring.jpa.hibernate.ddl-auto: update` (desarrollo)
- Hibernate actualiza automáticamente el esquema basado en las entidades
- **Nota**: En producción, usar migraciones explícitas (Flyway/Liquibase)

### 6.2 Transacciones
- Transacciones gestionadas por Spring (`@Transactional`)
- Nivel de aislamiento por defecto: READ_COMMITTED
- Transacciones en capa de servicio para operaciones complejas

### 6.3 Consultas Optimizadas
- **Índices compuestos críticos**:
  - `(conversation_id, send_date DESC)` en Messages para paginación eficiente
  - `(user_id, creation_date DESC)` en Posts para feed de usuario
  - `(user_a_id, status)` y `(user_b_id, status)` en Friendships para búsqueda bidireccional
- **Índices parciales**: En campos filtrados frecuentemente (status = 'FRIEND', revoked = false)
- Consultas nativas para casos complejos (feed, conversaciones)
- Paginación para evitar cargar grandes volúmenes de datos

### 6.4 Diseño Denormalizado
- **Posts**: Tiempos almacenados como columnas (r1_time...r8_time, w1_time...w8_time)
  - **Ventaja**: Reduce JOINs, consultas más rápidas para feed
  - **Trade-off**: Menos flexibilidad si cambia el número de pruebas
  - **Adecuado para MVP**: Mejor rendimiento con estructura fija

## 7. Optimizaciones de Rendimiento

### 7.1 Base de Datos
- **Diseño denormalizado en Posts**: Tiempos como columnas en lugar de tablas relacionadas
- **Claves primarias compuestas**: Likes, Friendships
  - Evitan índices adicionales
  - Garantizan unicidad a nivel de BD
- **Índices compuestos estratégicos**: Para consultas frecuentes (feed y mensajes por par de usuarios)
- **Limpieza automática**: Job programado para tokens expirados

### 7.2 Backend
- Paginación en todos los endpoints de listado
- Cache de consultas frecuentes (opcional, Redis)
- Lazy loading en relaciones JPA cuando sea apropiado
- Consultas nativas optimizadas para feed y conversaciones

### 7.3 Frontend
- Lazy loading de componentes y rutas
- Paginación infinita en feed y mensajes
- Memoización de componentes pesados
- Debounce en búsquedas (300-500ms)

## 8. Referencias

- [Database_Design.md](Database_Design.md) - Diseño detallado de la base de datos (basado en dbdiagram.sql)
- [API_Design.md](API_Design.md) - Especificación de endpoints REST
- [Security_Design.md](Security_Design.md) - Medidas de seguridad
- [Funcionalidades.md](Funcionalidades.md) - Requisitos funcionales
- `dbdiagram.sql` - Esquema fuente de la base de datos
