package com.crypticbit.f2f.db.neo4j.nodes;

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
import com.crypticbit.f2f.db.neo4j.nodes.EmptyGraphNode.PotentialRelationship;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations.UpdateOperation;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.internal.PathToken;

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
    public ArrayGraphNode(Node node, Relationship incomingRelationship,FundementalDatabaseOperations fdo) {
	this.node = node;
	this.virtualSuperclass = new GraphNodeImpl(this, incomingRelationship, fdo);
    }

    @Override
    public Neo4JGraphNode get(int index) {
	updateNodes();
	if (index == children.length)
	    return add();
	else
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
			NodeTypes.wrapAsGraphNode(r.getEndNode(), r, getStrategy()));
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
    public Neo4JGraphNode navigate(String path) throws IllegalJsonException {
	return virtualSuperclass.navigate(path);
    }

    @Override
    public String toJsonString() {
	return virtualSuperclass.toJsonString();
    }

    @Override
    public void write(String values) throws IllegalJsonException, JsonPersistenceException {
	virtualSuperclass.write(values);
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
    public Neo4JGraphNode put(String key) throws JsonPersistenceException {
	throw new JsonPersistenceException("It's not possible to add a map element to an array node");
    }

    @Override
    public EmptyGraphNode add() {

	return new EmptyGraphNode(new PotentialRelationship() {
	    private Relationship r;

	    @Override
	    public Relationship create() {
		getStrategy().update(virtualSuperclass.getIncomingRelationship(), false,
			new UpdateOperation() {
			    @Override
			    public void updateElement(Node node) {
				r = addElementToArray(getStrategy(), node, findNextUnusedIndex(node));
			    }
			});
		return r;
	    }
	},getStrategy());

    }

    static Relationship addElementToArray(FundementalDatabaseOperations dal, Node node, int index) {
	Node newNode = dal.createNewNode();
	Relationship r = node.createRelationshipTo(newNode, RelationshipTypes.ARRAY);
	r.setProperty(RelationshipParameters.INDEX.name(), index);
	return r;
    }

    public void removeElementFromArray(final Relationship relationshipToParent, final int index) {
	// this is a delete (on node) and update (on parent)
	final FundementalDatabaseOperations db = getStrategy();
	db.update(relationshipToParent, false, new UpdateOperation() {
	    @Override
	    public void updateElement(Node node) {
		for (Relationship relationshipToNodeToDelete : node.getRelationships(Direction.OUTGOING,
			RelationshipTypes.ARRAY))
		    if (relationshipToNodeToDelete.getProperty(RelationshipParameters.INDEX.name()).equals(index))
			db.delete(relationshipToNodeToDelete);
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
    public FundementalDatabaseOperations getStrategy() {
	return virtualSuperclass.getStrategy();
    }

    @Override
    public Neo4JGraphNode navigate(PathToken token) throws IllegalJsonException {
	if (!token.isArrayIndexToken())
	    throw new IllegalJsonException("Expecting an array element in json path expression: " + token.getFragment());
	return get(token.getArrayIndex());
    }

}