package com.mx.development.jose.said.scaffold.application.repository;

import com.mx.development.jose.said.scaffold.application.model.ApplicationAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationAttributeRepository extends JpaRepository<ApplicationAttribute, Long> {
    List<ApplicationAttribute> findByApplicationIdOrderBySortOrderAsc(Long applicationId);
    void deleteByApplicationId(Long applicationId);
}
