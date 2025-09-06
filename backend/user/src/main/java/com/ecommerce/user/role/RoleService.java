package com.ecommerce.user.role;

import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final KeyCloakService keyCloakService;

    @Transactional
    public Role createRole(String name) {

        String adminToken = keyCloakService.getClientAccessToken();

        if(roleRepository.existsByName(name)){
            throw new APIException("Role already exists in database");
        }

        keyCloakService.createRoleInKeycloak(name, adminToken);

        Role role = Role.builder()
                .name(name)
                .build();

        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getByName(String name){
        return roleRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    @Transactional
    public void deleteRole(String roleName){
        String adminToken = keyCloakService.getClientAccessToken();
        keyCloakService.deleteRoleFromKeycloak(roleName, adminToken);
        roleRepository.deleteByName(roleName);
    }
}
