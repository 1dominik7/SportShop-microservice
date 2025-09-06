package com.ecommerce.marketing.mainImages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Integer> {

    List<Images> findByDisplayOrderIsNotNull();

}
