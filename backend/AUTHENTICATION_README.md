# 🔐 Sistema de Autenticación con JWT

Este documento explica cómo funciona el sistema de autenticación implementado en TrainHub.

## 📋 Descripción General

El sistema utiliza **JWT (JSON Web Tokens)** para autenticar usuarios de forma stateless (sin sesiones en el servidor).

### Componentes Principales

1. **JwtService** - Genera y valida tokens JWT
2. **CustomUserDetailsService** - Carga usuarios desde la base de datos
3. **JwtAuthenticationFilter** - Intercepta peticiones y valida tokens
4. **SecurityConfig** - Configuración de Spring Security

## 🚀 Endpoints Disponibles

### 1. Registro de Usuario
**POST** `/api/users/register`

```json
{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Respuesta:**
```
"Usuario registrado con éxito"
```

### 2. Login de Usuario
**POST** `/api/users/login`

```json
{
  "usernameOrEmail": "usuario123",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "usuario123",
  "email": "usuario@example.com"
}
```

## 🔑 Usando el Token JWT

Una vez que obtienes el token del login, debes incluirlo en todas las peticiones a endpoints protegidos:

### Headers
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Ejemplo con cURL
```bash
curl -X GET http://localhost:8081/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Ejemplo con JavaScript (Fetch)
```javascript
fetch('http://localhost:8081/api/protected-endpoint', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9...',
    'Content-Type': 'application/json'
  }
})
```

### Ejemplo con Postman
1. Selecciona la pestaña **Authorization**
2. Tipo: **Bearer Token**
3. Pega tu token en el campo **Token**

## ⚙️ Configuración

### application.properties
```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000  # 24 horas en milisegundos
```

⚠️ **IMPORTANTE**: En producción, la clave secreta (`jwt.secret`) debe:
- Ser más compleja y segura
- Estar en variables de entorno
- NUNCA estar en el código fuente

## 🛡️ Seguridad

### Endpoints Públicos (Sin autenticación)
- `/api/users/register`
- `/api/users/login`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### Endpoints Protegidos
Todos los demás endpoints requieren un token JWT válido.

## 🔄 Flujo de Autenticación

1. **Usuario se registra** → POST `/api/users/register`
2. **Usuario hace login** → POST `/api/users/login` → Recibe token JWT
3. **Usuario accede a recursos protegidos** → Incluye token en header `Authorization`
4. **JwtAuthenticationFilter** valida el token en cada petición
5. Si el token es válido → Acceso permitido
6. Si el token es inválido/expirado → 403 Forbidden

## 📝 Notas Técnicas

- **Expiración**: Los tokens expiran después de 24 horas
- **Algoritmo**: HS256 (HMAC con SHA-256)
- **Sin estado (Stateless)**: No se guardan sesiones en el servidor
- **CSRF desactivado**: No es necesario con JWT

## 🧪 Testing con Swagger

Accede a: `http://localhost:8081/swagger-ui.html`

1. Primero ejecuta el endpoint `/api/users/login`
2. Copia el token de la respuesta
3. Haz clic en el botón **Authorize** (🔒) en la parte superior
4. Ingresa: `Bearer tu-token-aqui`
5. Ahora puedes probar endpoints protegidos

## 🔧 Solución de Problemas

### Error: "Token expirado"
- El token dura 24 horas. Necesitas hacer login nuevamente.

### Error: "401 Unauthorized"
- Verifica que el header `Authorization` esté presente
- Asegúrate de incluir "Bearer " antes del token
- Verifica que el token no esté expirado

### Error: "403 Forbidden"
- El token es inválido o ha sido manipulado
- Intenta hacer login nuevamente

## 👥 Autenticación con Username o Email

El sistema permite hacer login con **username** o **email**:

```json
// Opción 1: Con username
{
  "usernameOrEmail": "usuario123",
  "password": "password123"
}

// Opción 2: Con email
{
  "usernameOrEmail": "usuario@example.com",
  "password": "password123"
}
```

Ambas opciones funcionan correctamente.

