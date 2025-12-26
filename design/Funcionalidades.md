
## Requisitos Funcionales


### US-1.1: Registro de Nuevo Usuario

**Qué necesito hacer:**
- Crear una página de registro donde el usuario pueda darse de alta
- El formulario debe tener: nombre completo, email y contraseña
- Cuando el usuario se registre:
  - Validar que todos los campos estén bien
  - Guardar el usuario en la BD con estado "Pendiente de Confirmación"
  - Enviar un email de confirmación
  - Mostrarle una página que le diga "revisa tu correo"

**Flujo:**
1. Usuario va a /registro
2. Llena el formulario (nombre, email, contraseña)
3. Click en "Registrarse"
4. Si todo está bien → guardo en BD, envío email, muestro página de confirmación
5. Si algo falla → muestro error

### US-1.2: Inicio de Sesión (Login)

**Qué necesito hacer:**
- Crear una página de login donde el usuario pueda iniciar sesión
- El formulario debe tener: email y contraseña
- Cuando el usuario intente iniciar sesión:
  - Validar que el email y la contraseña sean correctos
  - Verificar que el email esté confirmado (emailVerified = true)
  - Verificar que la cuenta esté activa (accountStatus = ACTIVE)
  - Generar un token JWT para mantener la sesión
  - Redirigir al usuario a la página principal o dashboard
- Si algo falla, mostrar mensajes de error específicos:
  - "Credenciales incorrectas" si email/contraseña no coinciden
  - "Por favor, confirma tu email antes de iniciar sesión" si email no verificado
  - "Tu cuenta está bloqueada" si accountStatus = BLOCKED
  - "Tu cuenta está pendiente de confirmación" si accountStatus = PENDING_CONFIRMATION

**Flujo:**
1. Usuario va a /login
2. Llena el formulario (email, contraseña)
3. Click en "Iniciar Sesión"
4. Validar credenciales:
   - Buscar usuario por email
   - Verificar que la contraseña coincida con el hash almacenado
5. Validar estado de la cuenta:
   - Verificar que emailVerified = true
   - Verificar que accountStatus = ACTIVE
6. Si todo está bien → generar token JWT, guardar en cookie/localStorage, redirigir a dashboard
7. Si algo falla → mostrar mensaje de error específico según el caso