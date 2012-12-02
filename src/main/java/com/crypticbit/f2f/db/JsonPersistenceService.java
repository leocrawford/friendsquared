package com.crypticbit.f2f.db;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.neo4j.nodes.GraphNode;
import com.fasterxml.jackson.databind.JsonNode;

public interface JsonPersistenceService {

    public GraphNode navigate(String jsonPath);

    public void put(String json) throws IllegalJsonException, JsonPersistenceException;

    public JsonNode toJsonNode();

    public String toJsonString();

    Node getDatabaseNode();
    
}
