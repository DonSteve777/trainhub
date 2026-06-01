# TrainHub

Aplicación web de red social deportiva desarrollada como Trabajo de Fin de Grado.

**Stack:** Angular 21 · Spring Boot 3 (Java 17) · PostgreSQL 17 · Docker

---

## Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución
Para usar  el SMTP:
- Una cuenta de Gmail con [verificación en dos pasos](https://myaccount.google.com/security) activada
- Una [contraseña de aplicación de Google](https://myaccount.google.com/apppasswords) generada para el envío de emails


---

## Despliegue con Docker (recomendado)

### 1. Clonar el repositorio

```bash
git clone https://github.com/DonSteve777/trainhub.git
cd repo
```

### 2. Configurar las credenciales de email

Nota:
.env.example → se sube al repo, muestra qué variables hacen falta
.env → cada persona lo crea con sus propias credenciales, nunca se sube

Copia la plantilla de variables de entorno y rellena tus credenciales:

```bash
cp .env.example .env
```

Edita el archivo `.env`:

```
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx   # contraseña de aplicación de Google
```

### 3. Levantar todos los servicios

```bash
docker compose up --build
```

Este comando construye las imágenes del backend y el frontend, y arranca los tres contenedores (base de datos, backend y frontend). La primera vez tarda varios minutos.

La base de datos se inicializa automáticamente con el esquema y los datos de ejemplo incluidos en `schema.sql` e `inserts.sql`.

### 4. Poblar la base de datos con datos de ejemplo: ejecutar en bash

```bash
bash seed.sh
```

Este script realiza dos operaciones:

1. **Usuarios** — los registra vía API REST (no directamente en SQL) para que las contraseñas queden encriptadas con BCrypt. Si los usuarios ya existen, los omite sin error. Las contraseñas son siempre la misma: password123
2. **Resto de datos** — vacía completamente las tablas `posts`, `friendships`, `comments`, `comment_likes` y `post_likes`, y las vuelve a poblar desde cero. La tabla `users` **no se toca** en este paso.

Se puede ejecutar varias veces: siempre deja la base de datos en un estado limpio y consistente.

> **Usuarios de ejemplo** — todos con contraseña `password123`:
> `pedro.alonso@example.com`, `lucia.vega@example.com`, `javier.mena@example.com`, …

### 5. Acceder a la aplicación

| Servicio  | URL                          |
|-----------|------------------------------|
| Frontend  | http://localhost:4200        |
| Backend   | http://localhost:8080        |
| pgAdmin   | http://localhost:5050        |

**Credenciales de pgAdmin:**

| Campo    | Valor                  |
|----------|------------------------|
| Email    | admin@trainhub.com     |
| Password | admin123               |

Una vez dentro, para conectar al servidor PostgreSQL usa:

| Campo    | Valor             |
|----------|-------------------|
| Host     | postgres          |
| Port     | 5432              |
| Database | trainhub_dev_db   |
| Username | dev_user          |
| Password | dev_pass          |

### Parar los servicios

```bash
docker compose down
```

Para parar y eliminar también los datos de la base de datos:

```bash
docker compose down -v
```

---

## Desarrollo local (sin Docker)

### Requisitos adicionales

- Java 17
- Maven 3.9+
- Node.js 22 y npm 11.6+
- PostgreSQL 17 (o usar solo el contenedor de Postgres: `docker compose up postgres`)

### Base de datos

```bash
docker compose up postgres
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

El backend arranca en http://localhost:8080.

> Las variables de entorno `MAIL_USERNAME` y `MAIL_PASSWORD` deben estar definidas en el sistema, o configuradas en el IDE antes de arrancar.

### Frontend

```bash
cd frontend
npm install
npm start
```

El frontend arranca en http://localhost:4200.

---

## Estructura del proyecto

```
repo/
├── backend/              # API REST Spring Boot
├── frontend/             # SPA Angular
├── design/               # Documentación y diseño
├── schema.sql            # Esquema de la base de datos
├── inserts.sql           # Datos de ejemplo
├── docker-compose.yml    # Orquestación de servicios
├── Dockerfile.backend
├── Dockerfile.frontend
├── nginx.conf
└── .env.example          # Plantilla de variables de entorno
```
