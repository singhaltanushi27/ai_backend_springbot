package com.ai.learning.ai.backend.controller;

import com.ai.learning.ai.backend.service.OpenAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final OpenAIService openAIService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return openAIService.getChatResponse(message);
    }
}