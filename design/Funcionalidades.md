
## Requisitos Funcionales

### RF-1.1: Registro de Nuevo Usuario

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-1.1 |
| **Nombre** | Creación de Cuenta de Usuario |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir a un nuevo usuario crear una cuenta proporcionando la información requerida a través de la interfaz de registro. |

#### 1. Pasos de la Interacción (Flujo Principal)
1.  El usuario accede a la página de registro.
2.  El sistema presenta un formulario con los siguientes campos obligatorios:
    * Nombre Completo
    * Correo Electrónico (Email)
    * **Contraseña**
3.  El usuario ingresa la información requerida y selecciona la opción "Registrarse".
4.  El sistema valida que todos los campos obligatorios estén completos y sean válidos (según las reglas de negocio descritas a continuación).
5.  Si la validación es exitosa, el sistema crea un nuevo registro de usuario en la base de datos con el estado inicial "Pendiente de Confirmación".
6.  El sistema envía un correo electrónico de confirmación a la dirección proporcionada por el usuario.
7.  El sistema redirige al usuario a una página de confirmación, informándole que revise su correo.

#### 2. Reglas de Negocio y Restricciones
* **RN-1.1.1 (Validación de Email):** El correo electrónico debe tener un formato válido (ej. `nombre@dominio.com`) y no debe existir previamente en la base de datos del sistema.
* **RN-1.1.2 (Política de Contraseña):** La contraseña debe cumplir con los siguientes requisitos mínimos:
    * Longitud mínima de 8 caracteres.
    * Debe contener al menos una letra mayúscula.
    * Debe contener al menos un número o un símbolo.
* **RN-1.1.3 (Coincidencia de Contraseña):** El valor del campo "Contraseña" debe coincidir exactamente con el valor del campo "Confirmación de Contraseña".

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-1.1.1 (Campos Incompletos):** Si el usuario omite un campo obligatorio, el sistema mostrará un mensaje de error debajo del campo faltante (ej. "Este campo es requerido") y no procederá con el registro.
* **FA-1.1.2 (Email Duplicado):** Si el correo electrónico ya existe, el sistema mostrará un mensaje de error (ej. "Ya existe una cuenta asociada a esta dirección de correo").
* **FA-1.1.3 (Contraseña Débil):** Si la contraseña no cumple con la política (RN-1.1.2), el sistema mostrará un mensaje de error indicando las reglas que no se cumplen.



### RF-1.2: Inicio de Sesión (Login) con JWT

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-1.2 |
| **Nombre** | Autenticación de Usuario mediante JWT |
| **Prioridad** | Crítica |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá autenticar a un usuario existente mediante email y contraseña válidos y, si la autenticación es exitosa, emitir un par de tokens JWT (access token y refresh token) para el acceso a recursos protegidos. |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario accede a la pantalla de inicio de sesión.
2. El sistema presenta un formulario con los siguientes campos obligatorios:
    * Correo Electrónico (Email)
    * Contraseña
3. El usuario ingresa sus credenciales y selecciona la opción "Iniciar sesión".
4. El sistema valida las credenciales y el estado de la cuenta (existente, activa, email verificado y no bloqueada).
5. Si la validación es exitosa, el sistema:
    * Genera y firma un access token (JWT) de corta duración.
    * Genera y firma un refresh token (JWT u opaco) de mayor duración y registra su estado para permitir revocación/rotación.
    * Devuelve respuesta 200 con:
        - Cuerpo JSON con: `accessToken`, `tokenType="Bearer"`, `expiresIn` (segundos) y datos básicos del usuario (id, nombre, email, roles).
        - Cabecera `Set-Cookie` con `refreshToken` usando atributos `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/auth/refresh` y `Max-Age` acorde a su expiración.
6. A partir de ese momento, el cliente deberá incluir la cabecera `Authorization: Bearer <accessToken>` en cada solicitud a recursos protegidos.

