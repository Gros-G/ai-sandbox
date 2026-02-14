package ai.sandbox.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);

        // Example logging at different levels
        log.debug("Application started in DEBUG mode");
        log.info("Sandbox Application successfully initialized");
    }
}
