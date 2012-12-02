package com.crypticbit.f2f.db.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public interface MyGraphNode {

    public void put(JsonNode readTree);
    public MyGraphNode get(JsonPath compile);

}
