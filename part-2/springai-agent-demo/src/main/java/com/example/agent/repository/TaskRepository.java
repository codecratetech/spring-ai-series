package com.example.agent.repository;

import com.example.agent.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<com.example.agent.entity.Task, Long> {
    List<Task> findByCompletedFalseAndUserId(String userId);
}
