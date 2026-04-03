package com.waitless.user.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convertisseur de rôles Keycloak → Spring Security.
 *
 * Keycloak place les rôles dans le claim "realm_access.roles" du JWT.
 * Spring Security, par défaut, cherche les rôles dans le claim "scope".
 * Ce convertisseur fait le pont entre les deux.
 *
 * Exemple de JWT Keycloak :
 * {
 *   "realm_access": {
 *     "roles": ["CLIENT", "default-roles-waitless-realm"]
 *   }
 * }
 *
 * Résultat : Spring Security reconnaîtra ROLE_CLIENT et ROLE_default-roles-waitless-realm
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Récupère le bloc "realm_access" du JWT
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }

        // Récupère la liste de rôles et les préfixe avec "ROLE_"
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
