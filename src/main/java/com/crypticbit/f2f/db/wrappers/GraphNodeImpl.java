package com.crypticbit.f2f.db.wrappers;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.strategies.Context;
import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public class GraphNodeImpl {

    private GraphNode graphNode;

    public GraphNodeImpl(GraphNode graphNode) {
	this.graphNode = graphNode;
    }

    public GraphNode get(JsonPath path) {
	return path.read(graphNode);
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

}
