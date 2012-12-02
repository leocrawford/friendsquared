package com.crypticbit.f2f.db.wrappers;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class MapGraphNode extends AbstractMap<String, GraphNode> implements
	GraphNode {

    private Node node;
    private Set<Map.Entry<String, GraphNode>> entries;

    public MapGraphNode(Node node) {
	this.node = node;
    }

    public void updateNodes() {
	if (entries == null) {
	    entries = new HashSet<Map.Entry<String, GraphNode>>();

	    for (Relationship r : node.getRelationships(RelationshipTypes.MAP,
		    Direction.OUTGOING)) {

		Node endNode = r.getEndNode();
		Node chosenNode = endNode;
		if (endNode.hasRelationship(Direction.OUTGOING,
			RelationshipTypes.INCOMING_VERSION))
		    chosenNode = endNode
			    .getRelationships(Direction.OUTGOING,
				    RelationshipTypes.INCOMING_VERSION)
			    .iterator().next().getEndNode();
		entries.add(new AbstractMap.SimpleImmutableEntry<String, GraphNode>(
			(String) r.getProperty("name"), NodeTypes
				.wrapAsGraphNode(chosenNode)));
	    }
	}
    }

    @Override
    public Set<java.util.Map.Entry<String, GraphNode>> entrySet() {
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
    public GraphNode get(JsonPath path) {
	return path.read(this);
    }

    @Override
    public String toJsonString() {
	return toJsonNode().toString();
    }

    @Override
    public JsonNode toJsonNode() {
	return new ObjectNode(null, wrapChildrenAsJsonNode()) {
	};
    }

    public Map<String, JsonNode> wrapChildrenAsJsonNode() {
	return new AbstractMap<String, JsonNode>() {

	    @Override
	    public Set<Map.Entry<String, JsonNode>> entrySet() {
		return new AbstractSet<Map.Entry<String, JsonNode>>() {

		    @Override
		    public Iterator<Map.Entry<String, JsonNode>> iterator() {
			final Iterator<Map.Entry<String, GraphNode>> i = MapGraphNode.this.entrySet().iterator();
			return new Iterator<Map.Entry<String, JsonNode>>() {

			    @Override
			    public boolean hasNext() {
				return i.hasNext();
			    }

			    @Override
			    public Map.Entry<String, JsonNode> next() {
				java.util.Map.Entry<String, GraphNode> temp = i
					.next();
				return new AbstractMap.SimpleImmutableEntry<String, JsonNode>(
					temp.getKey(), temp.getValue()
						.toJsonNode());
			    }

			    @Override
			    public void remove() {
				i.remove();

			    }
			};
		    }

		    @Override
		    public int size() {
			return MapGraphNode.this.entrySet().size();
		    }
		};
	    }

	};
    }

}