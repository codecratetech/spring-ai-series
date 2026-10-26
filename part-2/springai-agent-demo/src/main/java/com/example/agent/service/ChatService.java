package com.example.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String userId, String message){
        String response = chatClient.prompt()
                .user("user id :" + userId + ", message: " + message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .call().content();
        return response;
    }
}