#### 2. Reglas de Negocio y Restricciones
* **RN-1.2.1 (Algoritmo y Firma):** Los JWT deberán estar firmados (RS256 recomendado). La clave privada nunca se expondrá en el cliente.
* **RN-1.2.2 (Expiraciones):** El access token expirará en 15 minutos (configurable). El refresh token expirará en 7 días (configurable) y deberá rotarse en cada uso.
* **RN-1.2.3 (Transporte de Tokens):** El access token se entregará en el cuerpo de la respuesta y se usará en la cabecera `Authorization`. El refresh token se almacenará en cookie `HttpOnly` y `Secure` para mitigar XSS.
* **RN-1.2.4 (Acceso a Recursos Protegidos):** Las rutas protegidas requerirán la cabecera `Authorization: Bearer <accessToken>` válida y no expirada. Ante ausencia o invalidez, se responderá con 401.
* **RN-1.2.5 (Refresco de Token):** Existirá un endpoint `POST /auth/refresh` que, con el refresh token válido, emitirá un nuevo access token y rotará el refresh token, invalidando el anterior.
* **RN-1.2.6 (Cierre de Sesión):** Existirá un endpoint `POST /auth/logout` que revocará el refresh token activo y eliminará la cookie del cliente.
* **RN-1.2.7 (Rate Limiting y Bloqueo):** Se limitarán intentos de login (p.ej., 5/min por IP y/o cuenta). Excesos producirán 429 y, tras repetición, bloqueo temporal de la cuenta.
* **RN-1.2.8 (Estados de Cuenta):** Si la cuenta no está verificada o está bloqueada, el sistema no emitirá tokens y responderá con el código correspondiente.
* **RN-1.2.9 (Skew de Reloj):** Se tolerará una desviación de reloj razonable (p.ej., 60 segundos) al validar `iat/exp`.

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-1.2.1 (Credenciales Inválidas):** Si el email o la contraseña son incorrectos, el sistema responderá con 401 (código `invalid_credentials`) sin especificar cuál campo falló.
* **FA-1.2.2 (Cuenta no Verificada):** Si el email no está verificado, el sistema responderá con 403 (código `email_not_verified`) e indicará el siguiente paso sugerido (reenviar verificación).
* **FA-1.2.3 (Cuenta Bloqueada):** Si la cuenta está bloqueada por seguridad, el sistema responderá con 403 (código `account_locked`).
* **FA-1.2.4 (Access Token Expirado en Recurso Protegido):** El sistema responderá con 401 (código `token_expired`). El cliente deberá invocar `/auth/refresh`. Si el refresh token es válido, se entregará un nuevo access token; si es inválido/expirado, se responderá 401 (código `refresh_invalid`) y el usuario deberá iniciar sesión nuevamente.
* **FA-1.2.5 (Rate Limit Excedido):** Si se excede el límite de intentos, el sistema responderá con 429 (código `too_many_attempts`) e incluirá cabeceras de retry (`Retry-After`) cuando aplique.


### RF-2.1: Gestión de Cuenta de Usuario (Perfil)

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-2.1 |
| **Nombre** | Edición y Visualización de Perfil de Usuario |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir al usuario autenticado visualizar y editar su perfil, incluyendo nombre, foto (avatar) y biografía (bio). |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario autenticado accede a la pantalla "Mi cuenta / Perfil".
2. El sistema muestra los datos actuales del perfil:
    * Nombre
    * Foto de perfil (avatar) si existe; en caso contrario, un placeholder.
    * Biografía (bio)
3. El usuario puede editar los campos permitidos:
    * Actualizar nombre.
    * Subir/actualizar foto de perfil (con previsualización antes de guardar).
    * Editar bio.
4. El usuario selecciona "Guardar cambios".
5. El sistema valida la información y, si es correcta, persiste los cambios y devuelve confirmación visual (mensaje de éxito) junto con los datos actualizados.

