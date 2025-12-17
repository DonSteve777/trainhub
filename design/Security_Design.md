# Diseño de Seguridad - TrainHub

Este documento describe las medidas de seguridad implementadas en TrainHub, incluyendo autenticación, autorización, protección de datos y mitigación de vulnerabilidades comunes.

## 1. Autenticación

### 1.1 JWT (JSON Web Tokens)

#### 1.1.1 Access Token
- **Algoritmo**: RS256 (recomendado) o HS256
- **Duración**: 15 minutos (configurable)
- **Contenido**:
  ```json
  {
    "sub": "user_id",
    "email": "user@example.com",
    "roles": ["USER"],
    "iat": 1234567890,
    "exp": 1234568790
  }
  ```
- **Transporte**: Header `Authorization: Bearer <token>`
- **Almacenamiento Frontend**: En memoria (React Context), **NO en localStorage**

#### 1.1.2 Refresh Token
- **Tipo**: JWT o token opaco
- **Duración**: 7 días (configurable)
- **Rotación**: Se rota en cada uso (invalida el anterior)
- **Transporte**: Cookie `HttpOnly`, `Secure`, `SameSite=Strict`
- **Path**: `/api/auth/refresh`
- **Almacenamiento**: Solo en cookie del navegador (no accesible desde JavaScript)

#### 1.1.3 Flujo de Autenticación

```
1. Usuario → POST /api/auth/login
2. Backend valida credenciales
3. Backend genera:
   - Access Token (15 min) → Response Body
   - Refresh Token (7 días) → Cookie HttpOnly
4. Frontend almacena Access Token en memoria
5. Cada request incluye: Authorization: Bearer <accessToken>
6. Si Access Token expira (401):
   - Frontend llama POST /api/auth/refresh
   - Backend valida Refresh Token de cookie
   - Backend genera nuevo Access Token y rota Refresh Token
7. Si Refresh Token expira → Usuario debe hacer login nuevamente
```

### 1.2 Validación de Credenciales

- **Contraseñas**: Hash con BCrypt (cost factor: 10-12)
- **Comparación**: `BCrypt.checkpw(plainPassword, hashedPassword)`
- **Nunca almacenar contraseñas en texto plano**

### 1.3 Rate Limiting en Login

- **Límite**: 5 intentos por minuto por IP y/o cuenta
- **Implementación**: Spring Security + Redis o en memoria
- **Respuesta**: HTTP 429 (Too Many Requests)
- **Headers**: `Retry-After: 60`
- **Bloqueo temporal**: Después de múltiples violaciones

---

## 2. Autorización

### 2.1 Control de Acceso

#### 2.1.1 Endpoints Públicos
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/verify-email`
- `GET /api/auth/refresh` (requiere refresh token válido)

#### 2.1.2 Endpoints Protegidos
Todos los demás endpoints requieren:
- Access Token válido en header `Authorization`
- Token no expirado
- Usuario con estado `ACTIVE` y `emailVerified = true`

#### 2.1.3 Verificación de Propiedad
- **Publicaciones**: Solo el creador puede eliminar
- **Perfil**: Solo el propietario puede editar
- **Mensajes**: Solo el remitente puede ver estados de entrega

### 2.2 Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable()) // Solo si usas JWT
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 3. Protección de Datos

### 3.1 Contraseñas

- **Hash**: BCrypt con cost factor 10-12
- **Validación**: Política estricta (mínimo 8 caracteres, mayúscula, número/símbolo)
- **Nunca**:
  - Enviar contraseñas en logs
  - Retornar contraseñas en respuestas
  - Almacenar en texto plano

### 3.2 Datos Sensibles

- **Emails**: Validación de formato, unicidad
- **Tokens**: Generación criptográficamente segura
- **IDs de usuario**: No exponer secuenciales predecibles (usar UUIDs o IDs grandes)

### 3.3 Sanitización de Entrada

- **Validación**: Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- **Sanitización**: Limpiar HTML, scripts en campos de texto
- **SQL Injection**: Prevenir con JPA/Prepared Statements (automático con Spring Data JPA)
- **XSS**: Escapar contenido en frontend, validar en backend

---

## 4. Protección contra Vulnerabilidades Comunes

### 4.1 OWASP Top 10

#### 4.1.1 Injection (SQL, NoSQL, etc.)
- **Mitigación**: Spring Data JPA usa Prepared Statements
- **Validación**: Validar y sanitizar todas las entradas
- **Consultas nativas**: Usar parámetros nombrados

#### 4.1.2 Broken Authentication
- **Mitigación**: 
  - Tokens JWT firmados criptográficamente
  - Refresh tokens en cookies HttpOnly
  - Rate limiting en login
  - Contraseñas hasheadas con BCrypt

#### 4.1.3 Sensitive Data Exposure
- **Mitigación**:
  - HTTPS en producción (obligatorio)
  - No exponer tokens en URLs
  - No loguear datos sensibles
  - Headers de seguridad (ver sección 5)

#### 4.1.4 XML External Entities (XXE)
- **No aplicable**: No se usa XML parsing

#### 4.1.5 Broken Access Control
- **Mitigación**:
  - Verificar autorización en cada endpoint
  - Validar propiedad de recursos
  - No confiar solo en frontend para autorización

#### 4.1.6 Security Misconfiguration
- **Mitigación**:
  - Configuración segura de Spring Security
  - Deshabilitar endpoints de debug en producción
  - Headers de seguridad configurados

#### 4.1.7 XSS (Cross-Site Scripting)
- **Mitigación**:
  - Sanitización de entrada
  - Content Security Policy (CSP)
  - Escapar contenido en frontend

#### 4.1.8 Insecure Deserialization
- **Mitigación**: 
  - Validar datos deserializados
  - No deserializar datos no confiables

#### 4.1.9 Using Components with Known Vulnerabilities
- **Mitigación**:
  - Mantener dependencias actualizadas
  - Usar herramientas como Dependabot, Snyk

#### 4.1.10 Insufficient Logging & Monitoring
- **Mitigación**:
  - Logging de intentos de login fallidos
  - Monitoreo de rate limits
  - Alertas para actividades sospechosas

---

## 5. Headers de Seguridad HTTP

### 5.1 Configuración Recomendada

```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<HeaderFilter> securityHeadersFilter() {
        FilterRegistrationBean<HeaderFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(new HeaderFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
    
    public class HeaderFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, 
                           ServletResponse response, 
                           FilterChain chain) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Prevenir clickjacking
            httpResponse.setHeader("X-Frame-Options", "DENY");
            
            // Prevenir MIME type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            
            // XSS Protection
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Content Security Policy
            httpResponse.setHeader("Content-Security-Policy", 
                "default-src 'self'; script-src 'self' 'unsafe-inline';");
            
            // Strict Transport Security (solo en HTTPS)
            // httpResponse.setHeader("Strict-Transport-Security", 
            //     "max-age=31536000; includeSubDomains");
            
            chain.doFilter(request, response);
        }
    }
}
```

---

## 6. CORS (Cross-Origin Resource Sharing)

### 6.1 Configuración

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir solo el dominio del frontend
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // Desarrollo
            "https://trainhub.com"     // Producción
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

---

## 7. Validación de Entrada

### 7.1 Bean Validation

```java
public class RegisterRequest {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String name;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[A-Z].*", message = "La contraseña debe contener al menos una mayúscula")
    @Pattern(regexp = ".*[0-9!@#$%^&*].*", message = "La contraseña debe contener al menos un número o símbolo")
    private String password;
    
