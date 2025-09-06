package com.ecommerce.user.keycloak;

import com.ecommerce.user.auth.RegistrationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeyCloakAdminService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-uid}")
    private String clientUid;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAdminAccessToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("username", adminUsername);
        params.add("password", adminPassword);
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

    public String createUser(String token, RegistrationRequest request){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userPayload = new HashMap<>();
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

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                entity,
                String.class
        );

        if(!HttpStatus.CREATED.equals(response.getStatusCode())) {
            throw new RuntimeException("Failed to create user in keycloak" + response.getBody());
        }

        URI location = response.getHeaders().getLocation();
        if(location == null){
            throw new RuntimeException("Keycloak did not return Location Header" + response.getBody());
        }

        String path = location.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private Map<String, Object> getRealmRoleRepresentation(String token, String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = keycloakServerUrl + "/admin/realms/" +
                realm + "/clients/" + clientUid + "/roles/" + roleName;
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
                HttpMethod.GET,
                entity,
                Map.class
        );
        return response.getBody();
    }

    public void assignRealmRoleToUser(String username, String roleName, String userId) {
        String token = getAdminAccessToken();
        Map<String, Object> roleRep = getRealmRoleRepresentation(
                token,
                roleName
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(List.of(roleRep), headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/"
                + userId + "/role-mappings/clients/" + clientUid;

        ResponseEntity<Void> response = restTemplate.postForEntity(
                url, entity, Void.class
        );
        if(!response.getStatusCode().is2xxSuccessful()){
            throw new RuntimeException(
                    "Failed to assign role " + roleName +
                            " to user " + username +
                            ": HTTP " + response.getStatusCode()
            );
        }
    }

    public void addRoleToKeycloakUser(String keycloakUserId, String roleName, String adminToken){

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List> rolesResponse = restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/roles",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map<String, Object>> roles = rolesResponse.getBody();
        Map<String, Object> roleToAdd = roles.stream()
                .filter(r -> roleName.equals(r.get("name")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found in Keycloak"));

        List<Map<String, Object>> rolesToAdd = List.of(roleToAdd);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesToAdd, headers);

        restTemplate.postForEntity(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mapping/realm",
                request,
                Void.class
        );
    }

    public void removeRoleToKeycloakUser(String keycloakUserId, String roleName, String adminToken){

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List> rolesResponse = restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/roles",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map<String, Object>> roles = rolesResponse.getBody();
        Map<String, Object> roleToRemove = roles.stream()
                .filter(r -> roleName.equals(r.get("name")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found in Keycloak"));

        List<Map<String, Object>> rolesToRemove = List.of(roleToRemove);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesToRemove, headers);

        restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mapping/realm",
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }
}