#### 2. Reglas de Negocio y Restricciones
* **RN-2.1.1 (Autorización):** Solo el propietario del perfil podrá editar su información. 
* **RN-2.1.2 (Nombre):** El nombre será obligatorio, longitud entre 2 y 60 caracteres, sin emojis de control ni solo espacios. Normalización de espacios consecutivos.
* **RN-2.1.3 (Bio):** La biografía es opcional, con longitud máxima de 280 caracteres. Permite caracteres Unicode seguros, sin HTML ejecutable.
* **RN-2.1.4 (Foto de Perfil):** Formatos permitidos: JPEG/PNG/WebP. Tamaño máximo 5 MB. Dimensiones mínimas 256×256 px; el sistema podrá recortar/escalar manteniendo proporciones.
* **RN-2.1.5 (Contenido Seguro):** Se rechazará contenido con malware o formatos no soportados. Se recomienda análisis antivirus/validación de cabeceras.
* **RN-2.1.6 (Consistencia):** Tras guardar, los datos deben reflejarse inmediatamente en el perfil y en componentes que lo consumen (navbar, feed, chat).

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-2.1.1 (Validación Fallida):** Si nombre o bio no cumplen restricciones, el sistema responderá con 400 indicando qué campo falló y la regla infringida.
* **FA-2.1.2 (Archivo No Válido):** Si la imagen excede el tamaño o el formato no es permitido, el sistema responderá con 400/415 y no guardará cambios parciales.
* **FA-2.1.3 (No Autorizado):** Si la sesión no es válida o el token expiró, responder con 401. El usuario deberá reautenticarse.
* **FA-2.1.4 (Conflicto/Estado):** Si existe un conflicto con el estado de la cuenta (p.ej., bloqueada), responder con 403 e informar la condición.
* **FA-2.1.5 (Error Interno):** Ante un fallo del almacenamiento o procesamiento de imagen, responder con 500 e invitar a reintentar.


### RF-3.1: Creación de Publicaciones de Entrenamiento

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-3.1 |
| **Nombre** | Creación y Publicación de Entrenamientos |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir a usuarios autenticados crear y publicar sus entrenamientos, ingresando los tiempos de 8 pruebas de running, 8 workouts y el tiempo total (roxzone), generando una publicación estilo Instagram con múltiples páginas deslizables (overview, running, workouts). |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario autenticado pulsa el botón "+" en el Navbar.
2. El sistema redirige al usuario a la pantalla de creación de publicación.
3. **Paso 1 - Running:** El sistema presenta una tabla con 8 filas para ingresar los tiempos de las pruebas de running:
   * Cada fila corresponde a una prueba de running.
   * Campos de entrada para tiempo (formato: MM:SS o HH:MM:SS).
   * El usuario puede navegar entre campos usando Tab o clic.
4. El usuario completa los tiempos de running y selecciona el botón "Next".
5. **Paso 2 - Workouts:** El sistema presenta una tabla con 8 filas para ingresar los tiempos de los workouts:
   * Cada fila corresponde a un workout.
   * Campos de entrada para tiempo (formato: MM:SS o HH:MM:SS).
   * El usuario puede navegar entre campos usando Tab o clic.
6. El usuario completa los tiempos de workouts y selecciona el botón "Next".
7. **Paso 3 - Tiempo Total:** El sistema presenta un campo de entrada para el tiempo total (roxzone):
   * Campo de entrada para tiempo total (formato: MM:SS o HH:MM:SS).
8. El usuario ingresa el tiempo total y selecciona el botón "Save" (Guardar).
9. El sistema valida que todos los campos requeridos estén completos y que los tiempos tengan formato válido.
10. Si la validación es exitosa, el sistema:
    * Persiste la publicación en la base de datos.
    * Genera una publicación estilo Instagram con formato deslizable que incluye:
      - **Página Overview:** Vista general con resumen de tiempos y estadísticas.
      - **Página Running:** Detalle de los 8 tiempos de running.
      - **Página Workouts:** Detalle de los 8 tiempos de workouts.
    * Asocia la publicación al usuario autenticado.
    * Redirige al usuario a su feed o perfil mostrando la nueva publicación.
11. El usuario puede deslizar horizontalmente entre las páginas de la publicación para ver los diferentes detalles.

#### 2. Reglas de Negocio y Restricciones
* **RN-3.1.1 (Autorización):** Solo usuarios autenticados pueden crear publicaciones. Cada publicación se asocia automáticamente al usuario que la crea.
* **RN-3.1.2 (Formato de Tiempo):** Los tiempos deben seguir el formato HH:MM:SS o MM:SS. El sistema aceptará valores válidos donde:
  * Horas: 0-23 (opcional si es menor a 1 hora).
  * Minutos: 0-59.
  * Segundos: 0-59.

