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

/**
 * Interface that is implemented by Map and Array nodes that extend their
 * Jackson parents, and which provides methods to lazily load children, and
 * return the original datbase node
 */
public class JsonNodeGraphAdapterImpl implements
	OutwardFacingJsonNodeGraphAdapter {

    private Node node;

    public JsonNodeGraphAdapterImpl(Node node) {
	this.node = node;
    }

    /** Return the underlying database node */
    public Node getDatabaseNode() {
	return node;
    }

    protected GraphDatabaseService getDatabaseService() {
	return node.getGraphDatabase();
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
	strategy.replaceNode(context, getDatabaseNode(), values);

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

}