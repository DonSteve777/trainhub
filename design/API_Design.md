# Diseño de API REST - TrainHub

Este documento especifica todos los endpoints de la API REST de TrainHub, incluyendo request/response, códigos HTTP, y ejemplos.

## 1. Convenciones Generales

### 1.1 Base URL
- **Desarrollo**: `http://localhost:8081/api`
- **Producción**: `https://api.trainhub.com/api`

### 1.2 Autenticación
- **Método**: JWT Bearer Token
- **Header**: `Authorization: Bearer <accessToken>`
- **Refresh Token**: Enviado automáticamente en cookie `HttpOnly` por el backend

### 1.3 Formato de Respuestas

#### Respuesta Exitosa
```json
{
  "data": { ... },
  "message": "Operación exitosa",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

#### Respuesta de Error
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Mensaje de error descriptivo",
    "details": { ... }
  },
  "timestamp": "2024-03-12T10:30:00Z"
}
```

### 1.4 Códigos HTTP
- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado exitosamente
- `400 Bad Request` - Error de validación o solicitud inválida
- `401 Unauthorized` - Token inválido o ausente
- `403 Forbidden` - No autorizado para la operación
- `404 Not Found` - Recurso no encontrado
- `429 Too Many Requests` - Rate limit excedido
- `500 Internal Server Error` - Error del servidor

### 1.5 Paginación
- **Parámetros**: `?page=0&size=20`
- **Respuesta**:
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

## 2. Endpoints de Autenticación

### 2.1 POST /api/auth/register
Registra un nuevo usuario en el sistema.

**Request Body:**
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

**Validaciones:**
- `name`: 2-60 caracteres, requerido
- `email`: formato válido, único, requerido
- `password`: mínimo 8 caracteres, al menos 1 mayúscula, 1 número o símbolo
- `confirmPassword`: debe coincidir con `password`

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "email": "juan@example.com",
    "name": "Juan Pérez",
    "message": "Usuario registrado. Por favor verifica tu email."
  },
  "message": "Registro exitoso",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

**Errores:**
- `400`: Email duplicado, contraseña débil, campos inválidos
- `500`: Error del servidor

---

### 2.2 POST /api/auth/login
Inicia sesión y retorna tokens JWT.

