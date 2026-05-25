package com.ai.learning.ai.backend.service;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final WebClient webClient;

    public OpenAIService(@Value("${openai.api.key}") String apiKey) throws SSLException {

        // ✅ SSL bypass (for corporate/local dev)
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getChatResponse(String userInput) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful backend assistant"),
                            Map.of("role", "user", "content", userInput)
                    ),
                    "temperature", 0.3
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // ✅ Handle OpenAI API error
            if (response.containsKey("error")) {
                Map error = (Map) response.get("error");
                return "OpenAI Error: " + error.get("message");
            }

            // ✅ Extract response safely
            List choices = (List) response.get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            return "Error occurred: " + e.getMessage();
        }
    }
}