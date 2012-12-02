package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.strategies.Context;
import com.crypticbit.f2f.db.neo4j.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.neo4j.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.neo4j.strategies.VersionStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class GraphNodeImpl implements GraphNode {

    private GraphNode graphNode;

    public GraphNodeImpl(GraphNode graphNode) {
	this.graphNode = graphNode;
    }

    public GraphNode navigate(String path) {
	return JsonPath.compile(path).read(graphNode);
    }

    public void put(String json) throws IllegalJsonException, JsonPersistenceException {
	Transaction tx = getDatabaseService().beginTx();
	try {
	    JsonNode values = new ObjectMapper().readTree(json);
	    put(values, new StrategyChainFactory().createVersionStrategies(UnversionedVersionStrategy.class),
		    new Context(tx, getDatabaseService()));
	    tx.success();
	} catch (JsonPersistenceException jpe) {
	    tx.failure();
	    throw jpe;
	} catch (JsonProcessingException jpe) {
	    tx.failure();
	    throw new IllegalJsonException("The JSON string was badly formed: " + json, jpe);
	} catch (IOException e) {
	    tx.failure();
	    throw new JsonPersistenceException("IOException whilst writing data to database", e);

	} finally {
	    tx.finish();
	}
    }

    public void put(JsonNode values, VersionStrategy strategy, Context context) throws JsonPersistenceException {
	strategy.replaceNode(context, graphNode.getDatabaseNode(), values);
    }

    private GraphDatabaseService getDatabaseService() {
	return graphNode.getDatabaseNode().getGraphDatabase();
    }

    public String toJsonString() {
	return graphNode.toJsonNode().toString();
    }

    @Override
    public JsonNode toJsonNode() {
	throw new UnsupportedOperationException("toJsonNode is to be provided locally");
    }

    @Override
    public Node getDatabaseNode() {
	throw new UnsupportedOperationException("getDatabaseNode is to be provided locally");
    }

}
