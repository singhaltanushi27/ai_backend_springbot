package com.ai.learning.ai.backend.service;

import com.ai.learning.ai.backend.dto.ChatRequest;
import com.ai.learning.ai.backend.dto.ChatResponse;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

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

    public ChatResponse getChatResponse(String userInput) {
        logger.info("User Input: {}", userInput);
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a senior Java backend engineer. Always give clear, concise, structured responses with examples."),
                            Map.of("role", "user", "content", userInput)
                    ),
                    "temperature", 0.3
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // ✅ Handle OpenAI API error
            if (response != null && response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMessage = "OpenAI Error: " + error.get("message");
                logger.error(errorMessage);
                return new ChatResponse(errorMessage);
            }

            // ✅ Extract response safely
            if (response == null || !response.containsKey("choices")) {
                logger.error("Invalid response structure from OpenAI API");
                return new ChatResponse("Error: Invalid response from OpenAI API");
            }

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices found in OpenAI response");
                return new ChatResponse("Error: No response choices available");
            }

            Map<String, Object> choice = (Map<String, Object>) choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            Object content = message.get("content");

            String responseText = content != null ? content.toString() : "No content received";
            logger.info("OpenAI Response: {}", responseText);
            return new ChatResponse(responseText);

        } catch (Exception e) {
            logger.error("Error occurred while calling OpenAI API", e);
            return new ChatResponse("Error occurred: " + e.getMessage());
        }
    }
}