package com.example.taskserver.mcp;

import com.example.taskserver.entity.Task;
import com.example.taskserver.service.TaskService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TaskTools {
    private final TaskService taskService;

    public TaskTools(TaskService taskService) {
        this.taskService = taskService;
    }

    @McpTool(description = "Create a new task with title and user id")
    Task createTask(@McpToolParam(description = "The title of the task") String title,
                    @McpToolParam(description = "The user id of the task") String userId) {
        return taskService.add(title, userId);
    }

    @McpTool(description = "Get all incomplete tasks for a user")
    java.util.List<Task> getIncompleteTasks(@McpToolParam(description = "The user id of the tasks") String userId) {
        return taskService.getIncompleteTasks(userId);
    }

    @McpTool(description = "Update status of the task to completed")
    Task completeTask(@McpToolParam(description = "The id of the task to complete") Long taskId) {
        return taskService.completeTask(taskId);
    }
}
