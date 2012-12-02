package com.crypticbit.f2f.db.wrappers;

import java.util.AbstractList;
import java.util.Map;
import java.util.TreeMap;

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
 * This class extends ArrayNode, but requires a call to updateNodes before calls
 * to any method that uses _children, so in practice we will typically wrap it
 * in a dynamic class that knows to call the method before all other calls.
 * 
 * @author leo
 * 
 */
public class ArrayNodeAdapter  extends AbstractList<MyGraphNode> implements MyGraphNode {

    private Node node;
    private MyGraphNode children[];

    public ArrayNodeAdapter(Node node) {
	this.node = node;
    }

    public void updateNodes() {
	if (children == null) {
	    Map<Integer, MyGraphNode> map = new TreeMap<Integer, MyGraphNode>();
	    for (Relationship r : node.getRelationships(
		    RelTypes.ARRAY, Direction.OUTGOING)) {
		map.put((Integer) r.getProperty("index"),
			NodeTypes.wrapAsJsonNode(r.getEndNode()));
	    }
	    children = (MyGraphNode[]) map.values().toArray(new MyGraphNode[map.size()]);
	}
    }
    
    @Override
    public MyGraphNode get(int index) {
	updateNodes();
	return children[index];
    }

    @Override
    public int size() {
	updateNodes();
	return children.length;
    }
    
    @Override
    public MyGraphNode get(JsonPath path) {
	return path.read(this);
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

}