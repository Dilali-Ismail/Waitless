FROM maven:3.9-eclipse-temurin-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le pom.xml d'abord (pour cache Docker)
COPY pom.xml .

# Télécharger les dépendances (mis en cache si pom.xml ne change pas)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler et packager (skip tests pour aller plus vite en dev)
RUN mvn clean package -DskipTests

# ==================== STAGE 2 : RUNTIME ====================
FROM eclipse-temurin:17-jre-alpine

# Créer un user non-root (sécurité)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Répertoire de travail
WORKDIR /app

# Copier le JAR depuis le stage build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8081

# Healthcheck (Docker vérifie que le service est UP)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]