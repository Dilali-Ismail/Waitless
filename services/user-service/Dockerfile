FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier d'abord pom.xml (cache Docker layer)
COPY pom.xml .

# Télécharger dépendances (layer caché si pom.xml ne change pas)
RUN mvn dependency:go-offline

# Copier le code source
COPY src ./src

# Compiler et packager
RUN mvn clean package -DskipTests

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
