package com.ejemplo.tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la aplicacion.
 * @ConfigurationPropertiesScan hace que Spring encuentre AppProperties
 * y lo llene con los valores "app.*" de application.properties.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsApplication.class, args);
    }
}
