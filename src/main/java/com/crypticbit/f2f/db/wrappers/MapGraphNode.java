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
 * Wraps a database node as a node that holds Map's
 * 
 */
public class MapGraphNode extends AbstractMap<String, GraphNode> implements GraphNode {

    private Node node;
    private Set<Map.Entry<String, GraphNode>> children;
    private GraphNodeImpl virtualSuperclass;

    public MapGraphNode(Node node) {
	this.node = node;
	this.virtualSuperclass = new GraphNodeImpl(this);
    }

    @Override
    public Node getDatabaseNode() {
	return node;
    }

    @Override
    public Set<java.util.Map.Entry<String, GraphNode>> entrySet() {
	updateNodes();
	return children;
    }

    @Override
    public JsonNode toJsonNode() {
	return new ObjectNode(null, wrapChildrenAsJsonNode()) {
	};
    }

    /**
     * Loads all the relationships, and packages them up as a Map which backs
     * this class. Typically called lazily
     */
    public void updateNodes() {
	if (children == null) {
	    children = new HashSet<Map.Entry<String, GraphNode>>();

	    for (Relationship r : node.getRelationships(RelationshipTypes.MAP, Direction.OUTGOING)) {

		Node endNode = r.getEndNode();
		Node chosenNode = endNode;
		if (endNode.hasRelationship(Direction.OUTGOING, RelationshipTypes.INCOMING_VERSION)) {
		    chosenNode = endNode.getRelationships(Direction.OUTGOING, RelationshipTypes.INCOMING_VERSION)
			    .iterator().next().getEndNode();
		}
		children.add(new AbstractMap.SimpleImmutableEntry<String, GraphNode>((String) r.getProperty("name"),
			NodeTypes.wrapAsGraphNode(chosenNode)));
	    }
	}
    }

    /** Converts the children to JsonNode's */
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
				java.util.Map.Entry<String, GraphNode> temp = i.next();
				return new AbstractMap.SimpleImmutableEntry<String, JsonNode>(temp.getKey(), temp
					.getValue().toJsonNode());
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

    // delegate methods

    @Override
    public GraphNode get(JsonPath path) {
	return virtualSuperclass.get(path);
    }

    @Override
    public String toJsonString() {
	return virtualSuperclass.toJsonString();
    }

    @Override
    public void put(JsonNode values) {
	virtualSuperclass.put(values);
    }

}