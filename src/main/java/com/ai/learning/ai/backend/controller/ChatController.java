package com.ai.learning.ai.backend.controller;

import com.ai.learning.ai.backend.dto.ChatRequest;
import com.ai.learning.ai.backend.dto.ChatResponse;
import com.ai.learning.ai.backend.service.MemoryOpenAIService;
import com.ai.learning.ai.backend.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Chat", description = "Chat APIs with OpenAI integration")
public class ChatController {

    private final OpenAIService openAIService;
    private final MemoryOpenAIService memoryOpenAIService;
    private int requestCount = 0;

    public ChatController(OpenAIService openAIService, MemoryOpenAIService memoryOpenAIService) {
        this.openAIService = openAIService;
        this.memoryOpenAIService = memoryOpenAIService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a chat message", description = "Sends a message to OpenAI and returns the response")
    public synchronized ChatResponse chatToken(@RequestBody ChatRequest request) {

        requestCount++;

        if (requestCount > 100) {
            return new ChatResponse("Rate limit exceeded. Try later.");
        }

        String response = String.valueOf(openAIService.getChatResponse(request.getMessage()));
        return new ChatResponse(response);
    }

    @GetMapping("/chat")
    @Operation(summary = "Get chat response (query param)", description = "Sends a message as query parameter to OpenAI")
    public ChatResponse chat(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            return new ChatResponse("Please provide a valid message.");
        }
        return openAIService.getChatResponse(message);
    }

    @GetMapping("/chatRAG")
    @Operation(summary = "Get chat response (query param)", description = "Sends a message as query parameter to OpenAI")
    public ChatResponse chatRAG(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            return new ChatResponse("Please provide a valid message.");
        }
        return new ChatResponse(memoryOpenAIService.getChatResponseRAG(message));
    }


    @GetMapping("/memorychat")
    @Operation(summary = "Get chat response with memory", description = "Sends a message with conversation memory to OpenAI")
    public ChatResponse memorychat(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            return new ChatResponse("Please provide a valid message.");
        }
        return new ChatResponse(memoryOpenAIService.getChatResponse(message));
    }
}