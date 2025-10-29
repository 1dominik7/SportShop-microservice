package com.ecommerce.user.keycloak;

import com.ecommerce.user.auth.RegistrationRequest;
import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class KeyCloakService {

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.client-uid}")
    private String clientUid;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken(String username, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("username", username);
        params.add("password", password);
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                entity,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    public String getClientAccessToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                entity,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    public String createUser(String token, RegistrationRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", request.getEmail());
        userPayload.put("email", request.getEmail());
        userPayload.put("enabled", true);
        userPayload.put("firstName", request.getFirstName());
        userPayload.put("lastName", request.getLastName());

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getPassword());
        credential.put("temporary", false);

        userPayload.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new APIException("User have not been created");
        }

        return getUserIdByUsername(token, request.getEmail());
    }

    private String getUserIdByUsername(String token, String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?username=" + username;

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new NotFoundException("User", Optional.ofNullable(username));
        }
        Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
        return (String) user.get("id");
    }

    public String getClientUUID(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId;

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new NotFoundException("Client", Optional.ofNullable(clientId));
        }
        Map<String, Object> client = (Map<String, Object>) response.getBody().get(0);
        return (String) client.get("id");
    }

    public Map<String, Object> getClientRole(String accessToken, String clientUUID, String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles/" + roleName;

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        if (response.getBody() == null) {
            throw new APIException("Role not found " + roleName + " for client " + clientId);
        }

        return response.getBody();
    }

    public void assignRealmRoleToUser(String accessToken, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String roleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUid + "/roles/USER";

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                roleUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> role = roleResponse.getBody();

        if (role == null || role.get("id") == null || role.get("name") == null) {
            throw new APIException("Role " + roleResponse.getBody() + " not found in client" + clientUid);
        }

        Map<String, Object> roleRepresentation = new HashMap<>();
        roleRepresentation.put("id", role.get("id"));
        roleRepresentation.put("name", role.get("name"));

        List<Map<String, Object>> roles = List.of(roleRepresentation);

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(roles, headers);

        String assignRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/"
                + userId + "/role-mappings/clients/" + clientUid;

        ResponseEntity<Void> response = restTemplate.postForEntity(assignRoleUrl, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new APIException("Failed to assign role " + "USER" +
                    " to user. HTTP " + response.getStatusCode());
        }
    }

    public Map<String, Object> loginUser(String email, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", email);
        params.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {

                throw new APIException("Invalid username or password");
            }
            throw new APIException("Login failed: " + e.getMessage());
        }
    }

    public void addRoleToKeycloakUser(String keycloakUserId, String roleName, String adminToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String clientUUID = getClientUUID(adminToken);

        String rolesUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles";
        ResponseEntity<List> rolesResponse = restTemplate.exchange(
                rolesUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map<String, Object>> roles = rolesResponse.getBody();

        Map<String, Object> roleToAdd = roles.stream()
                .filter(r -> roleName.equals(r.get("name")))
                .findFirst()
                .orElseThrow(() -> new APIException("Role not found in Keycloak"));

        List<Map<String, Object>> rolesToAdd = List.of(roleToAdd);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesToAdd, headers);

        restTemplate.postForEntity(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/clients/" + clientUUID,
                request,
                Void.class
        );
    }

    public void removeRoleToKeycloakUser(String keycloakUserId, String roleName, String adminToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String clientUUID = getClientUUID(adminToken);

        ResponseEntity<List> rolesResponse = restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map<String, Object>> roles = rolesResponse.getBody();
        Map<String, Object> roleToRemove = roles.stream()
                .filter(r -> roleName.equals(r.get("name")))
                .findFirst()
                .orElseThrow(() -> new APIException("Role not found in Keycloak"));

        List<Map<String, Object>> rolesToRemove = List.of(roleToRemove);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesToRemove, headers);

        restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/clients/" + clientUUID,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

    public void createRoleInKeycloak(String roleName, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String clientUUID = getClientUUID(adminToken);

        try {
            String checkRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles/" + roleName;
            ResponseEntity<Map> roleCheckResponse = restTemplate.exchange(
                    checkRoleUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (roleCheckResponse.getStatusCode() == HttpStatus.OK) {
                throw new IllegalArgumentException("Role " + roleName + " already exists in Keycloak!");
            }
        } catch (HttpClientErrorException.NotFound ex) {
        }

        Map<String, Object> roleData = new HashMap<>();
        roleData.put("name", roleName);
        roleData.put("description", "This is the role for " + roleName);

        String createRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles";
        ResponseEntity<Void> response = restTemplate.exchange(
                createRoleUrl,
                HttpMethod.POST,
                new HttpEntity<>(roleData, headers),
                Void.class
        );

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new APIException("Failed to create role " + roleName + " in Keycloak");
        }
    }

    public void deleteRoleFromKeycloak(String roleName, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String clientUUID = getClientUUID(adminToken);
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + clientUUID + "/roles/" + roleName;

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            if (res.getStatusCode() == HttpStatus.OK && res.getBody() != null) {
                ResponseEntity<Void> deleteResponse = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

                if (deleteResponse.getStatusCode() != HttpStatus.NO_CONTENT) {
                    throw new APIException("Failed to delete role " + roleName + " from Keycloak");
                }

            } else {
                throw new APIException("Role " + roleName + " not found in Keycloak.");
            }
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("Role " + roleName + " not found in Keycloak.");
        } catch (Exception e) {
            throw new RuntimeException("Error deleting role " + roleName + " from Keycloak", e);
        }
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        log.info("keyCloak refreshToken {}: ", refreshToken);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return restTemplate.postForObject(url, entity, Map.class);
    }

    public void logoutUser(String refreshToken) {
        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("refresh_token", refreshToken);

            String logoutUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

            restTemplate.postForEntity(logoutUrl, requestBody, String.class);
        } catch (Exception e) {
            log.error("Keycloak logout failed", e);
        }
    }
}