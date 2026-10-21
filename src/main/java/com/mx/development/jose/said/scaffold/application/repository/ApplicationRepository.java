package com.mx.development.jose.said.scaffold.application.repository;

import com.mx.development.jose.said.scaffold.application.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByName(String name);
    Optional<Application> findByNameIgnoreCase(String name);

    List<Application> findByAuthor(String author);

    boolean existsByName(String name);
}
