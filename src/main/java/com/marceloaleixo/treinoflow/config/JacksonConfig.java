package com.marceloaleixo.treinoflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração explícita do Jackson.
 *
 * O projeto utiliza spring-boot-starter-webmvc (e não o starter web tradicional),
 * portanto não devemos depender da criação automática de um ObjectMapper.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
