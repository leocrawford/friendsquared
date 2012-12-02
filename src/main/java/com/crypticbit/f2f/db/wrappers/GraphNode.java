package com.crypticbit.f2f.db.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public interface GraphNode {

    public void put(JsonNode readTree);
    public GraphNode get(JsonPath compile);
    public String toJsonString();
    public JsonNode toJsonNode();

}
