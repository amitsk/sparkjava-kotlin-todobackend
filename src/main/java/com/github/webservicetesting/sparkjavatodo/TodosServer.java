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

    private static final String PATH = "/todos";
    private static final String PATH_WITH_ID = PATH + "/:id";
    private static final String ID_PARAM = ":id";
    private static final String NAME = "name";
    private static final String TASK = "task";

    private static AtomicLong COUNTER = new AtomicLong();
    private static Map<Long, TodoItem> todos = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        get(PATH_WITH_ID, (req, res) -> {
            Long key = getIdParam(req);
            if (!todos.containsKey(key)) {
                halt(404);
            }
            return todos.get(key);
        }, mapper::writeValueAsString);

        post(PATH, (req, res) -> {
            Long key = COUNTER.incrementAndGet();
            TodoItem todoItem = createTodoItem(req, key);
            res.status(201);
            todos.put(key, todoItem);
            return todos.get(key);
        }, mapper::writeValueAsString);


        put(PATH_WITH_ID, (req, res) -> {
            Long key = getIdParam(req);
            TodoItem todoItem = createTodoItem(req, getIdParam(req));
            todos.put(key, todoItem);
            return todos.get(key);
        }, mapper::writeValueAsString);

        delete(PATH_WITH_ID, (req, res) -> {
            todos.remove(getIdParam(req));
            res.status(204);
            return "";
        });
    }

    private static Long getIdParam(Request req) {
        return Long.valueOf(req.params(ID_PARAM));
    }

    private static TodoItem createTodoItem(Request req, Long key) throws IOException {
        JsonNode tree = mapper.readTree(req.body());
        return new TodoItem(key, getNode(tree, NAME), getNode(tree, TASK));
    }

    private static String getNode(JsonNode tree, String name) {
        return tree.get(name).asText();
    }

}
