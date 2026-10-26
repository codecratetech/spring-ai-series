package com.example.agent.controller;

import com.example.agent.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        return new ChatResponse(chatService.chat(chatRequest.userId(), chatRequest.message()));
    }
    public record ChatRequest(String userId, String message) {}
    public record ChatResponse(String response){}
}
