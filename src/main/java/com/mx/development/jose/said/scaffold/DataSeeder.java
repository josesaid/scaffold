package com.mx.development.jose.said.scaffold;

import com.mx.development.jose.said.scaffold.application.model.Application;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final ApplicationRepository applicationRepository;

    @Bean
    public ApplicationRunner seedRootApplication() {
        return args -> {
            if (applicationRepository.count() == 0) {
                Application root = Application.builder()
                        .name("Scaffold")
                        .version("1.0")
                        .author("Jose Said Olano Garcia")
                        .description("Scaffold is an app that let generate another already customized Spring Boot Apps")
                        .comments("This is the Root app generator of the Spring Boot apps coming in the future")
                        .notes("TBD")
                        .parent(null)
                        .build();

                applicationRepository.save(root);
                log.info("✓ Root Application 'Scaffold' created automatically (id:1)");
            } else {
                log.info("Root Application already exists — skipping seed");
            }
        };
    }
}
