package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;

public class TimeStampedHistoryAdapter extends CompoundFdoAdapter {

    public TimeStampedHistoryAdapter(GraphDatabaseService graphDb, FundementalDatabaseOperations nextAdapter) {
	super(graphDb, nextAdapter);
    }

    @Override
    public Node createNewNode(UpdateOperation createOperation) {
	Node result = super.createNewNode(createOperation);
	addTimestampToNode(result);
	return result;
    }

    private void addTimestampToNode(Node node) {
	node.setProperty("timestamp", System.currentTimeMillis());
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation operation) {
	Node nodeToUpdate = relationshipToParent.getEndNode();
	Node clonedNode = super.getGraphDB().createNode();
	if (!removeEverything) {
	    copyOutgoingRelationships(nodeToUpdate, clonedNode);
	    copyProperties(nodeToUpdate, clonedNode);
	}
	Relationship replacementRelationship = cloneRelationshipToNewEndNode(clonedNode, relationshipToParent);
	relationshipToParent.delete();
	addTimestampToNode(clonedNode);
	clonedNode.createRelationshipTo(nodeToUpdate, RelationshipTypes.PREVIOUS_VERSION);
	super.update(replacementRelationship, false, operation);
    }

    private void copyProperties(Node fromNode, Node toNode) {
	for (String key : fromNode.getPropertyKeys())
	    toNode.setProperty(key, fromNode.getProperty(key));

    }

    private void copyOutgoingRelationships(Node fromNode, Node toNode) {
	for (Relationship rel : fromNode.getRelationships(Direction.OUTGOING)) {
	    if (!rel.isType(RelationshipTypes.PREVIOUS_VERSION))
		cloneRelationshipFromNewStartNode(toNode, rel);
	}

    }

    private Relationship cloneRelationshipToNewEndNode(Node newEndNode, Relationship oldRelationship) {
	return cloneRelationship(oldRelationship.getStartNode(), newEndNode, oldRelationship);
    }

    private Relationship cloneRelationshipFromNewStartNode(Node newStartNode, Relationship oldRelationship) {
	return cloneRelationship(newStartNode, oldRelationship.getEndNode(), oldRelationship);
    }

    private Relationship cloneRelationship(Node newStartNode, Node newEndNode, Relationship oldRelationship) {
	Relationship newRelationship = newStartNode.createRelationshipTo(newEndNode, oldRelationship.getType());
	for (String key : oldRelationship.getPropertyKeys())
	    newRelationship.setProperty(key, oldRelationship.getProperty(key));
	return newRelationship;
    }

    @Override
    public void delete(Relationship relationshipToNodeToDelete) {
	// FIXME - not implemented
    }

}
