package com.crypticbit.f2f.db.wrappers;

import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public interface GraphNode {

    public GraphNode get(JsonPath compile);

    public void put(JsonNode readTree);

    public JsonNode toJsonNode();

    public String toJsonString();

    Node getDatabaseNode();

}
