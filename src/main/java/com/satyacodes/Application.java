package com.satyacodes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        log.info("Application Ready!! \n\tListening on address: {} and port: {} \n\tProfiles: {}",
                env.getProperty("server.address"),
                env.getProperty("server.port"),
                env.getActiveProfiles()
        );
        log.info("********************************");
        log.info("http://localhost:8999/spring-ai-app");
        log.info("http://localhost:8999/spring-ai-app/swagger-ui/index.html");
        log.info("********************************\n \n ");
    }

}