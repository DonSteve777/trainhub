# Comandos utiless
docker compose up
docker exec -i repo-postgres-1 psql -U postgres < init.sql
docker exec -i repo-postgres-1 psql -U dev_user -d trainhub_dev_db < schema.sql

docker exec -i 25ffa7b2423beb216abf525e45cbdb0a58c54f2b04448f1a29e655480c54f712 psql -U dev_user -d trainhub_dev_db < schema.sql

mvn spring-boot:run
taskkill /F /IM java.exespring-boot:run
tasklist | findstr /i "java"


- [ ] llamar al endpoint de prueba de jwt
  - [ ] importar en psotman la coleccion
- [ ] página de login