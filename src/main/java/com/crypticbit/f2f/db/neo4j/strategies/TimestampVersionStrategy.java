package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class TimestampVersionStrategy extends VersionStrategyImpl {

    public TimestampVersionStrategy(VersionStrategyImpl successor) {
	super(successor);
    }

    private static Node createNewVersion(Context context, Node oldNode) {
	Node newNode;
	newNode = context.getGraphDb().createNode();
	newNode.createRelationshipTo(oldNode, RelationshipTypes.HISTORY);
	newNode.setProperty("timestamp", System.currentTimeMillis());
	return newNode;
    }

    @Override
    public void addElementToMap(Context context, Node parent, JsonNode json, String key) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void addNodeToArray(Context context, Node parent, JsonNode json) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void addNodeToArray(Context context, Node parent, JsonNode json, int index) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public Node createNewNode(Context context, JsonNode jsonNode) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void replaceNode(Context context, Relationship oldRelationship, JsonNode values) {
	// TODO Auto-generated method stub
	
    }


   
}
