package com.github.webservicetesting.sparkjavatodo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;

import static spark.Spark.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TodosServer {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static AtomicLong COUNTER = new AtomicLong();
    private static Map<Long, TodoItem> todos = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        get("/todos/:id", (req, res) -> {
            Long key = Long.valueOf(req.params(":id"));
            if (!todos.containsKey(key)) {
                halt(404);
            }
            return todos.get(key);
        }, mapper::writeValueAsString);

        post("/todos", (req, res) -> {
            Long key = COUNTER.incrementAndGet();
            TodoItem todoItem = createTodoItem(req, key);
            res.status(201);
            todos.put(key,todoItem);
            return todos.get(key);
        }, mapper::writeValueAsString);


        put("/todos/:id", (req, res) -> {
            Long key = Long.valueOf(req.params(":id"));
            TodoItem todoItem = createTodoItem(req, key);
            todos.put(key,todoItem);
            return todos.get(key);
         }, mapper::writeValueAsString);

        delete("/todos/:id", (req, res) -> {
            Long key = Long.valueOf(req.params(":id"));
            todos.remove(key);
            res.status(204);
            return "";
         });
    }

    private static TodoItem createTodoItem(Request req, Long key) throws IOException {
        JsonNode tree = mapper.readTree(req.body());
        return new TodoItem(key, tree.get("name").asText(), tree.get("task").asText());
    }

}
