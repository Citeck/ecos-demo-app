package ru.citeck.ecos.webapp.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ru.citeck.ecos.webapp.lib.spring.EcosSpringApplication;

/**
 * Main class for ecos-demo-app.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class EcosDemoApp {

    // This name required for ECOS to identify application
    // You can use spring property spring.application.name
    // but variable in code may be more useful in the future.
    @SuppressWarnings("unused")
    public static final String NAME = "ecos-demo-app";

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new EcosSpringApplication(EcosDemoApp.class).run(args);
    }
}
