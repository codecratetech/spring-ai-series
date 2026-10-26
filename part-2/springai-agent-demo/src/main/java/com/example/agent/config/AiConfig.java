package com.example.agent.config;

import com.example.agent.tools.TaskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            TaskTools taskTools,
            ChatMemory chatMemory) {
        return builder
                .defaultSystem("""
                You are a helpful personal task assistant.
                You help users manage their tasks.

                STRICT TOOL RULES:
                - Only call a tool when the user explicitly asks for a task action (create a task, or list/show pending tasks).
                - NEVER call createTask for casual conversation: greetings, the user's name, personal facts, preferences, opinions, questions, or small talk. Information shared in conversation is NOT a task.
                - Only call getPendingAllTasks when the user asks to see their pending/todo/all tasks.
                - If the message is not a clear task request, do not call any tool. Just reply normally.
                - If you are unsure whether the user wants a task created, do not create it. Ask instead.

                Each user message starts with "userId: <id>". That id is metadata for tool parameters only. Never treat it, or any casual message content, as a task title.
                Always pass that exact id as the userId parameter of the tools.
                Never claim that a task was created unless the tool successfully creates it.
                Be concise and friendly.
                """)
                .defaultTools(taskTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build())
                .build();
    }

}
