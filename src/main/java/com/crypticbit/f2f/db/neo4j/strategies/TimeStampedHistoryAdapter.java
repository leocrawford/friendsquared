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
	Node result = super.createNewNode(createOperation.add(getTimestampOperation()));
	return result;
    }

    private UpdateOperation getTimestampOperation() {
	return new UpdateOperation() {
	    @Override
	    public void updateElement(Node graphNode, FundementalDatabaseOperations dal) {
		graphNode.setProperty("timestamp", System.currentTimeMillis());
	    }
	};
    }

    @Override
    public void update(final Relationship relationshipToParent, final boolean removeEverything,
	    final UpdateOperation operation) {
	final Node nodeToUpdate = relationshipToParent.getEndNode();
	createNewNode(new UpdateOperation() {
	    @Override
	    public void updateElement(Node newGraphNode, FundementalDatabaseOperations dal) {
		if (!removeEverything) {
		    copyOutgoingRelationships(nodeToUpdate, newGraphNode);
		    copyProperties(nodeToUpdate, newGraphNode);
		}
		cloneRelationshipToNewEndNode(newGraphNode, relationshipToParent);
		relationshipToParent.delete();
		newGraphNode.createRelationshipTo(nodeToUpdate, RelationshipTypes.PREVIOUS_VERSION);
	    }
	}.add(operation));

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
