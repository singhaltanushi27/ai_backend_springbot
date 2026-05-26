package com.ai.learning.ai.backend.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MemoryOpenAIService {

    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(MemoryOpenAIService.class);

    private final List<Map<String, String>> conversationHistory = new ArrayList<>();

    public MemoryOpenAIService(@Value("${openai.api.key}") String apiKey) throws SSLException {

        conversationHistory.add(Map.of(
                "role", "system",
                "content", "You are a senior Java backend engineer. Answer clearly with examples."
        ));
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

    public String getChatResponseRAG(String userInput) {

        try {
            if (conversationHistory.size() > 10) {
                conversationHistory.remove(1); // keep system prompt
            }
            String context = "Company Leave Policy: Employees get 20 paid leaves annually.";

            String enhancedPrompt =
                    "Answer ONLY using the following data. " +
                            "If answer is not present, say 'Data not available'.\n\n" +
                            "Data:\n" + context +
                            "\n\nQuestion: " + userInput;

            // ✅ Add user message to history
            conversationHistory.add(Map.of(
                    "role", "user",
                    "content", enhancedPrompt
            ));
            logger.info("User Input: {}", enhancedPrompt);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", conversationHistory,
                    "temperature", 0.3,
                    "max_tokens", 100
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response.containsKey("error")) {
                return "OpenAI Error: " + ((Map) response.get("error")).get("message");
            }

            List choices = (List) response.get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            String aiReply = message.get("content").toString();

            // ✅ Add AI response to history
            conversationHistory.add(Map.of(
                    "role", "assistant",
                    "content", aiReply
            ));

            return aiReply;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getChatResponse(String userInput) {

        try {
            if (conversationHistory.size() > 10) {
                conversationHistory.remove(1); // keep system prompt
            }
            // ✅ Add user message to history
            conversationHistory.add(Map.of(
                    "role", "user",
                    "content", userInput
            ));
            logger.info("User Input: {}", userInput);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", conversationHistory,
                    "temperature", 0.3,
                    "max_tokens", 100
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response.containsKey("error")) {
                return "OpenAI Error: " + ((Map) response.get("error")).get("message");
            }

            List choices = (List) response.get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            String aiReply = message.get("content").toString();

            // ✅ Add AI response to history
            conversationHistory.add(Map.of(
                    "role", "assistant",
                    "content", aiReply
            ));

            return aiReply;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}