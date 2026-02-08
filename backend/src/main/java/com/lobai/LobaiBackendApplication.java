package com.lobai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LobAI Backend Application
 *
 * Main application class for LobAI Spring Boot backend.
 * This application provides:
 * - User authentication (JWT)
 * - AI chat message persistence
 * - Stats management (hunger/energy/happiness)
 * - Multi-persona support (5 personas)
 * - HIP (Human Identity Protocol) analysis with scheduled reanalysis
 */
@SpringBootApplication
@EnableScheduling
public class LobaiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LobaiBackendApplication.class, args);
    }

}
