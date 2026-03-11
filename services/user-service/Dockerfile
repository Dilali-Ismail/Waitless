FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier pom.xml + sources
COPY pom.xml .
COPY src ./src

# Compiler et packager (télécharge les deps + build en une seule étape)
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════════
# ÉTAPE 2 : RUNTIME (Exécution)
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR depuis l'étape build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8083

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
