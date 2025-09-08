package com.AIService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Configration {

    @Bean
    public WebClient authorizedWebClient() {
        return WebClient.builder().build();
    }
}
