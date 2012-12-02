package com.crypticbit.f2f.db.wrappers;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.strategies.Context;
import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.crypticbit.f2f.db.types.NodeTypes;
import com.crypticbit.f2f.db.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;

/**
 * This class extends ArrayNode, but requires a call to updateNodes before calls
 * to any method that uses _children, so in practice we will typically wrap it
 * in a dynamic class that knows to call the method before all other calls.
 * 
 * @author leo
 * 
 */
public class ArrayGraphNode extends AbstractList<GraphNode> implements
	GraphNode {

    private Node node;
    private GraphNode children[];

    public ArrayGraphNode(Node node) {
	this.node = node;
    }

    public void updateNodes() {
	if (children == null) {
	    Map<Integer, GraphNode> map = new TreeMap<Integer, GraphNode>();
	    for (Relationship r : node.getRelationships(
		    RelationshipTypes.ARRAY, Direction.OUTGOING)) {
		map.put((Integer) r.getProperty("index"),
			NodeTypes.wrapAsGraphNode(r.getEndNode()));
	    }
	    children = (GraphNode[]) map.values().toArray(
		    new GraphNode[map.size()]);
	}
    }

    @Override
    public GraphNode get(int index) {
	updateNodes();
	return children[index];
    }

    @Override
    public int size() {
	updateNodes();
	return children.length;
    }

    @Override
    public GraphNode get(JsonPath path) {
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

    @Override
    public String toJsonString() {
	return toJsonNode().toString();
    }

    @Override
    public JsonNode toJsonNode() {
	return new ArrayNode(null, wrapChildrenAsJsonNode()) {
	};
    }

    public List<JsonNode> wrapChildrenAsJsonNode() {
	return new AbstractList<JsonNode>() {

	    @Override
	    public JsonNode get(int index) {
		return ArrayGraphNode.this.get(index).toJsonNode();
	    }

	    @Override
	    public int size() {
		return ArrayGraphNode.this.size();
	    }
	};
    }

}