* **RN-3.1.4 (Validación de Tiempos):** El tiempo total debe ser mayor a la suma de los tiempos individuales de running y workouts. El sistema validará la coherencia lógica de los tiempos.
* **RN-3.1.5 (Navegación entre Pasos):** El usuario puede navegar hacia adelante con "Next" y hacia atrás con "Back" o "Cancel" en cualquier momento durante el proceso de creación.
* **RN-3.1.6 (Cancelación):** Si el usuario selecciona "Cancel" en cualquier paso, el sistema descartará todos los datos ingresados y redirigirá al usuario a la página anterior sin guardar cambios.
* **RN-3.1.7 (Formato de Publicación):** La publicación generada tendrá formato estilo Instagram con:
  * Diseño visual atractivo y consistente.
  * Capacidad de deslizar horizontalmente entre las tres páginas (overview, running, workouts).
  * Indicadores visuales de página activa (dots o similar).
* **RN-3.1.8 (Persistencia):** Una vez guardada, la publicación será permanente y visible en el feed del usuario y en su perfil. El usuario podrá eliminarla posteriormente si lo desea.
* **RN-3.1.9 (Timestamp):** Cada publicación incluirá automáticamente la fecha y hora de creación, visible en el formato relativo (ej. "hace 2 horas", "hoy", "ayer").
* **RN-3.1.10 (Edición):** Las publicaciones no podrán editarse una vez guardadas. Si el usuario necesita corregir datos, deberá eliminar y crear una nueva publicación.

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-3.1.1 (Campos Incompletos):** Si el usuario intenta avanzar al siguiente paso sin completar todos los campos requeridos del paso actual, el sistema mostrará mensajes de error debajo de los campos faltantes (ej. "Este campo es requerido") y no permitirá avanzar.
* **FA-3.1.2 (Formato de Tiempo Inválido):** Si el usuario ingresa un tiempo con formato inválido (ej. "abc", "25:70:30"), el sistema mostrará un mensaje de error indicando el formato esperado y no permitirá avanzar hasta corregirlo.
* **FA-3.1.3 (Tiempo Total Incoherente):** Si el tiempo total es menor que la suma de los tiempos individuales, el sistema mostrará un mensaje de advertencia y permitirá al usuario corregir los valores o confirmar si es intencional.
* **FA-3.1.4 (No Autorizado):** Si el token de autenticación expira durante el proceso de creación, el sistema responderá con 401 y redirigirá al usuario a la pantalla de login. Los datos ingresados se perderán.
* **FA-3.1.5 (Error de Persistencia):** Si falla el guardado de la publicación en la base de datos, el sistema mostrará un mensaje de error (ej. "Error al guardar la publicación. Por favor, inténtalo de nuevo") y permitirá al usuario reintentar el guardado sin perder los datos ingresados.
* **FA-3.1.6 (Navegación Atrás):** Si el usuario selecciona "Back" o navega hacia atrás, el sistema conservará los datos ingresados en los pasos anteriores y permitirá editarlos antes de continuar.
* **FA-3.1.7 (Cancelación Confirmada):** Si el usuario selecciona "Cancel", el sistema puede mostrar una confirmación (opcional) preguntando si está seguro de descartar los cambios, especialmente si ya ha ingresado datos en múltiples pasos.


### RF-4.1: Chat Privado entre Usuarios

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-4.1 |
| **Nombre** | Comunicación en Tiempo Real mediante Chat Privado |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir a usuarios autenticados comunicarse en tiempo real mediante mensajes privados, permitiendo el envío y recepción de mensajes de texto, visualización del historial de conversaciones y notificaciones de nuevos mensajes. |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario autenticado accede a la sección de "Chat" o "Mensajes" desde el menú principal.
2. El sistema muestra una lista de conversaciones activas, ordenadas por fecha del último mensaje (más recientes primero), mostrando:
   * Avatar y nombre del otro usuario.
   * Vista previa del último mensaje (máximo 50 caracteres).
   * Indicador de mensajes no leídos (badge con contador).
   * Timestamp del último mensaje (formato relativo: "hace 5 min", "hoy", "ayer", fecha completa).
