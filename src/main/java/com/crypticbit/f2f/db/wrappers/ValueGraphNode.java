package com.crypticbit.f2f.db.wrappers;

import java.io.IOException;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.strategies.Context;
import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.jayway.jsonpath.JsonPath;

public class ValueGraphNode extends ValueNode implements GraphNode {

    private JsonNode delegate;
    private Node graphNode;

    public ValueGraphNode(Node graphNode) {
	this.graphNode = graphNode;
	try {
	    // FIXME factor out object mapper
	    if (graphNode.hasProperty("value"))
		this.delegate = new ObjectMapper().readTree((String) graphNode
			.getProperty("value"));
	    else
		this.delegate = new ObjectMapper().readTree("null");
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
    public void serialize(JsonGenerator jgen, SerializerProvider provider)
	    throws IOException, JsonProcessingException {
	((ValueNode) delegate).serialize(jgen, provider);

    }

    @Override
    public String toString() {
	return delegate.toString();
    }

    /**
     * Put the values at the location depicted by the path. Path must be to an
     * existing and valid node (which will be overwritten). To add to an
     * existing node use add.
     * 
     * @see add
     */
    public void put(JsonNode values, VersionStrategy strategy, Context context)
	    throws JsonPersistenceException {
	strategy.replaceNode(context, graphNode, values);

    }

    public void put(JsonNode values) {
	Transaction tx = getDatabaseService().beginTx();
	try {
	    put(values,
		    new StrategyChainFactory()
			    .createVersionStrategies(UnversionedVersionStrategy.class),
		    new Context(tx, getDatabaseService()));
	    tx.success();
	} catch (JsonPersistenceException e) {
	    tx.failure();
	    e.printStackTrace();
	} finally {
	    tx.finish();
	}
    }

    private GraphDatabaseService getDatabaseService() {
	return graphNode.getGraphDatabase();
    }

    @Override
    public GraphNode get(JsonPath path) {
	throw new Error("Not possible to navigate from leaf object");
    }
    
    public String toJsonString() {
	return delegate.toString();
    }

    @Override
    public JsonNode toJsonNode() {
	return delegate;
    }


}
