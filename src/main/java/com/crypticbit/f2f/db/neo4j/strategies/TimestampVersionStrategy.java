package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class TimestampVersionStrategy extends VersionStrategyImpl {

    public TimestampVersionStrategy(VersionStrategyImpl successor) {
	super(successor);
    }

    // @Override
    // public void addElementToMap(Context context, Node parent, JsonNode json,
    // String key) {
    // // TODO Auto-generated method stub
    //
    // }

    // @Override
    // public void addNodeToArray(Context context, Node parent, JsonNode json) {
    // // TODO Auto-generated method stub
    //
    // }

    // @Override
    // public void addNodeToArray(Context context, Node parent, JsonNode json,
    // int index) {
    // // TODO Auto-generated method stub
    //
    // }

    @Override
    public Node createNewNode(Context context, JsonNode jsonNode) {
	Node successorResult = getSuccessor().createNewNode(context, jsonNode);
	successorResult.setProperty("timestamp", System.currentTimeMillis());
	return successorResult;
    }

    // @Override
    // public Relationship replaceNode(Context context, Relationship
    // oldRelationship, JsonNode values) {
    // }

    @Override
    public void replaceRelationship(Relationship oldRelationship, Relationship newRelationship) {
	addHistoryToNode(newRelationship.getEndNode(), oldRelationship.getEndNode());
	addHistoryToNode(newRelationship.getStartNode(), oldRelationship.getStartNode());
	this.getSuccessor().replaceRelationship(oldRelationship, newRelationship);
    }

    private void addHistoryToNode(Node newNode, Node oldNode) {
	if (newNode.getId() != oldNode.getId()) {
	    Relationship r = newNode.createRelationshipTo(oldNode, RelationshipTypes.HISTORY);
	    System.out.println("Just created " + r + " between " + r.getStartNode() + "," + r.getEndNode());
	}

    }
}
