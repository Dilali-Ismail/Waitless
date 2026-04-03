# Guide de Sécurisation Spring Boot avec Keycloak & `@PreAuthorize`

Ce guide explique étape par étape comment configurer vos microservices (`user-service`, `queue-service`, `ticket-service`) pour qu'ils sécurisent leurs API (endpoints) en validant les jetons JWT émis par Keycloak, et comment utiliser l'annotation `@PreAuthorize` pour la gestion des rôles.

---

## 1. Ajouter les dépendances (pom.xml)

Dans chaque microservice que vous souhaitez sécuriser, ajoutez ces deux dépendances Spring Boot dans votre fichier `pom.xml` :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

---

## 2. Configuration dans `application.yml`

Précisez l'URI complet de votre *realm* Keycloak. Cette information est primordiale pour que Spring télécharge automatiquement la clé publique de Keycloak (nécessaire pour vérifier la pertinence du JWT).

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/waitless-realm # À adapter si vous lancez dans un container docker
```

*(Note : Dans `docker-compose.yml`, utilisez `http://keycloak:8080/realms/waitless-realm` pour permettre la communication de container à container).*

---

## 3. Convertir les Rôles de Keycloak (KeycloakRoleConverter)

Par défaut, Spring Security recherche les habilitations (authorities) dans un attribut JWT appelé `scope` ou `scp`. Par contre, Keycloak place les rôles par défaut dans un attribut nommé `realm_access.roles`. Il faut donc un « convertisseur » pour expliquer à Spring où chercher ces rôles.

Créez une nouvelle classe `KeycloakRoleConverter` dans un package de configuration (ex: `config/security`) :

```java
package com.waitless.user.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        // Spring Security s'attend à ce que les rôles commencent par "ROLE_"
        return roles.stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```

---

## 4. Activer et Configurer Spring Security (SecurityConfig)

Maintenant, créez la classe de configuration principale pour la sécurité. Cette classe s'assure que :
1. Toutes les requêtes HTTP sont authentifiées (sauf mention contraire).
2. L'authentification OAuth2 Resource Server (en utilisant un JWT) est activée.
3. Le convertisseur que vous avez créé est branché au mécanisme d'authentification.
4. Les annotations de type `@PreAuthorize` sont autorisées.

```java
package com.waitless.user.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Obligatoire pour utiliser @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Désactivé pour les APIs Stateless (REST)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll() // Autoriser les tests de santé publics
                .anyRequest().authenticated() // Les autres requêtes nécessitent d'être connecté
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    // Connecte le convertisseur Keycloak à l'authentification Spring
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return jwtConverter;
    }
}
```

---

## 5. Utiliser l'annotation `@PreAuthorize`

Maintenant, vous pouvez protéger sereinement chaque fonction de vos Controllers avec l'annotation `@PreAuthorize`. Spring utilisera les listes de rôles fraîchement mappées par le `KeycloakRoleConverter`.

### Exemples d'implémentation dans un `Controller` :

```java
package com.waitless.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Ne s'exécutera que si le JWT détient le rôle "ADMIN"
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnlyEndpoint() {
        return "Bonjour Admin, voici des informations sensibles.";
    }

    // Ne s'exécutera que si le JWT détient le rôle "USER"
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userEndpoint() {
        return "Accès utilisateur autorisé !";
    }

    // Ne s'exécutera que si le JWT détient "ADMIN" OU "MANAGER"
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteUser(@PathVariable Long id) {
        return "Utilisateur " + id + " supprimé par le manager.";
    }
}
```

---

## 🚀 Récapitulatif

1. **Le Token JWT** contient des rôles Keycloak : `realm_access: { "roles": ["ADMIN"] }`.
2. **`KeycloakRoleConverter`** intercepte le Token et lit ces valeurs pour les transformer en : `[ROLE_ADMIN]`.
3. **`SecurityConfig`** paramètre le filtre Spring pour analyser le token et en extraire de manière transparente les `GrantedAuthority`.
4. **`@PreAuthorize("hasRole('ADMIN')")`** valide l'accès sur la fonction : c'est simple, transparent, et efficace.

Bon développement !
