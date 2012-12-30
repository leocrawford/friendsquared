package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations.UpdateOperation;
import com.crypticbit.f2f.db.neo4j.strategies.Neo4JSimpleFdoAdapter;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Wraps a database node as a node that holds Map's
 * 
 */
public class MapGraphNode extends AbstractMap<String, Neo4JGraphNode> implements Neo4JGraphNode {

    private Node node;
    private Set<Map.Entry<String, Neo4JGraphNode>> children;
    private GraphNodeImpl virtualSuperclass;

    public MapGraphNode(Node node, Relationship incomingRelationship) {
	this.node = node;
	this.virtualSuperclass = new GraphNodeImpl(this, incomingRelationship);
    }

    @Override
    public Node getDatabaseNode() {
	return node;
    }

    @Override
    public Set<java.util.Map.Entry<String, Neo4JGraphNode>> entrySet() {
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
	    children = new HashSet<Map.Entry<String, Neo4JGraphNode>>();

	    for (Relationship r : node.getRelationships(RelationshipTypes.MAP, Direction.OUTGOING)) {

		Node endNode = r.getEndNode();
		Node chosenNode = endNode;
		if (endNode.hasRelationship(Direction.OUTGOING, RelationshipTypes.INCOMING_VERSION)) {
		    chosenNode = endNode.getRelationships(Direction.OUTGOING, RelationshipTypes.INCOMING_VERSION)
			    .iterator().next().getEndNode();
		}
		children.add(new AbstractMap.SimpleImmutableEntry<String, Neo4JGraphNode>((String) r
			.getProperty(RelationshipParameters.KEY.name()), NodeTypes.wrapAsGraphNode(chosenNode, r)));
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
			final Iterator<Map.Entry<String, Neo4JGraphNode>> i = MapGraphNode.this.entrySet().iterator();
			return new Iterator<Map.Entry<String, JsonNode>>() {

			    @Override
			    public boolean hasNext() {
				return i.hasNext();
			    }

			    @Override
			    public Map.Entry<String, JsonNode> next() {
				java.util.Map.Entry<String, Neo4JGraphNode> temp = i.next();
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
    public Neo4JGraphNode navigate(String path) {
	return virtualSuperclass.navigate(path);
    }

    @Override
    public String toJsonString() {
	return virtualSuperclass.toJsonString();
    }

    @Override
    public void overwrite(String values) throws IllegalJsonException, JsonPersistenceException {
	virtualSuperclass.overwrite(values);
    }

    @Override
    public List<History> getHistory() {
	return virtualSuperclass.getHistory();
    }

    @Override
    public long getTimestamp() {
	return virtualSuperclass.getTimestamp();
    }

    @Override
    public void put(final String key, String json) throws IllegalJsonException, JsonPersistenceException {
	if (this.containsKey(key))
	    this.get(key).overwrite(json);
	else {
	    FundementalDatabaseOperations db = getStrategy();
	    try {
		final JsonNode values = new ObjectMapper().readTree(json);
		// this is a create, and an update (on the parent)
		db.update(virtualSuperclass.getIncomingRelationship(), false, new UpdateOperation() {
		    @Override
		    public void updateElement(Neo4JSimpleFdoAdapter dal, Node node) {
			addElementToMap(dal, node, key, values);
		    }
		});
		db.commit();
	    } catch (JsonProcessingException jpe) {
		db.rollback();
		throw new IllegalJsonException("The JSON string was badly formed: " + json, jpe);
	    } catch (IOException e) {
		db.rollback();
		throw new JsonPersistenceException("IOException whilst writing data to database", e);
	    }
	}
    }

    static Node addElementToMap(Neo4JSimpleFdoAdapter dal, Node node, final String key, JsonNode json) {
	Node newNode = dal.createNewNode();
	GraphNodeImpl.populateWithJson(dal, newNode, json);
	node.createRelationshipTo(newNode, RelationshipTypes.MAP).setProperty(RelationshipParameters.KEY.name(), key);
	return newNode;
    }

    public void removeElementFromMap(Relationship relationshipToParent, final String key) {
	// this is a delete (on node) and update (on parent)
	FundementalDatabaseOperations db = getStrategy();
	db.update(relationshipToParent, false, new UpdateOperation() {
	    @Override
	    public void updateElement(Neo4JSimpleFdoAdapter dal, Node node) {
		for (Relationship relationshipToNodeToDelete : node.getRelationships(Direction.OUTGOING,
			RelationshipTypes.MAP))
		    if (relationshipToNodeToDelete.getProperty(RelationshipParameters.KEY.name()).equals(key))
			dal.delete(relationshipToNodeToDelete);
	    }
	});
    }

    @Override
    public void add(String json) throws JsonPersistenceException {
	throw new JsonPersistenceException("It's not possible to add an array element to a map node. ");
    }

    @Override
    public FundementalDatabaseOperations getStrategy() {
	return virtualSuperclass.getStrategy();
    }
}