---
name: Diseño Software TrainHub TFG
overview: "Plan de diseño completo para TrainHub: arquitectura, estructura de módulos, API REST, componentes React y flujos principales, con nivel medio de detalle apropiado para un TFG."
todos: []
---

# Plan de Diseño - TrainHub TFG

## 1. Arquitectura General del Sistema

### 1.1 Arquitectura de Alto Nivel

- **Arquitectura**: Monolítica modular (backend) + SPA (frontend React)
- **Comunicación**: REST API + WebSockets para chat en tiempo real
- **Base de Datos**: PostgreSQL 17 con Spring Data JPA
- **Autenticación**: JWT (access token + refresh token en cookies HttpOnly)

### 1.2 Separación de Capas

```
Frontend (React) ←→ REST API ←→ Backend (Spring Boot)
                           ↓
                    PostgreSQL Database
```

## 2. Estructura de Módulos Backend

### 2.1 Estructura de Paquetes Java

Basado en [trainhub/planificacion/Estructura.md](trainhub/planificacion/Estructura.md), organizar en:

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

## 3. Diseño de API REST

### 3.1 Endpoints Principales

#### Autenticación (`/api/auth`)

- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Inicio de sesión (retorna access token)
- `POST /api/auth/refresh` - Refrescar access token
- `POST /api/auth/logout` - Cerrar sesión
- `GET /api/auth/verify-email?token={token}` - Verificar email

#### Usuario (`/api/users`)

- `GET /api/users/me` - Perfil del usuario autenticado
- `PUT /api/users/me` - Actualizar perfil
- `POST /api/users/me/avatar` - Subir foto de perfil
- `DELETE /api/users/me/avatar` - Eliminar foto de perfil
- `GET /api/users/{id}` - Perfil público de otro usuario

#### Publicaciones (`/api/posts`)

- `POST /api/posts` - Crear publicación
- `GET /api/posts/feed` - Feed de publicaciones (paginado)
- `GET /api/posts/{id}` - Obtener publicación específica
- `DELETE /api/posts/{id}` - Eliminar publicación
- `POST /api/posts/{id}/like` - Dar/quitar like
- `GET /api/posts/{id}/likes` - Lista de usuarios que dieron like

#### Chat (`/api/chat`)

- `GET /api/chat/conversations` - Lista de conversaciones
- `GET /api/chat/conversations/{userId}/messages` - Mensajes de conversación (paginado)
- `POST /api/chat/messages` - Enviar mensaje
- `PUT /api/chat/messages/{id}/read` - Marcar como leído
- WebSocket: `/ws/chat` - Conexión para mensajes en tiempo real

#### Búsqueda (`/api/search`)

- `GET /api/search/users?q={query}` - Búsqueda de usuarios

#### Amistades (`/api/friendships`)

- `POST /api/friendships/request` - Enviar solicitud de amistad
- `PUT /api/friendships/{id}/accept` - Aceptar solicitud
- `GET /api/friendships` - Lista de amigos

### 3.2 Convenciones API

- Prefijo base: `/api`
- Autenticación: Header `Authorization: Bearer <accessToken>`
- Respuestas JSON consistentes
- Códigos HTTP estándar (200, 201, 400, 401, 403, 404, 429, 500)
- Paginación: `?page=0&size=20`

## 4. Diseño Frontend (React)

### 4.1 Estructura de Carpetas

```
src/
├─ components/          // Componentes reutilizables
│  ├─ common/          // Botones, inputs, modals
│  ├─ layout/          // Navbar, Footer, Layout
│  └─ post/            // Componentes de publicaciones
│
├─ pages/              // Páginas/Views principales
│  ├─ Login/
│  ├─ Register/
│  ├─ Feed/
│  ├─ Profile/
│  ├─ CreatePost/
│  └─ Chat/
│
├─ services/           // API clients, WebSocket
│  ├─ api/
│  └─ websocket/
│
├─ context/            // React Context (Auth, Theme)
├─ hooks/              // Custom hooks
├─ utils/              // Utilidades, formatters
└─ styles/             // CSS/SCSS globales
```

