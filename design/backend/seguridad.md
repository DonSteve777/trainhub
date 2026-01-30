### 🧠 **ÍNDICE — Spring Security para Registro y Login**

### 4. 🔐 Password Encoding

- Uso de `PasswordEncoder` (`BCryptPasswordEncoder`)
- Cómo encriptar contraseñas antes de guardarlas

### 5. 🚪 Registro de Usuarios

- Endpoint `POST /register`
- Validaciones de datos
- Guardado en base de datos
- Encriptar contraseña antes de persistir
- (Opcional) Verificación por email o token

### 6. 🔑 Autenticación (Login)

- Flujo de login tradicional con form-data o JSON
- Uso de `AuthenticationManager` y `UsernamePasswordAuthenticationToken`
- Generación de JWT (si usas tokens) o manejo de sesión (si usas cookies)

### 7. 🧾 Autorización

- Roles y Authorities
- Anotaciones: `@PreAuthorize`, `@Secured`, `@RolesAllowed`
- Configurar qué endpoints son públicos o protegidos

### 8. 🧠 JWT (JSON Web Token) (si vas por autenticación stateless)

- Qué es y cómo funciona
- Generar, firmar y validar tokens
- Filtros JWT (para validar tokens en cada request)
- Configuración stateless con `SecurityFilterChain`

### 9. 🧩 Custom UserDetailsService

- Implementar `UserDetailsService` para cargar usuarios desde la base de datos
- Integrar con Spring Security

### 11. 🚫 Manejo de Errores y Excepciones

- Personalizar mensajes de error de login o acceso denegado
- Filtros personalizados para excepciones JWT o de seguridad

