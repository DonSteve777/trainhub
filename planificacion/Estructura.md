


# 🏗️ Estructura

Arquitectura monolítica modular con un microservicio de notificaciones ?

```java
com.tfg.redsocial
│
├─ controller      // endpoints REST
├─ service         // lógica de negocio
├─ repository      // acceso a la DB (Spring Data JPA)
├─ model           // entidades de la base de datos
├─ dto             // objetos de transferencia (opcional pero útil)
├─ security        // autenticación y autorización
└─ config          // configuración de Spring Boot, CORS, JWT, etc.
```

# Java


# 🍃 Spring 

inyección de dependencias, manejo de configuración, seguridad, y un ecosistema estable

⚠️ Caveat: No te pases creando microservicios ni cosas súper complejas; el objetivo es que 
funcione y se vea profesional, no que sea una copia de Facebook a escala industrial.

- Organización y escalabilidad
- Seguridad
- Integración con BDD
- API REST
- Buenas prácticas