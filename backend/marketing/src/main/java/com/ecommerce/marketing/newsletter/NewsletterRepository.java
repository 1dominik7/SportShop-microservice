package com.ecommerce.marketing.newsletter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Integer> {

    boolean existsByEmail(String email);
}