3. El usuario puede:
   * Seleccionar una conversación existente para continuarla.
   * Iniciar una nueva conversación buscando y seleccionando un usuario desde la lista de contactos o mediante búsqueda.
4. Al abrir una conversación, el sistema muestra:
   * El historial de mensajes (cargado de forma paginada, mostrando los más recientes primero).
   * Un campo de entrada de texto para escribir mensajes.
   * Indicadores de estado de mensajes (enviado, entregado, leído).
5. El usuario escribe un mensaje y presiona "Enviar" o la tecla Enter.
6. El sistema:
   * Envía el mensaje mediante WebSocket para comunicación en tiempo real.
   * Muestra el mensaje inmediatamente en la interfaz con estado "enviando" y luego "enviado".
   * Persiste el mensaje en la base de datos.
   * Notifica al destinatario si está conectado; si no, almacena la notificación para cuando se conecte.
7. Si el destinatario está en línea, recibe el mensaje en tiempo real y se actualiza su interfaz.
8. El sistema marca los mensajes como "entregado" cuando el destinatario los recibe y como "leído" cuando los visualiza.

#### 2. Reglas de Negocio y Restricciones
* **RN-4.1.1 (Autorización):** Solo usuarios autenticados pueden enviar y recibir mensajes. Un usuario solo puede ver sus propias conversaciones.
* **RN-4.1.2 (Longitud de Mensaje):** Los mensajes de texto tienen una longitud máxima de 2000 caracteres. Mensajes vacíos o solo espacios en blanco no se permiten.
* **RN-4.1.3 (Comunicación en Tiempo Real):** El sistema utilizará WebSockets . La conexión se mantendrá activa mientras el usuario esté en la aplicación.
* **RN-4.1.4 (Persistencia):** Todos los mensajes se almacenan permanentemente en la base de datos para permitir el acceso al historial, incluso si el usuario se desconecta y vuelve a conectarse.
* **RN-4.1.5 (Estados de Mensaje):** Los mensajes tendrán estados: "enviando", "enviado", "entregado" (recibido por el destinatario) y "leído" (visualizado por el destinatario). El estado "leído" se actualiza cuando el destinatario abre la conversación y visualiza el mensaje.
* **RN-4.1.6 (Notificaciones):** Si el destinatario no está conectado o no tiene la conversación abierta, el sistema enviará una notificación push (si está habilitada) y actualizará el contador de mensajes no leídos.
* **RN-4.1.7 (Paginación del Historial):** El historial de mensajes se cargará de forma paginada (p.ej., 50 mensajes por página) para optimizar el rendimiento. El usuario podrá cargar mensajes más antiguos mediante scroll hacia arriba o botón "Cargar más".
* **RN-4.1.8 (Orden de Mensajes):** Los mensajes se mostrarán en orden cronológico (más antiguos arriba, más recientes abajo) con auto-scroll al último mensaje al abrir la conversación.
* **RN-4.1.9 (Bloqueo de Usuarios):** Si un usuario bloquea a otro, no podrán enviarse mensajes entre sí. El sistema verificará esta restricción antes de permitir el envío.
* **RN-4.1.10 (Rate Limiting):** Se limitará el número de mensajes que un usuario puede enviar por minuto (p.ej., 30 mensajes/minuto) para prevenir spam y abuso.

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-4.1.1 (Usuario No Encontrado):** Si el usuario intenta iniciar una conversación con un usuario que no existe o no está disponible, el sistema responderá con 404 (código `user_not_found`).
* **FA-4.1.2 (Mensaje Vacío o Inválido):** Si el mensaje está vacío, solo contiene espacios o excede la longitud máxima, el sistema no lo enviará
* **FA-4.1.3 (Conexión Perdida):** Si se pierde la conexión WebSocket, el sistema intentará reconectar automáticamente. Los mensajes escritos durante la desconexión se guardarán localmente y se enviarán cuando se restaure la conexión.