    // Getters y setters
}
```

### 7.2 Validación Personalizada

```java
@Service
public class UserService {
    
    public void validateUserCreation(RegisterRequest request) {
        // Validar email único
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        
        // Validar contraseña
        PasswordValidator validator = new PasswordValidator();
        if (!validator.isValid(request.getPassword())) {
            throw new BadRequestException("La contraseña no cumple los requisitos");
        }
    }
}
```

---

## 8. Protección de Archivos

### 8.1 Subida de Imágenes (Avatares)

- **Validación de tipo**: Solo JPEG, PNG, WebP
- **Validación de tamaño**: Máximo 5 MB
- **Validación de dimensiones**: Mínimo 256x256 px
- **Escaneo**: Verificar cabeceras de archivo (no confiar solo en extensión)
- **Almacenamiento**: 
  - Desarrollo: Sistema de archivos local
  - Producción: S3, Cloudinary, o similar
- **Nombres de archivo**: Generar nombres únicos (UUID) para evitar conflictos

### 8.2 Ejemplo de Validación

```java
@Service
public class FileService {
    
    public void validateImage(MultipartFile file) {
        // Validar tipo MIME
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png", "image/webp")
                .contains(contentType)) {
            throw new BadRequestException("Formato de imagen no soportado");
        }
        
        // Validar tamaño
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("El archivo excede 5 MB");
        }
        
        // Validar dimensiones (usar biblioteca como ImageIO)
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image.getWidth() < 256 || image.getHeight() < 256) {
            throw new BadRequestException("Dimensiones mínimas: 256x256 px");
        }
    }
}
```

---

## 9. Logging y Monitoreo

### 9.1 Eventos a Registrar

- **Autenticación**:
  - Intentos de login exitosos/fallidos
  - Registros de nuevos usuarios
  - Tokens refrescados
  - Logouts

- **Seguridad**:
  - Rate limit excedido
  - Intentos de acceso no autorizado
  - Tokens inválidos/expirados

- **Operaciones críticas**:
  - Eliminación de publicaciones
  - Cambios de contraseña (futuro)
  - Bloqueos de usuarios

### 9.2 Información a NO Registrar

- Contraseñas (ni en texto plano ni hasheadas)
- Tokens completos (solo IDs o hashes)
- Datos personales sensibles sin necesidad

---

## 10. Consideraciones de Producción

### 10.1 Variables de Entorno

- **Nunca** hardcodear:
  - Claves JWT
  - Credenciales de base de datos
  - API keys
  - URLs de servicios externos

- **Usar**:
  - Variables de entorno
  - Secrets management (AWS Secrets Manager, HashiCorp Vault)
  - Docker secrets

### 10.2 HTTPS

- **Obligatorio** en producción
- Certificados SSL/TLS válidos
- Redirigir HTTP a HTTPS

### 10.3 Actualizaciones

- Mantener dependencias actualizadas
- Revisar vulnerabilidades conocidas (CVE)
- Aplicar parches de seguridad

---

## 11. Referencias

- [Architecture.md](Architecture.md) - Arquitectura del sistema
- [API_Design.md](API_Design.md) - Especificación de endpoints
- [Funcionalidades.md](Funcionalidades.md) - Requisitos funcionales
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- Spring Security: https://spring.io/projects/spring-security

