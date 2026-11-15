package com.example.taskserver.service;

import com.example.taskserver.entity.Task;
import com.example.taskserver.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    public Task add(String title, String userId){
        return taskRepository.save(new Task(title, userId));
    }
    public List<Task> getIncompleteTasks(String userId) {
        return taskRepository.findByCompletedFalseAndUserId(userId);
    }
    public Task completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public String summary() {
        //return summary of all task , which are open and pending for all user ids
        List<Task> tasks = taskRepository.findAll();
        StringBuilder summary = new StringBuilder();
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                summary.append("Task ID: ").append(task.getId())
                        .append(", Title: ").append(task.getTitle())
                        .append(", User ID: ").append(task.getUserId())
                        .append(", Completed: ").append(task.isCompleted())
                        .append("\n");
            }
        }
        return summary.toString();
    }
}
