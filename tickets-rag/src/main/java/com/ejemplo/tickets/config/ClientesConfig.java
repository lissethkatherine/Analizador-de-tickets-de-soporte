package com.ejemplo.tickets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para llamar a n8n.
 * El flujo de n8n hace embedding + busqueda en Qdrant + LLM, asi que puede tardar
 * varios segundos: por eso la espera de lectura es larga (120 s).
 */
@Configuration
public class ClientesConfig {

    @Bean
    public RestClient n8nClient() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5_000);
        fabrica.setReadTimeout(120_000);
        return RestClient.builder().requestFactory(fabrica).build();
    }
}
