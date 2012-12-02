package com.crypticbit.f2f.db.neo4j.wrappers;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public interface GraphNode {

    public GraphNode navigate(String jsonPath);

    public void put(String json) throws IllegalJsonException, JsonPersistenceException;

    public JsonNode toJsonNode();

    public String toJsonString();

    Node getDatabaseNode();

}
