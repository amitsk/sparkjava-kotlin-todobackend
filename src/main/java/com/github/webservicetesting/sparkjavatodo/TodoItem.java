package com.github.webservicetesting.sparkjavatodo;

/**
 * Created by archana on 9/4/16.
 */
public class TodoItem {
    private final Long id;
    private final String name;
    private final String task;

    public TodoItem(Long id, String name, String task) {
        this.id = id;
        this.name = name;
        this.task = task;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTask() {
        return task;
    }
}
