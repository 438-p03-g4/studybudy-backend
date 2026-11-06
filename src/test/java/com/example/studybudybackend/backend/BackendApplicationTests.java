package com.example.studybudybackend.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Use an isolated in-memory database; tests never connect to or modify Neon.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:studybudy-context;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
