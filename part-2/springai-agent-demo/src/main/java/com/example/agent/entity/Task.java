package com.example.agent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Setter
    private String title;

    @Getter @Setter
    private boolean completed;

    @Getter @Setter
    private String userId;

    public Task() {
    }

    public Task(String title, String userId) {
        this.title = title;
        this.completed = false;
        this.userId = userId;
    }
}
