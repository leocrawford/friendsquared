package com.crypticbit.f2f.db.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class TimestampVersionStrategy extends VersionStrategyImpl  {

    public TimestampVersionStrategy(VersionStrategy parent) {
	super(parent);
    }

    @Override
    public void replaceNode(Context context, Node graphNode, JsonNode values) {
	getParent().replaceNode(context, createNewVersion(context,graphNode), values);

    }

    private static Node createNewVersion(Context context, Node graphNode) {
	Node newNode;
	newNode = context.getGraphDb().createNode();
	graphNode.createRelationshipTo(newNode, RelationshipTypes.INCOMING_VERSION);
	return newNode;
    }
}