**Request Body:**
```json
{
  "email": "juan@example.com",
  "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "email": "juan@example.com",
      "name": "Juan Pérez",
      "username": "juan_perez",
      "avatarUrl": null,
      "emailVerified": true
    }
  },
  "message": "Login exitoso",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

**Headers de Respuesta:**
- `Set-Cookie: refreshToken=...; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh; Max-Age=604800`

**Errores:**
- `401`: Credenciales inválidas
- `403`: Email no verificado, cuenta bloqueada
- `429`: Demasiados intentos (rate limit)

---

### 2.3 POST /api/auth/refresh
Refresca el access token usando el refresh token.

**Request:**
- Cookie: `refreshToken` (automática)
- No requiere body

**Response (200 OK):**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Token refrescado",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

**Errores:**
- `401`: Refresh token inválido o expirado

---

### 2.4 POST /api/auth/logout
Cierra sesión y revoca el refresh token.

**Request:**
- Header: `Authorization: Bearer <accessToken>`
- Cookie: `refreshToken` (automática)

**Response (200 OK):**
```json
{
  "message": "Sesión cerrada exitosamente",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

---

### 2.5 GET /api/auth/verify-email
Verifica el email del usuario mediante token.

**Query Parameters:**
- `token`: Token de verificación

**Response (200 OK):**
```json
{
  "message": "Email verificado exitosamente",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

**Errores:**
- `400`: Token inválido o expirado
- `404`: Token no encontrado

---

## 3. Endpoints de Usuario

### 3.1 GET /api/users/me
Obtiene el perfil del usuario autenticado.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "email": "juan@example.com",
    "name": "Juan Pérez",
    "photoUrl": "https://cdn.trainhub.com/avatars/1.jpg",
    "bio": "Entrenador de Hyrox apasionado",
    "emailVerified": true,
    "accountStatus": "ACTIVE",
    "registrationDate": "2024-01-15T10:00:00Z",
    "lastLogin": "2024-03-12T09:00:00Z"
  },
  "message": "Perfil obtenido",
  "timestamp": "2024-03-12T10:30:00Z"
}
```

---

### 3.2 PUT /api/users/me
Actualiza el perfil del usuario autenticado.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Request Body:**
```json
{
  "name": "Juan Carlos Pérez",
  "bio": "Nueva biografía actualizada"
}
```

**Validaciones:**
- `name`: 2-60 caracteres, requerido
- `bio`: máximo 280 caracteres, opcional

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "name": "Juan Carlos Pérez",
    "bio": "Nueva biografía actualizada",
    "updatedAt": "2024-03-12T10:35:00Z"
  },
  "message": "Perfil actualizado",
  "timestamp": "2024-03-12T10:35:00Z"
}
```

**Errores:**
- `400`: Validación fallida
- `401`: Token inválido

---

### 3.3 POST /api/users/me/avatar
Sube o actualiza la foto de perfil del usuario.

**Headers:**
- `Authorization: Bearer <accessToken>`
- `Content-Type: multipart/form-data`

**Request Body:**
- `file`: Archivo de imagen (JPEG, PNG, WebP)
- Tamaño máximo: 5 MB
- Dimensiones mínimas: 256x256 px

**Response (200 OK):**
```json
{
  "data": {
    "photoUrl": "https://cdn.trainhub.com/avatars/1.jpg"
  },
  "message": "Avatar actualizado",
  "timestamp": "2024-03-12T10:40:00Z"
}
```

**Errores:**
- `400`: Archivo inválido, formato no soportado, tamaño excedido
- `415`: Tipo de archivo no soportado

---

### 3.4 DELETE /api/users/me/avatar
Elimina la foto de perfil del usuario.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Response (200 OK):**
```json
{
  "message": "Avatar eliminado",
  "timestamp": "2024-03-12T10:45:00Z"
}
```

---

### 3.5 GET /api/users/{id}
Obtiene el perfil público de otro usuario.

**Headers:**
- `Authorization: Bearer <accessToken>` (opcional, para ver información adicional)

**Path Parameters:**
- `id`: ID del usuario

**Response (200 OK):**
```json
{
  "data": {
    "id": 2,
    "username": "maria_garcia",
    "name": "María García",
    "avatarUrl": "https://cdn.trainhub.com/avatars/2.jpg",
    "bio": "Atleta profesional",
    "createdAt": "2024-02-01T10:00:00Z"
  },
  "message": "Perfil obtenido",
  "timestamp": "2024-03-12T10:50:00Z"
}
```

**Errores:**
- `404`: Usuario no encontrado

---

## 4. Endpoints de Publicaciones

### 4.1 POST /api/posts
Crea una nueva publicación de entrenamiento.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Request Body:**
```json
{
  "r1Time": "12:30",
  "r2Time": "13:15",
  "r3Time": "11:45",
  "r4Time": "14:20",
  "r5Time": "12:10",
  "r6Time": "13:00",
  "r7Time": "11:50",
  "r8Time": "12:45",
  "w1Time": "8:30",
  "w2Time": "9:15",
  "w3Time": "7:45",
  "w4Time": "10:20",
  "w5Time": "8:10",
  "w6Time": "9:00",
  "w7Time": "7:50",
  "w8Time": "8:45",
  "totalTime": "1:45:30"
}
```

**Validaciones:**
- Todos los 8 tiempos de running (r1Time a r8Time) requeridos
- Todos los 8 tiempos de workout (w1Time a w8Time) requeridos
- Formato de tiempo: `MM:SS` para tiempos individuales, `HH:MM:SS` para total
- Tiempo total debe ser >= suma de tiempos individuales

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "userId": 1,
    "r1Time": "12:30",
    "r2Time": "13:15",
    "r3Time": "11:45",
    "r4Time": "14:20",
    "r5Time": "12:10",
    "r6Time": "13:00",
    "r7Time": "11:50",
    "r8Time": "12:45",
    "w1Time": "8:30",
    "w2Time": "9:15",
    "w3Time": "7:45",
    "w4Time": "10:20",
    "w5Time": "8:10",
    "w6Time": "9:00",
    "w7Time": "7:50",
    "w8Time": "8:45",
    "totalTime": "1:45:30",
    "likesCount": 0,
    "createdAt": "2024-03-12T11:00:00Z"
  },
  "message": "Publicación creada",
  "timestamp": "2024-03-12T11:00:00Z"
}
```

