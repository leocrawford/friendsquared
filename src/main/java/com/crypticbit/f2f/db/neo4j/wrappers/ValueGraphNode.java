package com.crypticbit.f2f.db.neo4j.wrappers;

import java.io.IOException;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.jayway.jsonpath.JsonPath;

public class ValueGraphNode extends ValueNode implements GraphNode {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private JsonNode delegate;
    private Node node;
    private GraphNodeImpl virtualSuperclass;

    public ValueGraphNode(Node graphNode) {
	this.node = graphNode;
	virtualSuperclass = new GraphNodeImpl(this);
	try {
	    if (graphNode.hasProperty("value")) {
		this.delegate = OBJECT_MAPPER.readTree((String) graphNode.getProperty("value"));
	    } else {
		this.delegate = OBJECT_MAPPER.readTree("null");
	    }
	} catch (JsonProcessingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public String asText() {
	return delegate.asText();
    }

    @Override
    public JsonToken asToken() {
	return delegate.asToken();
    }

    @Override
    public boolean equals(Object o) {
	return delegate.equals(o);
    }

    @Override
    public GraphNode navigate(String path) {
	throw new Error("Not possible to navigate from leaf object");
    }

    @Override
    public JsonNode toJsonNode() {
	return delegate;
    }

    @Override
    public String toJsonString() {
	return delegate.toString();
    }

    @Override
    public String toString() {
	return delegate.toString();
    }

    @Override
    public Node getDatabaseNode() {
	return node;
    }
    
    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
	((BaseJsonNode) delegate).serialize(jgen, provider);
	
    }

    // delegate methods

    @Override
    public void put(String values) throws IllegalJsonException, JsonPersistenceException {
	virtualSuperclass.put(values);
    }



}