### RF-5.1: Feed de Publicaciones

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-5.1 |
| **Nombre** | Visualización del Feed de Publicaciones de Amigos |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir a usuarios autenticados visualizar un feed con publicaciones de sus amigos, ordenadas cronológicamente, con capacidad de scroll infinito, interacción mediante likes y acceso mediante el logotipo de Trainhub. |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario autenticado pulsa el logotipo de "Trainhub" en el Navbar o accede a la página principal.
2. El sistema redirige al usuario al Feed de publicaciones.
3. El sistema carga y muestra las publicaciones de los amigos del usuario, ordenadas por fecha de publicación (más recientes primero), mostrando:
   * Avatar y nombre del usuario que publicó.
   * La publicación completa con formato estilo Instagram (deslizable entre overview, running, workouts).
   * Timestamp de la publicación (formato relativo: "hace 2 horas", "hoy", "ayer", fecha completa).
   * Contador de likes (número total de likes).
   * Indicador visual si el usuario actual ya dio like a la publicación (corazón relleno vs. vacío).
   * Botón de like (corazón).
4. El usuario puede:
   * Hacer scroll vertical para ver más publicaciones (scroll infinito).
   * Deslizar horizontalmente dentro de cada publicación para ver las diferentes páginas (overview, running, workouts).
   * Pulsar el botón de like para dar o quitar like a una publicación.
5. Cuando el usuario pulsa el botón de like:
   * Si la publicación no tiene like del usuario, el sistema:
     * Marca la publicación como "liked" por el usuario.
     * Incrementa el contador de likes.
     * Actualiza el icono del botón (corazón vacío → corazón relleno).
     * Persiste el like en la base de datos.
   * Si la publicación ya tiene like del usuario, el sistema:
     * Quita el like de la publicación.
     * Decrementa el contador de likes.
     * Actualiza el icono del botón (corazón relleno → corazón vacío).
     * Elimina el like de la base de datos.
6. El sistema carga más publicaciones automáticamente cuando el usuario se acerca al final del scroll (paginación infinita o carga bajo demanda).
7. El usuario puede hacer clic en el avatar o nombre de un usuario para acceder a su perfil.

#### 2. Reglas de Negocio y Restricciones
* **RN-5.1.1 (Autorización):** Solo usuarios autenticados pueden acceder al Feed. El Feed mostrará únicamente publicaciones de usuarios que son amigos del usuario actual.
* **RN-5.1.2 (Orden de Publicaciones):** Las publicaciones se mostrarán en orden cronológico descendente (más recientes primero). Si dos publicaciones tienen la misma fecha/hora, se ordenarán por ID descendente.
* **RN-5.1.3 (Paginación):** El Feed utilizará paginación infinita o carga bajo demanda. Se cargarán inicialmente las primeras N publicaciones (p.ej., 20) y se cargarán más cuando el usuario haga scroll cerca del final.
* **RN-5.1.4 (Likes):** Cada usuario puede dar like a una publicación una sola vez. Un usuario puede quitar su like y volver a darlo posteriormente. El contador de likes muestra el número total de usuarios únicos que han dado like.
* **RN-5.1.5 (Actualización en Tiempo Real):** El Feed puede actualizarse en tiempo real cuando un amigo publica una nueva publicación (opcional, mediante WebSocket o polling). Si no hay actualización en tiempo real, el usuario puede refrescar manualmente.
* **RN-5.1.6 (Acceso al Feed):** El logotipo de Trainhub en el Navbar siempre redirigirá al Feed principal, independientemente de la página actual del usuario.
* **RN-5.1.7 (Publicaciones Vacías):** Si el usuario no tiene amigos o sus amigos no han publicado nada, el Feed mostrará un mensaje indicando que no hay publicaciones disponibles y sugerirá agregar amigos o crear una publicación.
* **RN-5.1.8 (Rendimiento):** El sistema optimizará la carga de publicaciones para evitar sobrecarga. Las imágenes y contenido pesado se cargarán de forma lazy (bajo demanda) cuando el usuario haga scroll.
* **RN-5.1.9 (Visibilidad de Publicaciones):** Solo se mostrarán publicaciones de usuarios que son amigos mutuos. 
* **RN-5.1.10 (Interacción con Publicaciones):** El usuario puede interactuar con las publicaciones mediante likes. Otras interacciones (comentarios, compartir) pueden estar fuera del alcance del MVP.

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-5.1.1 (No Autorizado):** Si el token de autenticación expira o es inválido, el sistema responderá con 401 y redirigirá al usuario a la pantalla de login.
* **FA-5.1.2 (Error al Cargar Publicaciones):** Si falla la carga de publicaciones desde el servidor, el sistema mostrará un mensaje de error (ej. "Error al cargar publicaciones. Por favor, inténtalo de nuevo") y permitirá al usuario reintentar la carga.
* **FA-5.1.3 (Error al Dar Like):** Si falla el proceso de dar/quitar like (p.ej., error de red o servidor), el sistema mostrará un mensaje de error temporal y revertirá el estado visual del botón de like al estado anterior. El usuario podrá intentar nuevamente.
* **FA-5.1.4 (Feed Vacío):** Si el usuario no tiene amigos o no hay publicaciones disponibles, el sistema mostrará un mensaje amigable (ej. "Aún no hay publicaciones. ¡Agrega amigos o crea tu primera publicación!") en lugar de un feed vacío.
* **FA-5.1.6 (Publicación Eliminada):** Si una publicación es eliminada por su autor mientras el usuario está viendo el Feed, el sistema actualizará el Feed la próxima vez que se recargue, removiendo la publicación eliminada.


