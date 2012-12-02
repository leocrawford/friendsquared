package com.crypticbit.f2f.db.strategies;

import java.util.Iterator;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.types.NodeTypes;
import com.crypticbit.f2f.db.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class UnversionedVersionStrategy extends VersionStrategyImpl {

    public UnversionedVersionStrategy(VersionStrategy parent) {
	super(parent);
    }

    @Override
    public void replaceNode(Context context, Node graphNode, JsonNode values) {
	copyJsonToGraph(context, graphNode, values);
    }

    private void copyJsonToGraph(Context context, Node graphNode, JsonNode jsonNode) {
	if (jsonNode.isContainerNode()) {
	    if (jsonNode.isArray()) {
		graphNode.setProperty("type", NodeTypes.ARRAY.toString());
		for (int loop = 0; loop < jsonNode.size(); loop++) {
		    Node newNode = context.getGraphDb().createNode();
		    graphNode.createRelationshipTo(newNode, RelationshipTypes.ARRAY).setProperty("index", loop);
		    copyJsonToGraph(context, newNode, jsonNode.get(loop));
		}
	    }
	    if (jsonNode.isObject()) {
		graphNode.setProperty("type", NodeTypes.MAP.toString());
		Iterator<String> fieldNamesIterator = jsonNode.fieldNames();
		while (fieldNamesIterator.hasNext()) {
		    String f = fieldNamesIterator.next();
		    JsonNode e = jsonNode.get(f);
		    Node newNode = context.getGraphDb().createNode();
		    graphNode.createRelationshipTo(newNode, RelationshipTypes.MAP).setProperty("name", f);
		    ;
		    copyJsonToGraph(context, newNode, e);
		}
	    }
	} else {
	    graphNode.setProperty("type", NodeTypes.VALUE.toString());
	    graphNode.setProperty("value", jsonNode.toString());
	}

    }
}
