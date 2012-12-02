package com.crypticbit.f2f.db.neo4j.nodes;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.strategies.Context;
import com.crypticbit.f2f.db.neo4j.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.neo4j.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.neo4j.strategies.VersionStrategy;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;

/**
 * This class hold GraphNodes that represent array's. It provides conversions to
 * JsonNode.
 * 
 * @author leo
 * 
 */
public class ArrayGraphNode extends AbstractList<GraphNode> implements GraphNode {

    private Node node;
    private GraphNode children[];
    private GraphNodeImpl virtualSuperclass;

    /** Create a node that represents this graph node */
    public ArrayGraphNode(Node node) {
	this.node = node;
	this.virtualSuperclass = new GraphNodeImpl(this);
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
    public JsonNode toJsonNode() {
	return new ArrayNode(null, wrapChildrenAsJsonNode()) {
	};
    }

    /**
     * Read the node's relationships to build the children list. This is
     * typically done lazily
     */
    public void updateNodes() {
	if (children == null) {
	    Map<Integer, GraphNode> map = new TreeMap<Integer, GraphNode>();
	    for (Relationship r : node.getRelationships(RelationshipTypes.ARRAY, Direction.OUTGOING)) {
		map.put((Integer) r.getProperty("index"), NodeTypes.wrapAsGraphNode(r.getEndNode()));
	    }
	    children = map.values().toArray(new GraphNode[map.size()]);
	}
    }

    /**
     * The children are exposed as a collection of GraphNode's. In order to
     * build a JsonNode, they need to be converted to a collection of
     * JsonNode's.
     * 
     * @return the children collection, exposed as a collection of JsonNode's
     */
    private List<JsonNode> wrapChildrenAsJsonNode() {
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

    @Override
    public Node getDatabaseNode() {
	return node;
    }

    // delegate methods

    @Override
    public GraphNode navigate(String path) {
	return virtualSuperclass.navigate(path);
    }

    @Override
    public String toJsonString() {
	return virtualSuperclass.toJsonString();
    }

    @Override
    public void put(String values) throws IllegalJsonException, JsonPersistenceException {
	virtualSuperclass.put(values);
    }

}