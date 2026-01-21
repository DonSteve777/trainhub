# Comandos utiless
docker compose up
docker exec -i repo-postgres-1 psql -U postgres < init.sql
docker exec -i repo-postgres-1 psql -U dev_user -d trainhub_dev_db < schema.sql
mvn spring-boot:run
taskkill /F /IM java.exespring-boot:run
tasklist | findstr /i "java"


# AUTH
[ ] verificacion por email
    // AccountStatus.PENDING_CONFIRMATION
        AccountStatus.ACTIVE    // temporal, meintras el email confirmacion no esta implementado

[ ] // En una implementación real, aquí se enviaría el email usando un servicio como
        // Spring Mail, SendGrid, AWS SES, etc.

[x ] probar endpoint /api/auth/register:
    respuestas del endpoint ✔️
    validaciones ✔️

[ ] login


---

# Producción
- [ ] Revisar comentarios de producción 
    .properties
