package com.javatodev.finance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistryApplicationTest {

    @Test
    void applicationClass_exists() {
        InternetBankingServiceRegistryApplication application = new InternetBankingServiceRegistryApplication();
        assertNotNull(application);
    }

    @Test
    void mainMethod_exists() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = InternetBankingServiceRegistryApplication.class;
            clazz.getMethod("main", String[].class);
        });
    }

    @Test
    void application_hasSpringBootAnnotation() {
        Class<?> clazz = InternetBankingServiceRegistryApplication.class;
        assertTrue(clazz.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void application_hasEnableEurekaServerAnnotation() {
        Class<?> clazz = InternetBankingServiceRegistryApplication.class;
        assertTrue(clazz.isAnnotationPresent(org.springframework.cloud.netflix.eureka.server.EnableEurekaServer.class));
    }
}
