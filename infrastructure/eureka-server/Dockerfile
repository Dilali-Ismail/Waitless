# Étape 1 : Build (compile le .jar)
FROM maven:3.9-eclipse-temurin-17 AS build
# Pourquoi cette image ? Maven 3.9 + Java 17 (ce qu'on utilise)

WORKDIR /app
# Crée et se place dans /app dans le container

COPY pom.xml .
# Copie d'abord pom.xml seul (pour cache Docker layers)

RUN mvn dependency:go-offline
# Télécharge toutes les dépendances Maven
# Pourquoi séparé ? Si pom.xml ne change pas, Docker utilise le cache

COPY src ./src
# Maintenant copie le code source

RUN mvn clean package -DskipTests
# Compile et package en .jar
# -DskipTests : pas de tests (on va vite pour le dev)

# Étape 2 : Runtime (exécute le .jar)
FROM eclipse-temurin:17-jre-alpine
# Pourquoi JRE et pas JDK ? Plus léger (on n'a plus besoin de compiler)
# Pourquoi alpine ? Image Linux ultra-légère (5MB vs 200MB)

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
# Copie le .jar depuis l'étape build

EXPOSE 8761
# Documente que le container écoute sur 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
# Commande pour démarrer l'app