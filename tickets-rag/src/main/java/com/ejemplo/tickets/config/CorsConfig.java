package com.ejemplo.tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend llame a la API aunque se abra desde otro origen
 * (por ejemplo, Live Server de VS Code en el puerto 5500).
 * Si el frontend se abre desde http://localhost:8080 (lo normal) no hace falta.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
