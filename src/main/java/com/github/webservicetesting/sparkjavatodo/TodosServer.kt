package com.github.webservicetesting.sparkjavatodo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.eclipse.jetty.http.MimeTypes
import org.slf4j.LoggerFactory
import spark.Request

import spark.Spark.*
import spark.kotlin.Http
import spark.kotlin.ignite

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object TodosServer {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val logger = LoggerFactory.getLogger(TodosServer::class.java)

    private val PATH = "/todos"
    private val PATH_WITH_ID = PATH + "/:id"
    private val ID_PARAM = ":id"
    private val NAME = "name"
    private val TASK = "task"

    private val COUNTER = AtomicLong()
    private val todos = ConcurrentHashMap<Long, TodoItem>()

    data class TodoItem(val id: Long?, val name: String, val task: String)

    @JvmStatic fun main(args: Array<String>) {
        val http: Http = ignite().port(9090).apply {

            get(PATH_WITH_ID, "application/json", { req, _ ->
                val key = getIdParam(req)
                if (!todos.containsKey(key)) {
                    spark.kotlin.halt(404)
                    logger.info("Key {} Not found ", key)
                }
                logger.info("Key {}  found and returned ", key)
                todos[key]
            }, mapper::writeValueAsString)

            post(PATH, { req, res ->
                val key = COUNTER.incrementAndGet()
                val todoItem = createTodoItem(req, key)
                res.status(201)
                logger.info("Key {}  Created and returned ", key)
                todos.put(key, todoItem)
                todos[key]
            }, mapper::writeValueAsString)


            put(PATH_WITH_ID, { req, res ->
                val key = getIdParam(req)
                val todoItem = createTodoItem(req, getIdParam(req))
                todos.put(key, todoItem)
                logger.info("Key {}  Updated and returned ", key)
                todos[key]
            }, mapper::writeValueAsString)
        }

        http.delete(path = PATH_WITH_ID, function = {
            todos.remove(getIdParam(request))
            response.status(204)
            logger.info(" Item deleted")
        })
        http.after {
            response.type(MimeTypes.Type.APPLICATION_JSON.asString())
            response.header("Content-Encoding", "gzip")
        }

    }

    private fun getIdParam(req: Request): Long {
        return java.lang.Long.valueOf(req.params(ID_PARAM))
    }

    @Throws(IOException::class)
    private fun createTodoItem(req: Request, key: Long?): TodoItem {
        var tree: JsonNode? = null
        try {
            tree = mapper.readTree(req.body())
        } catch (e: IOException) {
            logger.error("Caught exception ", e)
            throw e
        }

        return TodoItem(key, getNode(tree, NAME), getNode(tree, TASK))
    }

    private fun getNode(tree: JsonNode, name: String): String {
        return tree.get(name).asText()
    }

}
