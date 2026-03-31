package com.waitless.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.user.dto.RegisterClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakProvisioningService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${waitless.keycloak.base-url}")
    private String keycloakBaseUrl;

    @Value("${waitless.keycloak.realm}")
    private String realm;

    @Value("${waitless.keycloak.admin.username}")
    private String adminUsername;

    @Value("${waitless.keycloak.admin.password}")
    private String adminPassword;

    public String createClientUser(RegisterClientRequest request) {
        return createUserWithRole(request.getName(), request.getEmail(), request.getPassword(), "CLIENT");
    }

    public String createAgentUser(String name, String email, String password) {
        return createUserWithRole(name, email, password, "AGENT");
    }

    private String createUserWithRole(String name, String email, String password, String role) {
        String adminToken = getAdminAccessToken();
        String userId = createUserInKeycloak(adminToken, name, email, password);
        assignRealmRole(adminToken, userId, role);
        return userId;
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakBaseUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        body.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Keycloak admin token request failed. status=" + response.getStatusCode());
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.get("access_token").asText();
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Missing access_token in Keycloak response");
            }
            return accessToken;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Keycloak admin token response", e);
        }
    }

    private String createUserInKeycloak(String adminToken, String name, String email, String password) {
        String usersUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String fullName = name.trim();
        String[] parts = fullName.split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        Map<String, Object> passwordCreds = new HashMap<>();
        passwordCreds.put("type", "password");
        passwordCreds.put("value", password);
        passwordCreds.put("temporary", false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", email);
        payload.put("email", email);
        payload.put("firstName", firstName);
        if (!lastName.isBlank()) {
            payload.put("lastName", lastName);
        }
        payload.put("enabled", true);
        payload.put("emailVerified", true);
        payload.put("credentials", List.of(passwordCreds));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(usersUrl, HttpMethod.POST, entity, Void.class);

        String location = response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : null;

        if (location == null || location.isBlank()) {
            log.warn("Keycloak user create response has no Location header. Falling back to lookup by email.");
            return getUserIdByEmail(adminToken, email);
        }

        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String getUserIdByEmail(String adminToken, String email) {
        String lookupUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(lookupUrl, HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Keycloak user lookup failed. status=" + response.getStatusCode());
        }

        try {
            JsonNode arr = objectMapper.readTree(response.getBody());
            if (!arr.isArray() || arr.isEmpty()) {
                throw new IllegalStateException("User not found in Keycloak after creation. email=" + email);
            }
            JsonNode first = arr.get(0);
            String id = first.get("id").asText();
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Keycloak lookup returned empty id. email=" + email);
            }
            return id;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Keycloak user lookup response", e);
        }
    }

    private void assignRealmRole(String adminToken, String userId, String roleName) {
        String roleId = getRealmRoleId(adminToken, roleName);

        String roleMappingsUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> rolePayload = new HashMap<>();
        rolePayload.put("id", roleId);
        rolePayload.put("name", roleName);
        rolePayload.put("composite", false);
        rolePayload.put("clientRole", false);

        List<Map<String, Object>> payload = List.of(rolePayload);
        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.exchange(roleMappingsUrl, HttpMethod.POST, entity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new IllegalStateException("Keycloak role mapping failed. status=" + response.getStatusCode());
        }
    }

    private String getRealmRoleId(String adminToken, String roleName) {
        String rolesUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/roles";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(rolesUrl, HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Keycloak roles lookup failed. status=" + response.getStatusCode());
        }

        try {
            JsonNode arr = objectMapper.readTree(response.getBody());
            for (JsonNode node : arr) {
                if (roleName.equals(node.get("name").asText())) {
                    return node.get("id").asText();
                }
            }
            throw new IllegalStateException("Role not found in Keycloak realm: " + roleName);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Keycloak roles lookup response", e);
        }
    }
}