### RF-6.1: Búsqueda de Usuarios

| Atributo | Detalle |
| :--- | :--- |
| **Identificador** | RF-6.1 |
| **Nombre** | Búsqueda y Localización de Usuarios |
| **Prioridad** | Alta |
| **Fuente** | Cliente/Usuario |
| **Descripción** | El sistema deberá permitir a usuarios autenticados buscar otros usuarios mediante un campo de búsqueda que se activa al pulsar el icono de lupa en el Navbar, mostrando resultados en tiempo real mientras el usuario escribe. |

#### 1. Pasos de la Interacción (Flujo Principal)
1. El usuario autenticado pulsa el icono de lupa (🔍) situado en el Navbar.
2. El sistema transforma el icono de lupa en un campo de entrada de texto (input) en el mismo lugar del Navbar, manteniendo el icono de lupa visible dentro o al lado del campo.
3. El campo de entrada se activa automáticamente (focus) y el cursor se posiciona dentro del campo, listo para escribir.
4. El usuario comienza a escribir el nombre o término de búsqueda en el campo de entrada.
5. Mientras el usuario escribe (búsqueda en tiempo real o con debounce), el sistema:
   * Realiza la búsqueda en la base de datos de usuarios.
   * Muestra los resultados de búsqueda en un dropdown o panel desplegable debajo del campo de búsqueda.
6. Los resultados de búsqueda muestran para cada usuario encontrado:
   * Avatar (foto de perfil) o placeholder si no tiene foto.
   * Nombre del usuario.
   * Indicador de estado de amistad (si es amigo, si ya se envió solicitud, si no hay relación).
   * Botón de acción (ej. "Agregar amigo", "Ver perfil", "Enviar mensaje").
7. El usuario puede:
   * Hacer clic en un resultado para acceder al perfil del usuario.
   * Interactuar con los botones de acción disponibles en cada resultado.
   * Continuar escribiendo para refinar la búsqueda.
   * Pulsar Escape o hacer clic fuera del campo para cerrar la búsqueda y volver al icono de lupa.
8. Si el usuario hace clic fuera del campo de búsqueda o pulsa Escape, el sistema:
   * Cierra el panel de resultados.
   * Restaura el icono de lupa en el Navbar (el campo de entrada desaparece).
   * Limpia el contenido de búsqueda.

