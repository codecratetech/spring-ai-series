package com.example.taskserver.mcp;

import com.example.taskserver.service.TaskService;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class TaskResources {

    private final TaskService taskService;

    public TaskResources(TaskService taskService) {
        this.taskService = taskService;
    }

    @McpResource(uri = "tasks://summary",
            name = "Tasks Summary",
            description = "Counts of open and completed tasks",
            mimeType = "text/plain")
    public String summary() {
        return taskService.summary();
    }
}
