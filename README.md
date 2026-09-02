# TrainHub

Aplicación web de red social deportiva desarrollada como Trabajo de Fin de Grado.

**Stack:** Angular 21 · Spring Boot 3 (Java 17) · PostgreSQL 17 · Docker

---

## Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución

Para el envío de emails (SMTP / Gmail):

- Cuenta de Gmail con [verificación en dos pasos](https://myaccount.google.com/security) activada
- Una [contraseña de aplicación de Google](https://myaccount.google.com/apppasswords)

---

## Despliegue con Docker (recomendado)

Todos los comandos de esta sección se ejecutan desde la carpeta `deploy/`.

### 1. Clonar el repositorio

```bash
git clone https://github.com/DonSteve777/trainhub.git
cd trainhub/deploy
```

### 2. Configurar las credenciales de email

- `.env.example` — plantilla versionada en el repo (qué variables hacen falta)
- `.env` — credenciales reales de cada persona; **no se sube** al repositorio

```bash
# Linux / macOS / Git Bash
cp .env.example .env

# Windows (cmd / PowerShell)
copy .env.example .env
```

Edita `.env`:

```
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx
```

### 3. Levantar los servicios

```bash
docker compose up --build
```

Construye las imágenes de backend y frontend, y arranca Postgres, backend, frontend y pgAdmin. La primera vez puede tardar varios minutos.

El esquema de la base de datos se crea automáticamente con `bdd/schema.sql` la primera vez que se inicializa el volumen de Postgres.

### 4. Poblar datos de ejemplo

Con los contenedores en marcha, desde `deploy/`:

```powershell
# Windows (PowerShell)
.\seed.ps1
```

```bash
# Linux / macOS / Git Bash
bash seed.sh
```

El script:

1. **Usuarios** — los registra vía API REST para que las contraseñas queden en BCrypt. Si ya existen, los omite. Contraseña de todos: `password123`
2. **Resto de datos** — vacía posts, amistades, comentarios, likes, goals, etc., y los vuelve a insertar desde `bdd/inserts.sql`. La tabla `users` no se trunca en este paso.

Se puede ejecutar varias veces: deja la base en un estado limpio y consistente (salvo los usuarios, que se reutilizan).

> **Usuarios de ejemplo** (password `password123`):
> `pedro.alonso@example.com`, `lucia.vega@example.com`, `javier.mena@example.com`

### 5. Acceder a la aplicación

| Servicio | URL |
| -------- | --- |
| Frontend | http://localhost:4200 |
| Backend  | http://localhost:8080 |
| pgAdmin  | http://localhost:5050 |

**Credenciales de pgAdmin**

| Campo    | Valor             |
| -------- | ----------------- |
| Email    | admin@trainhub.com |
| Password | admin123          |

Para registrar el servidor PostgreSQL dentro de pgAdmin (contenedor Docker):

| Campo    | Valor           |
| -------- | --------------- |
| Host     | `postgres`      |
| Port     | `5432`          |
| Database | trainhub_dev_db |
| Username | dev_user        |
| Password | dev_pass        |

> Desde pgAdmin de escritorio en el host Windows, usa host `localhost` y puerto `5431`.

### Parar los servicios

Desde `deploy/`:

```bash
docker compose down
```

Para parar y borrar también los datos de Postgres:

```bash
docker compose down -v
```

---

## Desarrollo local (sin contenedores de app)

### Requisitos adicionales

- Java 17
- Maven 3.9+ (o el wrapper `./mvnw` del backend)
- Node.js 22 y npm 11.6+
- PostgreSQL 17, o solo el contenedor de Postgres:

```bash
cd deploy
docker compose up -d postgres
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Arranca en http://localhost:8080.

Define `MAIL_USERNAME` y `MAIL_PASSWORD` en el sistema o en el IDE. El backend se conecta a Postgres en `localhost:5431` (puerto publicado del contenedor).

### Frontend

```bash
cd frontend
npm install
npm start
```

Arranca en http://localhost:4200.

---

## Estructura del proyecto

```
trainhub/
├── backend/                 # API REST Spring Boot
├── frontend/                # SPA Angular
├── design/                  # Documentación y diseño
├── bdd/                     # Esquema SQL e inserts de ejemplo
│   ├── schema.sql
│   └── inserts.sql
└── deploy/                  # Docker, seed y variables de entorno
    ├── docker-compose.yml
    ├── Dockerfile.backend
    ├── Dockerfile.frontend
    ├── nginx.conf
    ├── seed.ps1             # seed en Windows (PowerShell)
    ├── seed.sh              # seed en Linux / macOS / Git Bash
    ├── seed-users.json
    └── .env.example
```