#### 2. Reglas de Negocio y Restricciones
* **RN-6.1.1 (Autorización):** Solo usuarios autenticados pueden realizar búsquedas. Los resultados mostrarán únicamente usuarios que no están bloqueados y que no han bloqueado al usuario actual.
* **RN-6.1.2 (Longitud Mínima de Búsqueda):** La búsqueda se activará cuando el usuario haya ingresado al menos 2 caracteres. Con menos de 2 caracteres, no se mostrarán resultados o se mostrará un mensaje indicando que se necesitan más caracteres.
* **RN-6.1.3 (Búsqueda en Tiempo Real):** El sistema realizará la búsqueda mientras el usuario escribe, con un debounce de aproximadamente 300-500ms para evitar sobrecarga del servidor. La búsqueda se ejecutará automáticamente sin necesidad de presionar Enter o un botón de búsqueda.
* **RN-6.1.4 (Criterios de Búsqueda):** La búsqueda se realizará sobre el nombre del usuario (coincidencia parcial, case-insensitive). El sistema buscará coincidencias que comiencen con el término ingresado o lo contengan.
* **RN-6.1.5 (Límite de Resultados):** Se mostrarán un máximo de 10-20 resultados en el dropdown. Si hay más resultados, se mostrará un indicador (ej. "Mostrando 10 de 25 resultados") y opción para ver más resultados en una página dedicada.
* **RN-6.1.6 (Exclusión del Usuario Actual):** El usuario actual no aparecerá en los resultados de búsqueda.
* **RN-6.1.7 (Visibilidad de Resultados):** Los resultados mostrarán información básica del usuario (nombre, avatar). El estado de amistad se indicará visualmente para ayudar al usuario a decidir qué acción tomar.
* **RN-6.1.8 (Persistencia del Campo):** El campo de búsqueda permanecerá activo mientras el usuario esté interactuando con él. Si el usuario navega a otra página, el campo se cerrará y se restaurará el icono de lupa.
* **RN-6.1.9 (Búsqueda Vacía):** Si el usuario borra todo el texto del campo, el sistema ocultará los resultados y mostrará el campo vacío, listo para una nueva búsqueda.
* **RN-6.1.10 (Acceso Rápido):** El usuario puede presionar Enter cuando hay resultados para acceder al primer resultado, o usar las flechas del teclado para navegar entre resultados.

#### 3. Flujos Alternativos (Manejo de Errores)
* **FA-6.1.1 (No Autorizado):** Si el token de autenticación expira o es inválido durante la búsqueda, el sistema responderá con 401 y redirigirá al usuario a la pantalla de login.
* **FA-6.1.2 (Error de Búsqueda):** Si falla la búsqueda en el servidor (error de red o del servidor), el sistema mostrará un mensaje de error en el área de resultados (ej. "Error al buscar usuarios. Por favor, inténtalo de nuevo") y permitirá al usuario reintentar.
* **FA-6.1.3 (Sin Resultados):** Si no se encuentran usuarios que coincidan con el término de búsqueda, el sistema mostrará un mensaje en el dropdown (ej. "No se encontraron usuarios con ese nombre") en lugar de mostrar una lista vacía.
* **FA-6.1.4 (Conexión Lenta):** Si la conexión es lenta, el sistema mostrará un indicador de carga (spinner) en el área de resultados mientras se realiza la búsqueda. El usuario podrá seguir escribiendo durante la carga.
* **FA-6.1.5 (Búsqueda Muy Corta):** Si el usuario ingresa menos de 2 caracteres, el sistema no realizará la búsqueda y puede mostrar un mensaje sutil (ej. "Ingresa al menos 2 caracteres") o simplemente no mostrar resultados hasta que se cumpla el mínimo.
* **FA-6.1.6 (Caracteres Especiales):** Si el usuario ingresa caracteres especiales o símbolos que no son válidos para nombres, el sistema realizará la búsqueda normalmente pero puede mostrar resultados vacíos si no hay coincidencias. No se mostrará error por caracteres inválidos.
* **FA-6.1.7 (Timeout de Búsqueda):** Si la búsqueda tarda demasiado (timeout), el sistema mostrará un mensaje indicando que la búsqueda está tardando y permitirá al usuario cancelar o reintentar.

# REqwuisitos no funcionales

- **Accesibilidad:**  debe funcionar en navegadores modernos (chrome y firefox) y ser resposinve.
- **Rendimineto**

