package com.ai.learning.ai.backend.controller;

import com.ai.learning.ai.backend.service.MemoryOpenAIService;
import com.ai.learning.ai.backend.service.OpenAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final OpenAIService openAIService;
    private final MemoryOpenAIService memoryOpenAIService;

    public ChatController(OpenAIService openAIService, MemoryOpenAIService memoryOpenAIService) {
        this.openAIService = openAIService;
        this.memoryOpenAIService = memoryOpenAIService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return openAIService.getChatResponse(message);
    }


    @GetMapping("/memorychat")
    public String memorychat(@RequestParam String message) {
        return memoryOpenAIService.getChatResponse(message);
    }
}