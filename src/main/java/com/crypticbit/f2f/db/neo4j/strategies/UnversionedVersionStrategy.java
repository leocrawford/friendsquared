package com.crypticbit.f2f.db.neo4j.strategies;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import scala.util.Random;

import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class UnversionedVersionStrategy extends VersionStrategyImpl {

    public UnversionedVersionStrategy(VersionStrategyImpl successor) {
	super(successor);
    }

    public void addElementToMap(Context context, Node parent, JsonNode json, String key) {
	parent.createRelationshipTo(getRoot().createNewNode(context,json), RelationshipTypes.MAP).setProperty("name", key);
    }

    public void addNodeToArray(Context context, Node parent, JsonNode json) {
	addNodeToArray(context, parent, json, findNextUnusedIndex(parent));
    }

    public Node createNewNode(Context context, JsonNode json) {
	Node newNode = context.getGraphDb().createNode();
	copyJsonToGraph(context, newNode, json);
	return newNode;
    }

    public void replaceNode(Context context, Relationship oldRelationship, JsonNode values) {
	Relationship newRealtionship = oldRelationship.getStartNode().createRelationshipTo(
		getRoot().createNewNode(context, values), oldRelationship.getType());
	// clone properties
	for (String key : oldRelationship.getPropertyKeys()) {
	    newRealtionship.setProperty(key, oldRelationship.getProperty(key));
	}
	// remove old relationship
	oldRelationship.delete();
    }

    public void addNodeToArray(Context context, Node parent, JsonNode json, int index) {
	parent.createRelationshipTo(getRoot().createNewNode(context,json), RelationshipTypes.ARRAY).setProperty("index", index);
    }

    private void copyJsonToGraph(Context context, Node graphNode, JsonNode jsonNode) {
	if (jsonNode.isContainerNode()) {
	    if (jsonNode.isArray()) {
		graphNode.setProperty("type", NodeTypes.ARRAY.toString());
		for (int loop = 0; loop < jsonNode.size(); loop++) {
		    getRoot().addNodeToArray(context, graphNode, jsonNode.get(loop), loop);
		}
	    }
	    if (jsonNode.isObject()) {
		graphNode.setProperty("type", NodeTypes.MAP.toString());
		Iterator<String> fieldNamesIterator = jsonNode.fieldNames();
		while (fieldNamesIterator.hasNext()) {
		    String f = fieldNamesIterator.next();
		    getRoot().addElementToMap(context, graphNode, jsonNode.get(f), f);
		}
	    }
	} else {
	    graphNode.setProperty("type", NodeTypes.VALUE.toString());
	    graphNode.setProperty("value", jsonNode.toString());
	}

    }

    private int findNextUnusedIndex(Node parent) {
	int max = 0;
	for (Relationship a : parent.getRelationships(Direction.OUTGOING, RelationshipTypes.ARRAY)) {
	    if ((int) a.getProperty("index") > max)
		max = (int) a.getProperty("index");
	}
	return max + 1;
    }
}
