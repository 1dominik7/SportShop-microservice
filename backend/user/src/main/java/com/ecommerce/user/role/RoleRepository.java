package com.ecommerce.user.role;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(String role);
    boolean existsByName(String name);
    void deleteByName(String name);
}
