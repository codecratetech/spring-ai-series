package com.example.agent.tools;

import com.example.agent.entity.Task;
import com.example.agent.repository.TaskRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TaskTools {
    private final TaskRepository taskRepository;

    public TaskTools(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Tool(description = "Create a new task with title and user id")
    Task createTask(@ToolParam(description = "The title of the task") String title,
                    @ToolParam(description = "The user id of the task") String userId) {
        return taskRepository.save(new com.example.agent.entity.Task(title, userId));
    }

    @Tool(description = "Get all incomplete tasks for a user")
    java.util.List<Task> getIncompleteTasks(@ToolParam(description = "The user id of the tasks") String userId) {
        return taskRepository.findByCompletedFalseAndUserId(userId);
    }

}
