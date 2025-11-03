FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Pre-descarga dependencias para acelerar builds
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Copia el resto del proyecto (incluye src y mvnw si lo usas)
COPY . .

# Arranque en modo desarrollo con hot-reload
CMD ["mvn","spring-boot:run","-Dspring-boot.run.fork=false"]