### 4.2 Componentes Principales

- **Navbar**: Logo, búsqueda, navegación, perfil
- **PostCard**: Publicación con deslizamiento horizontal (overview/running/workouts)
- **ChatWindow**: Interfaz de chat con lista de conversaciones
- **UserSearch**: Dropdown de búsqueda de usuarios
- **CreatePostWizard**: Formulario multi-paso (running → workouts → total)

### 4.3 Gestión de Estado

- **React Context** para autenticación (usuario, token)
- **Estado local** para formularios y UI
- **React Query/SWR** (opcional) para cache de datos del servidor

## 5. Flujos de Datos Principales

### 5.1 Flujo de Autenticación

1. Usuario → Login → Backend valida → Retorna access token
2. Frontend almacena token en memoria/context
3. Cada request incluye `Authorization: Bearer <token>`
4. Si token expira (401) → Frontend llama `/auth/refresh`
5. Refresh token en cookie HttpOnly se usa automáticamente

### 5.2 Flujo de Creación de Publicación

1. Usuario completa formulario multi-paso
2. Frontend valida datos localmente
3. `POST /api/posts` con todos los tiempos
4. Backend valida y persiste
5. Frontend redirige a feed o perfil

### 5.3 Flujo de Chat en Tiempo Real

1. Usuario abre chat → Conexión WebSocket
2. Frontend carga historial vía REST (`GET /api/chat/conversations/{id}/messages`)
3. Nuevos mensajes vía WebSocket
4. Estado: SENDING → SENT → DELIVERED → READ

## 6. Consideraciones de Seguridad

### 6.1 Backend

- Spring Security con filtros JWT
- Rate limiting en login (5 intentos/minuto)
- Validación de entrada (Bean Validation)
- CORS configurado para dominio frontend
- Contraseñas hasheadas con BCrypt

### 6.2 Frontend

- Tokens nunca en localStorage (solo en memoria/context)
- Refresh token en cookie HttpOnly (manejado por backend)
- Sanitización de inputs
- Validación de formularios antes de enviar

## 7. Integración WebSocket

### 7.1 Configuración

- Endpoint: `/ws/chat`
- Protocolo: STOMP sobre WebSocket (o mensajes JSON simples)
- Autenticación: Token JWT en query param o header

### 7.2 Eventos

- `message.sent` - Nuevo mensaje enviado
- `message.delivered` - Mensaje entregado
- `message.read` - Mensaje leído
- `user.typing` - Usuario escribiendo (opcional)

## 8. Planificación de Desarrollo

### 8.1 Fases Sugeridas

1. **Fase 1**: Autenticación (registro, login, JWT)
2. **Fase 2**: Perfil de usuario (CRUD básico)
3. **Fase 3**: Publicaciones (crear, feed, likes)
4. **Fase 4**: Chat (REST + WebSocket)
5. **Fase 5**: Búsqueda y amistades
6. **Fase 6**: Pulido y testing

### 8.2 Documentación a Generar

- Especificación OpenAPI/Swagger (SpringDoc)
- README con instrucciones de despliegue
- Diagramas de flujo principales (opcional, para memoria)

## 9. Archivos de Diseño a Crear/Completar

1. **`design/Architecture.md`** - Arquitectura detallada del sistema
2. **`design/API_Design.md`** - Especificación completa de endpoints
3. **`design/Frontend_Structure.md`** - Estructura y componentes React
4. **`design/Security_Design.md`** - Medidas de seguridad implementadas
5. Actualizar **`trainhub/planificacion/tasks.md`** con tareas de implementación

## 10. Decisiones Técnicas Pendientes

- [ ] ¿Usar STOMP para WebSocket o mensajes JSON simples?
- [ ] ¿Implementar React Query o manejar estado manualmente?
- [ ] ¿Usar librería de UI (Material-UI, Ant Design) o CSS custom?
- [ ] ¿Estrategia de almacenamiento de imágenes (local, S3, Cloudinary)?
- [ ] ¿Implementar notificaciones push o solo in-app?