**Errores:**
- `400`: Validación fallida, tiempos incoherentes
- `401`: No autenticado

---

### 4.2 GET /api/posts/feed
Obtiene el feed de publicaciones de amigos.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "user": {
          "id": 2,
          "username": "maria_garcia",
          "name": "María García",
          "avatarUrl": "https://cdn.trainhub.com/avatars/2.jpg"
        },
        "r1Time": "12:30",
        "r2Time": "13:15",
        "r3Time": "11:45",
        "r4Time": "14:20",
        "r5Time": "12:10",
        "r6Time": "13:00",
        "r7Time": "11:50",
        "r8Time": "12:45",
        "w1Time": "8:30",
        "w2Time": "9:15",
        "w3Time": "7:45",
        "w4Time": "10:20",
        "w5Time": "8:10",
        "w6Time": "9:00",
        "w7Time": "7:50",
        "w8Time": "8:45",
        "totalTime": "1:45:30",
        "likesCount": 5,
        "isLiked": true,
        "createdAt": "2024-03-12T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  },
  "message": "Feed obtenido",
  "timestamp": "2024-03-12T11:05:00Z"
}
```

**Errores:**
- `401`: No autenticado

---

### 4.3 GET /api/posts/{id}
Obtiene una publicación específica.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `id`: ID de la publicación

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "user": { ... },
    "totalTimeSeconds": 6330,
    "runningTimes": [ ... ],
    "workoutTimes": [ ... ],
    "likesCount": 5,
    "isLiked": true,
    "createdAt": "2024-03-12T10:00:00Z"
  },
  "message": "Publicación obtenida",
  "timestamp": "2024-03-12T11:10:00Z"
}
```

**Errores:**
- `404`: Publicación no encontrada

---

### 4.4 DELETE /api/posts/{id}
Elimina una publicación (soft delete).

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `id`: ID de la publicación

**Response (200 OK):**
```json
{
  "message": "Publicación eliminada",
  "timestamp": "2024-03-12T11:15:00Z"
}
```

**Errores:**
- `403`: No es el propietario de la publicación
- `404`: Publicación no encontrada

---

### 4.5 POST /api/posts/{id}/like
Da o quita like a una publicación.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `id`: ID de la publicación

**Response (200 OK):**
```json
{
  "data": {
    "liked": true,
    "likesCount": 6
  },
  "message": "Like actualizado",
  "timestamp": "2024-03-12T11:20:00Z"
}
```

**Errores:**
- `404`: Publicación no encontrada

---

### 4.6 GET /api/posts/{id}/likes
Obtiene la lista de usuarios que dieron like.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "id": 2,
        "username": "maria_garcia",
        "name": "María García",
        "avatarUrl": "https://cdn.trainhub.com/avatars/2.jpg"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 6,
    "totalPages": 1
  },
  "message": "Likes obtenidos",
  "timestamp": "2024-03-12T11:25:00Z"
}
```

---

## 5. Endpoints de Chat

### 5.1 GET /api/chat/conversations
Obtiene la lista de conversaciones del usuario. Las conversaciones se derivan agrupando mensajes por pares de usuarios.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "otherUserId": 2,
        "otherUser": {
          "id": 2,
          "name": "María García",
          "photoUrl": "https://cdn.trainhub.com/avatars/2.jpg"
        },
        "lastMessage": {
          "id": 10,
          "content": "¡Hola! ¿Cómo estás?",
          "senderId": 2,
          "receiverId": 1,
          "status": "READ",
          "sendDate": "2024-03-12T11:00:00Z",
          "readAt": "2024-03-12T11:01:00Z"
        },
        "unreadCount": 0
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  },
  "message": "Conversaciones obtenidas",
  "timestamp": "2024-03-12T11:30:00Z"
}
```

