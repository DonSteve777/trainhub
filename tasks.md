# Comandos utiless
docker compose up
docker exec -i repo-postgres-1 psql -U postgres < init.sql
docker exec -i repo-postgres-1 psql -U dev_user -d trainhub_dev_db < schema.sql
mvn spring-boot:run
taskkill /F /IM java.exespring-boot:run
tasklist | findstr /i "java"


ESLint 