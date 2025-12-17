  
## Hoy 12/3

# Diseño
[x] Funcionalidades / Requisitos funcionales

[x] diseño BDD 
  [ x] definir entidades  
  [ x] Índices para optimizar consultas frecuentes
  [ x] Constraints a nivel de BD para validación
  [ x] Triggers para actualizar updated_at
  [ x] Vistas para consultas comunes (feed, conversaciones)

[x] Architecture.md

[x] API_Design.md

[x] Diseño de la Interfaz de Usuario (UI/UX) 

[ ] Entender Security_Design.md


[ ] Entender Frontend_Structure.md

[ ] Definir despliegue

[?] Planificacion
   Linear. Pasarle este archivo



--- 

### 🐳 Docker

⚠️ Está un poco raro el Docker-compose por las variables de entorno, porque me obligo a crearlas manualmente
  - [] hot-reload está funcionando? cómo lo uso ?

---

## Implementación - Fases de Desarrollo

### Fase 1: Autenticación (Backend + Frontend)
#### Backend
- [ ] Configurar Spring Security
- [ ] Implementar JWT Token Provider
- [ ] Crear AuthController (register, login, refresh, logout)
- [ ] Implementar AuthService con validaciones
- [ ] Crear entidades: User, RefreshToken, EmailVerification
- [ ] Implementar UserRepository
- [ ] Configurar BCrypt para contraseñas
- [ ] Implementar rate limiting en login
- [ ] Crear DTOs: RegisterRequest, LoginRequest, AuthResponse
- [ ] Implementar verificación de email
- [ ] Configurar CORS
- [ ] Tests unitarios de autenticación

#### Frontend (Angular)
- [ ] Crear proyecto Angular 17 standalone con Vite/SSR y SCSS (`ng new --v17 --ssr --style=scss`)
- [ ] Configurar routing standalone + lazy routes y guards (auth/guest)
- [ ] Interceptor JWT + manejo 401/403 (refresh / redirigir a login)
- [ ] Servicio `auth.service.ts` (login, register, logout, refresh)
- [ ] Páginas Login y Register con Reactive Forms y validaciones
- [ ] Manejo de errores de autenticación (UI + toast)
- [ ] Integración con backend

---

### Fase 2: Perfil de Usuario (Backend + Frontend)
#### Backend
- [ ] Crear UserController (GET /me, PUT /me)
- [ ] Implementar UserService
- [ ] Endpoint para subir avatar (POST /me/avatar)
- [ ] Endpoint para eliminar avatar (DELETE /me/avatar)
- [ ] Endpoint para perfil público (GET /users/{id})
- [ ] Validación de imágenes (formato, tamaño, dimensiones)
- [ ] Almacenamiento de archivos (local o cloud)
- [ ] DTOs: UserResponse, UpdateUserRequest
- [ ] Tests de endpoints de usuario

#### Frontend (Angular)
- [ ] Página Profile (pública/propia) con Angular standalone
- [ ] Componente ProfileEdit con Reactive Forms
- [ ] Upload de avatar (input file) + previsualización
- [ ] Servicio `user.service.ts`
- [ ] Integración con backend

---

### Fase 3: Publicaciones (Backend + Frontend)
#### Backend
- [ ] Crear entidades: Post, PostRunningTime, PostWorkoutTime, PostLike
- [ ] Crear PostController (POST, GET feed, GET /{id}, DELETE)
- [ ] Implementar PostService con validaciones
- [ ] Endpoint para crear publicación (POST /posts)
- [ ] Endpoint para feed (GET /posts/feed) con paginación
- [ ] Endpoint para like/unlike (POST /posts/{id}/like)
- [ ] Validación de tiempos (formato, coherencia)
- [ ] Consulta optimizada de feed (JOINs, índices)
- [ ] DTOs: CreatePostRequest, PostResponse
- [ ] Tests de publicaciones

#### Frontend (Angular)
- [ ] Página CreatePost (wizard 3 pasos, Reactive Forms)
- [ ] Componentes PostCard / PostOverview / PostRunning / PostWorkouts
- [ ] Página Feed con scroll infinito (observador de scroll)
- [ ] Botón de like y contador
- [ ] Formateo de tiempos (pipes)
- [ ] Servicio `post.service.ts`
- [ ] Integración con backend

---

### Fase 4: Chat (Backend + Frontend)
#### Backend
- [ ] Crear entidad Message
- [ ] Crear ChatController (GET conversations, GET messages, POST message)
- [ ] Implementar ChatService
- [ ] Configurar WebSocket (STOMP o JSON)
- [ ] WebSocketHandler para mensajes en tiempo real
- [ ] Endpoints REST para historial
- [ ] Estados de mensajes (SENT, DELIVERED, READ)
- [ ] Rate limiting de mensajes (30/min)
- [ ] Validación de bloqueos de usuarios
- [ ] DTOs: SendMessageRequest, MessageResponse
- [ ] Tests de chat

#### Frontend (Angular)
- [ ] Página Chat (standalone)
- [ ] Componentes ChatWindow, ConversationList, MessageList (paginación), MessageInput
- [ ] Servicio WebSocket RxJS para chat
- [ ] Estado de mensajes (enviando, enviado, entregado, leído)
- [ ] Servicio `chat.service.ts` (REST + WS)
- [ ] Integración con backend

---

### Fase 5: Búsqueda y Amistades (Backend + Frontend)
#### Backend
- [ ] Crear entidad Friendship
- [ ] Crear SearchController (GET /search/users)
- [ ] Implementar búsqueda de usuarios (nombre, case-insensitive)
- [ ] Crear FriendshipController (POST request, PUT accept, GET list)
- [ ] Implementar FriendshipService
- [ ] Validación de relaciones bidireccionales
- [ ] Endpoint de búsqueda con debounce en frontend
- [ ] DTOs: SearchResponse, FriendshipRequest
- [ ] Tests de búsqueda y amistades

#### Frontend (Angular)
- [ ] Componente UserSearch (navbar, debounce)
- [ ] Mostrar estado de amistad en resultados
- [ ] Página de lista de amigos
- [ ] Envío/aceptación de solicitudes de amistad
- [ ] Servicios `search.service.ts` y `friendship.service.ts`
- [ ] Integración con backend

---

### Fase 6: Pulido y Testing
#### Backend
- [ ] Manejo global de excepciones (GlobalExceptionHandler)
- [ ] Validación de todos los endpoints
- [ ] Documentación Swagger/OpenAPI completa
- [ ] Tests de integración
- [ ] Optimización de consultas
- [ ] Logging y monitoreo
- [ ] Configuración de producción

#### Frontend (Angular)
- [ ] Manejo de errores global (interceptor + toasts)
- [ ] Loading states en todos los componentes
- [ ] Optimización: change detection OnPush, trackBy, lazy routes
- [ ] Responsive design
- [ ] Accesibilidad básica
- [ ] Testing de componentes críticos
- [ ] Documentación de componentes

#### General
- [ ] README con instrucciones de despliegue
- [ ] Documentación de API (Swagger)
- [ ] Configuración de Docker para producción
- [ ] Variables de entorno documentadas
- [ ] Revisión de seguridad
- [ ] Pruebas de carga básicas
