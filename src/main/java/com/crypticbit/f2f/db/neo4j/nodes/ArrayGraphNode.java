package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations.UpdateOperation;
import com.crypticbit.f2f.db.neo4j.strategies.Neo4JSimpleFdoAdapter;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This class hold GraphNodes that represent array's. It provides conversions to
 * JsonNode.
 * 
 * @author leo
 * 
 */
public class ArrayGraphNode extends AbstractList<Neo4JGraphNode> implements Neo4JGraphNode {

    private Node node;
    private Neo4JGraphNode children[];
    private GraphNodeImpl virtualSuperclass;

    /**
     * Create a node that represents this graph node
     * 
     * @param incomingRelationship
     */
    public ArrayGraphNode(Node node, Relationship incomingRelationship) {
	this.node = node;
	this.virtualSuperclass = new GraphNodeImpl(this, incomingRelationship);
    }

    @Override
    public Neo4JGraphNode get(int index) {
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
	    Map<Integer, Neo4JGraphNode> map = new TreeMap<Integer, Neo4JGraphNode>();
	    for (Relationship r : node.getRelationships(RelationshipTypes.ARRAY, Direction.OUTGOING)) {
		map.put((Integer) r.getProperty(RelationshipParameters.INDEX.name()),
			NodeTypes.wrapAsGraphNode(r.getEndNode(), r));
	    }
	    children = map.values().toArray(new Neo4JGraphNode[map.size()]);
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
    public void put(String key, String json) throws JsonPersistenceException {
	throw new JsonPersistenceException("It's not possible to add a map element to an array node");
    }

    @Override
    public void add(String json) throws IllegalJsonException, JsonPersistenceException {
	Neo4JSimpleFdoAdapter db = getStrategy();
	try {
	    final JsonNode values = new ObjectMapper().readTree(json);
	    db.update(virtualSuperclass.getIncomingRelationship(), false, new UpdateOperation() {
		@Override
		public void updateElement(Neo4JSimpleFdoAdapter dal, Node node) {
		    addElementToArray(dal, node, findNextUnusedIndex(node), values);
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

    static Node addElementToArray(Neo4JSimpleFdoAdapter dal, Node node, int index, JsonNode json) {
	Node newNode = dal.createNewNode();
	GraphNodeImpl.populateWithJson(dal, newNode, json);
	node.createRelationshipTo(newNode, RelationshipTypes.ARRAY).setProperty(RelationshipParameters.INDEX.name(),
		index);
	return newNode;
    }

    public void removeElementFromArray(Relationship relationshipToParent, final int index) {
	// this is a delete (on node) and update (on parent)
	Neo4JSimpleFdoAdapter db = getStrategy();
	db.update(relationshipToParent, false, new UpdateOperation() {
	    @Override
	    public void updateElement(Neo4JSimpleFdoAdapter dal, Node node) {
		for (Relationship relationshipToNodeToDelete : node.getRelationships(Direction.OUTGOING,
			RelationshipTypes.ARRAY))
		    if (relationshipToNodeToDelete.getProperty(RelationshipParameters.INDEX.name()).equals(index))
			dal.delete(relationshipToNodeToDelete);
	    }
	});
    }

    private int findNextUnusedIndex(Node parent) {
	int max = 0;
	for (Relationship a : parent.getRelationships(Direction.OUTGOING, RelationshipTypes.ARRAY)) {
	    if ((int) a.getProperty(RelationshipParameters.INDEX.name()) > max)
		max = (int) a.getProperty(RelationshipParameters.INDEX.name());
	}
	return max + 1;
    }

    @Override
    public Neo4JSimpleFdoAdapter getStrategy() {
	return virtualSuperclass.getStrategy();
    }

}