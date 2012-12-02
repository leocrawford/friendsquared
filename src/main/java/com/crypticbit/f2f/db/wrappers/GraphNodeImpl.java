package com.crypticbit.f2f.db.wrappers;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.strategies.Context;
import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public class GraphNodeImpl implements GraphNode{

    private GraphNode graphNode;

    public GraphNodeImpl(GraphNode graphNode) {
	this.graphNode = graphNode;
    }

    public GraphNode navigate(String path) {
	return JsonPath.compile(path).read(graphNode);
    }

    public void put(JsonNode values) {
	Transaction tx = getDatabaseService().beginTx();
	try {
	    put(values, new StrategyChainFactory().createVersionStrategies(UnversionedVersionStrategy.class),
		    new Context(tx, getDatabaseService()));
	    tx.success();
	} catch (JsonPersistenceException e) {
	    tx.failure();
	    e.printStackTrace();
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
