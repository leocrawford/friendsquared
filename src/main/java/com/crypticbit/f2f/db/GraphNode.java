package com.crypticbit.f2f.db;

import com.fasterxml.jackson.databind.JsonNode;

public interface GraphNode  {

    public GraphNode navigate(String jsonPath);
    public void overwrite(String json) throws IllegalJsonException, JsonPersistenceException;
    public void put(String key, String json) throws IllegalJsonException, JsonPersistenceException;
    public void add(String json) throws IllegalJsonException, JsonPersistenceException;
    public JsonNode toJsonNode();
    public String toJsonString();
    
}