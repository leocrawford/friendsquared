package com.crypticbit.f2f.db.wrappers;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.NodeTypes;
import com.crypticbit.f2f.db.RelTypes;
import com.crypticbit.f2f.db.strategies.Context;
import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

/**
 * This class extends ObjectNode, but requires a call to updateNodes before
 * calls to any method that uses _children, so in practice we will typically
 * wrap it in a dynamic class that knows to call the method before all other
 * calls.
 * 
 * @author leo
 * 
 */
public class MapNodeAdapter extends AbstractMap<String,MyGraphNode>
	implements MyGraphNode {

    private Node node;
    private Set<Map.Entry<String, MyGraphNode>> entries;

    public MapNodeAdapter(Node node) {
	this.node = node;
    }

    public void updateNodes() {
	if (entries == null) {
	    entries = new HashSet<Map.Entry<String, MyGraphNode>>();

	    for (Relationship r : node.getRelationships(RelTypes.MAP,
		    Direction.OUTGOING)) {

		Node endNode = r.getEndNode();
		Node chosenNode = endNode;
		if (endNode.hasRelationship(Direction.OUTGOING,
			RelTypes.INCOMING_VERSION))
		    chosenNode = endNode
			    .getRelationships(Direction.OUTGOING,
				    RelTypes.INCOMING_VERSION).iterator()
			    .next().getEndNode();
		entries.add(new AbstractMap.SimpleImmutableEntry<String, MyGraphNode>(
			(String) r.getProperty("name"), NodeTypes
				.wrapAsJsonNode(chosenNode)));
	    }
	}
    }

    @Override
    public Set<java.util.Map.Entry<String, MyGraphNode>> entrySet() {
	updateNodes();
	return entries;
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
	strategy.replaceNode(context, node, values);

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
	return node.getGraphDatabase();
    }

    @Override
    public MyGraphNode get(JsonPath path) {
	return path.read(this);
    }

}