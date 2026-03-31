package com.waitless.queueservice.service.company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakCompanyProvisioningService {

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

    /**
     * Crée le compte Keycloak d'une company et assigne le rôle COMPANY_ADMIN.
     */
    public void createCompanyAdminAccount(CompanyDTO companyDTO) {
        String adminToken = getAdminAccessToken();
        String userId = createUserInKeycloak(adminToken, companyDTO);
        assignRealmRole(adminToken, userId, "COMPANY_ADMIN");
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
        final ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
        } catch (RestClientException e) {
            log.error("Keycloak admin token: {}", e.getMessage());
            throw new BusinessException(
                    "Keycloak injoignable depuis queue-service (URL: " + keycloakBaseUrl + "). Vérifiez que Keycloak tourne et que waitless.keycloak.* est correct.");
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException("Keycloak admin token request failed");
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.get("access_token").asText();
            if (accessToken == null || accessToken.isBlank()) {
                throw new BusinessException("Missing access_token in Keycloak response");
            }
            return accessToken;
        } catch (Exception e) {
            throw new BusinessException("Unable to parse Keycloak admin token response");
        }
    }

    private String createUserInKeycloak(String adminToken, CompanyDTO companyDTO) {
        String usersUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> passwordCreds = new HashMap<>();
        passwordCreds.put("type", "password");
        passwordCreds.put("value", companyDTO.getPassword());
        passwordCreds.put("temporary", false);

        Map<String, Object> payload = new HashMap<>();
        // Login simple: company se connecte avec son email + mot de passe.
        payload.put("username", companyDTO.getEmail());
        payload.put("email", companyDTO.getEmail());
        payload.put("firstName", companyDTO.getName());
        payload.put("enabled", true);
        payload.put("emailVerified", true);
        payload.put("credentials", List.of(passwordCreds));

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Void> response = restTemplate.exchange(usersUrl, HttpMethod.POST, entity, Void.class);
            String location = response.getHeaders().getLocation() != null
                    ? response.getHeaders().getLocation().toString()
                    : null;
            if (location == null || location.isBlank()) {
                throw new BusinessException("Keycloak user create failed (no location)");
            }
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (HttpClientErrorException.Conflict conflict) {
            throw new BusinessException("Company login email already exists in Keycloak");
        } catch (HttpClientErrorException e) {
            log.warn("Keycloak create user HTTP {} : {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Keycloak création utilisateur : " + e.getStatusCode()
                    + " — " + truncate(e.getResponseBodyAsString(), 200));
        } catch (RestClientException e) {
            log.error("Keycloak create user: {}", e.getMessage());
            throw new BusinessException("Keycloak injoignable lors de la création utilisateur : " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
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

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(List.of(rolePayload), headers);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(roleMappingsUrl, HttpMethod.POST, entity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new BusinessException("Failed to assign COMPANY_ADMIN role in Keycloak");
            }
        } catch (HttpClientErrorException e) {
            log.warn("Keycloak assign role HTTP {} : {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Keycloak assignation rôle : " + e.getStatusCode()
                    + " — " + truncate(e.getResponseBodyAsString(), 200));
        } catch (RestClientException e) {
            throw new BusinessException("Keycloak injoignable (assignation rôle) : " + e.getMessage());
        }
    }

    private String getRealmRoleId(String adminToken, String roleName) {
        String rolesUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/roles";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(rolesUrl, HttpMethod.GET, entity, String.class);
        } catch (RestClientException e) {
            throw new BusinessException("Keycloak injoignable (liste des rôles) : " + e.getMessage());
        }
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException("Keycloak roles lookup failed");
        }

        try {
            JsonNode arr = objectMapper.readTree(response.getBody());
            for (JsonNode node : arr) {
                if (roleName.equals(node.get("name").asText())) {
                    return node.get("id").asText();
                }
            }
            throw new BusinessException("Role not found in Keycloak: " + roleName);
        } catch (Exception e) {
            throw new BusinessException("Unable to parse Keycloak roles response");
        }
    }
}