**Nota**: Las conversaciones se calculan agrupando mensajes donde el usuario actual es `sender_id` o `receiver_id`, identificando al otro usuario del par.

---

### 5.2 GET /api/chat/conversations/{userId}/messages
Obtiene los mensajes de una conversación con otro usuario específico.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `userId`: ID del otro usuario de la conversación

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 50)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "id": 10,
        "senderId": 2,
        "receiverId": 1,
        "content": "¡Hola! ¿Cómo estás?",
        "status": "READ",
        "sendDate": "2024-03-12T11:00:00Z",
        "readAt": "2024-03-12T11:01:00Z"
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 25,
    "totalPages": 1
  },
  "message": "Mensajes obtenidos",
  "timestamp": "2024-03-12T11:35:00Z"
}
```

**Nota**: La consulta busca mensajes donde el usuario autenticado y `userId` son `sender_id` o `receiver_id` (bidireccional).

---

### 5.3 POST /api/chat/messages
Envía un nuevo mensaje. Si no existe conversación, se crea automáticamente.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Request Body:**
```json
{
  "receiverId": 2,
  "content": "¡Hola María! ¿Quieres entrenar mañana?"
}
```

**Validaciones:**
- `receiverId`: ID válido, diferente del sender
- `content`: 1-2000 caracteres, no vacío

**Response (201 Created):**
```json
{
  "data": {
    "id": 11,
    "conversationId": 1,
    "senderId": 1,
    "content": "¡Hola María! ¿Quieres entrenar mañana?",
    "status": "SENT",
    "sendDate": "2024-03-12T11:40:00Z"
  },
  "message": "Mensaje enviado",
  "timestamp": "2024-03-12T11:40:00Z"
}
```

**Nota**: El backend busca o crea automáticamente la conversación entre el usuario autenticado y el receiverId, y agrega ambos como participantes si no existen.

**Errores:**
- `400`: Contenido inválido, usuario bloqueado
- `404`: Usuario receptor no encontrado

---

### 5.4 PUT /api/chat/messages/{id}/read
Marca un mensaje como leído.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `id`: ID del mensaje

**Response (200 OK):**
```json
{
  "data": {
    "id": 11,
    "readAt": "2024-03-12T11:45:00Z"
  },
  "message": "Mensaje marcado como leído",
  "timestamp": "2024-03-12T11:45:00Z"
}
```

---

## 6. Endpoints de Búsqueda

### 6.1 GET /api/search/users
Busca usuarios por nombre.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Query Parameters:**
- `q`: Término de búsqueda (mínimo 2 caracteres)
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10, máximo: 20)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "id": 2,
    "name": "María García",
    "photoUrl": "https://cdn.trainhub.com/avatars/2.jpg",
    "friendshipStatus": "FRIEND"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1
  },
  "message": "Búsqueda completada",
  "timestamp": "2024-03-12T11:50:00Z"
}
```

**Errores:**
- `400`: Término de búsqueda muy corto (< 2 caracteres)

---

## 7. Endpoints de Amistades

