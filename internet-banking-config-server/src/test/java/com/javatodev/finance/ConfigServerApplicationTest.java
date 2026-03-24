package com.javatodev.finance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigServerApplicationTest {

    @Test
    void applicationClass_exists() {
        InternetBankingConfigServerApplication application = new InternetBankingConfigServerApplication();
        assertNotNull(application);
    }

    @Test
    void mainMethod_exists() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = InternetBankingConfigServerApplication.class;
            clazz.getMethod("main", String[].class);
        });
    }

    @Test
    void application_hasSpringBootAnnotation() {
        Class<?> clazz = InternetBankingConfigServerApplication.class;
        assertTrue(clazz.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void application_hasEnableConfigServerAnnotation() {
        Class<?> clazz = InternetBankingConfigServerApplication.class;
        assertTrue(clazz.isAnnotationPresent(org.springframework.cloud.config.server.EnableConfigServer.class));
    }
}
