# Arquitectura del Sistema - TrainHub

Este documento describe la arquitectura general del sistema TrainHub, incluyendo la estructura de capas, módulos, y flujos de comunicación, basado en el esquema real de `dbdiagram.sql`.

## 1. Arquitectura de Alto Nivel

### 1.1 Visión General

TrainHub sigue una **arquitectura monolítica modular** con separación clara entre frontend y backend:

```
┌─────────────────────────────────────────────────────────┐
│                Frontend (Angular 20 SPA)                 │
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

- **Frontend**: Angular 20 (standalone, SPA)
- **Estado**: Signals + servicios; sin NgRx (MVP)
- **Build**: Angular CLI con Vite sin SSR, SCSS
- **Backend**: Spring Boot 3.5.5 (Java 17)
- **Base de Datos**: PostgreSQL 17
- **ORM**: Spring Data JPA (Hibernate)
- **Autenticación**: JWT (JSON Web Tokens)
- **Comunicación en Tiempo Real**: WebSockets (STOMP o JSON)


### 1.3 **patrón de arquitectura en capas (Layered Architecture)**:

#### **Capa de Presentación** (Frontend Angular)
   - Features/páginas standalone
   - Componentes shared
   - Estado con Signals en servicios
   - Llamadas a API vía HttpClient + interceptores

#### Controller Layer
- **Responsabilidades**:
  - Recibir requests HTTP
  - Validar formato de entrada (DTOs)
  - Invocar servicios
  - Retornar respuestas HTTP
  - Manejo básico de excepciones HTTP

#### Service Layer
- **Responsabilidades**:
  - Lógica de negocio
  - Validaciones de reglas de negocio
  - Orquestación de operaciones complejas
  - Transacciones
  - Invocación de múltiples repositorios

#### Repository Layer
- **Responsabilidades**:
  - Acceso a datos
  - Consultas a base de datos
  - Mapeo objeto-relacional
  - Operaciones CRUD básicas
  
---

## 2. Estructura de Módulos Backend

### 2.1 Organización de Paquetes

```
com.trainhub.backend
│
├─ config/              // Configuración Spring (Security, CORS, JWT, WebSocket)
├─ security/            // JWT utilities, Security filters, Authentication
│  ├─ jwt/
│  ├─ filters/
│  └─ services/
│
├─ controller/          // REST Controllers por dominio
│  ├─ auth/
│  ├─ user/
│  ├─ post/
│  ├─ chat/
│  └─ search/
│
├─ service/             // Lógica de negocio
│  ├─ auth/
│  ├─ user/
│  ├─ post/
│  ├─ chat/
│  └─ friendship/
│
├─ repository/          // Spring Data JPA Repositories
│
├─ model/               // Entidades JPA (mapeo a BD)
│
├─ dto/                 // Data Transfer Objects (request/response)
│  ├─ request/
│  └─ response/
│
└─ exception/           // Excepciones personalizadas y handlers
```
### 2.2 Módulos Funcionales

1. **Módulo de Autenticación** (`auth`): Registro, login, refresh token, logout
2. **Módulo de Usuario** (`user`): Perfil, edición, búsqueda
3. **Módulo de Publicaciones** (`post`): Creación, feed, likes
4. **Módulo de Chat** (`chat`): Mensajería, WebSockets
5. **Módulo de Amistades** (`friendship`): Gestión de relaciones

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



## 8. Referencias

- [Database_Design.md](Database_Design.md) - Diseño detallado de la base de datos (basado en dbdiagram.sql)
- [API_Design.md](API_Design.md) - Especificación de endpoints REST
- [Security_Design.md](Security_Design.md) - Medidas de seguridad
- [Funcionalidades.md](Funcionalidades.md) - Requisitos funcionales
- `dbdiagram.sql` - Esquema fuente de la base de datos