### 7.1 POST /api/friendships/request
Envía una solicitud de amistad.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Request Body:**
```json
{
  "userId": 2
}
```

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "user1Id": 1,
    "user2Id": 2,
    "status": "PENDING",
    "createdAt": "2024-03-12T12:00:00Z"
  },
  "message": "Solicitud de amistad enviada",
  "timestamp": "2024-03-12T12:00:00Z"
}
```

**Errores:**
- `400`: Ya existe una solicitud, usuarios bloqueados
- `404`: Usuario no encontrado

---

### 7.2 PUT /api/friendships/{userId}/accept
Acepta una solicitud de amistad.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Path Parameters:**
- `userId`: ID del usuario que envió la solicitud

**Response (200 OK):**
```json
{
  "data": {
    "userAId": 1,
    "userBId": 2,
    "status": "FRIEND",
    "relationshipStartDate": "2024-03-12T12:05:00Z"
  },
  "message": "Solicitud aceptada",
  "timestamp": "2024-03-12T12:05:00Z"
}
```

**Errores:**
- `403`: No es el receptor de la solicitud
- `404`: Solicitud no encontrada

**Nota**: El status cambia de `PENDING` a `FRIEND`. La relación se identifica por la PK compuesta (user_a_id, user_b_id) donde user_a_id < user_b_id.

---

### 7.3 GET /api/friendships
Obtiene la lista de amigos del usuario.

**Headers:**
- `Authorization: Bearer <accessToken>`

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)

**Response (200 OK):**
```json
{
  "data": {
    "content": [
      {
        "id": 2,
        "name": "María García",
        "photoUrl": "https://cdn.trainhub.com/avatars/2.jpg",
        "relationshipStartDate": "2024-03-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "message": "Amigos obtenidos",
  "timestamp": "2024-03-12T12:10:00Z"
}
```

---

## 8. WebSocket para Chat

### 8.1 Conexión WebSocket
- **Endpoint**: `ws://localhost:8081/ws/chat`
- **Autenticación**: Token JWT en query parameter: `?token=<accessToken>`
- **Protocolo**: STOMP sobre WebSocket (o mensajes JSON simples)

### 8.2 Eventos

#### message.sent
```json
{
  "type": "message.sent",
  "data": {
    "id": 11,
    "senderId": 1,
    "receiverId": 2,
    "content": "¡Hola!",
    "status": "SENT",
    "createdAt": "2024-03-12T12:15:00Z"
  }
}
```

#### message.delivered
```json
{
  "type": "message.delivered",
  "data": {
    "messageId": 11,
    "deliveredAt": "2024-03-12T12:15:05Z"
  }
}
```

#### message.read
```json
{
  "type": "message.read",
  "data": {
    "messageId": 11,
    "readAt": "2024-03-12T12:16:00Z"
  }
}
```

---

## 9. Notas sobre Estructura de Datos

### 9.1 Posts
- Los tiempos se almacenan como columnas individuales (r1Time a r8Time, w1Time a w8Time)
- Formato: `MM:SS` para tiempos individuales, `HH:MM:SS` para totalTime
- Este diseño denormalizado mejora el rendimiento al evitar JOINs

### 9.2 Chat
- Los mensajes tienen `sender_id` y `receiver_id` directamente (no hay tabla de conversaciones)
- Las conversaciones se derivan agrupando mensajes por pares de usuarios
- Para obtener conversaciones: agrupar mensajes donde el usuario es `sender_id` o `receiver_id`
- El campo `read_at` tiene DEFAULT NOW() pero debe actualizarse cuando el mensaje es leído

### 9.3 Friendships
- Status: `PENDING`, `FRIEND`, `BLOCKED` (no `ACCEPTED`)
- La relación se identifica por PK compuesta (user_a_id, user_b_id) donde user_a_id < user_b_id
- Esto garantiza unicidad y simplifica consultas bidireccionales

## 10. Referencias

- [Architecture.md](Architecture.md) - Arquitectura del sistema
- [Database_Design.md](Database_Design.md) - Diseño de base de datos (basado en dbdiagram.sql)
- [Security_Design.md](Security_Design.md) - Medidas de seguridad
- [Funcionalidades.md](Funcionalidades.md) - Requisitos funcionales
- `dbdiagram.sql` - Esquema fuente de la base de datos

