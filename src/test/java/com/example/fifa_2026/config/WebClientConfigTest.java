package com.example.fifa_2026.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientConfigTest {

    @Test
    void webClient_returnsConfiguredClient() {
        WebClientConfig config = new WebClientConfig();

        WebClient webClient = config.webClient(WebClient.builder());

        assertNotNull(webClient);
    }
}
