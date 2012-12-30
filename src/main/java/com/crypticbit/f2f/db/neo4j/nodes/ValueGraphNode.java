package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.Neo4JSimpleFdoAdapter;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class ValueGraphNode extends ValueNode implements Neo4JGraphNode {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private JsonNode delegate;
    private Node node;
    private GraphNodeImpl virtualSuperclass;

    public ValueGraphNode(Node graphNode, Relationship incomingRelationship) {
	this.node = graphNode;
	virtualSuperclass = new GraphNodeImpl(this, incomingRelationship);
	try {
	    if (graphNode.hasProperty(RelationshipParameters.VALUE.name())) {
		this.delegate = OBJECT_MAPPER.readTree((String) graphNode
			.getProperty(RelationshipParameters.VALUE.name()));
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
    public Neo4JGraphNode navigate(String path) {
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
    public void overwrite(String values) throws IllegalJsonException, JsonPersistenceException {
	virtualSuperclass.overwrite(values);
    }

    @Override
    public List<History> getHistory() {
	return virtualSuperclass.getHistory();
    }

    @Override
    public long getTimestamp() {
	return virtualSuperclass.getTimestamp();
    }

    @Override
    public void put(String key, String json) throws JsonPersistenceException {
	throw new JsonPersistenceException("It's not possible to add data to a child node. ");
    }

    @Override
    public void add(String json) throws JsonPersistenceException {
	throw new JsonPersistenceException("It's not possible to add data to a child node. ");
    }

    @Override
    public Neo4JSimpleFdoAdapter getStrategy() {
	return virtualSuperclass.getStrategy();
    }

}
