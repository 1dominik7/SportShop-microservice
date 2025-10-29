package com.ecommerce.user.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    List<User> findByIdIn(List<String> userIds);

    Page<User> findByEmailContainingIgnoreCase(String query, Pageable pageable);

    @Query("{ '$expr': { '$regexMatch': { 'input': { '$toString': '$_id' }, 'regex': ?0, 'options': 'i' } } }")
    Page<User> findByObjectIdContaining(String partialId, Pageable pageable);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

}

