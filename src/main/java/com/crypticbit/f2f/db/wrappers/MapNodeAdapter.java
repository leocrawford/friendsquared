package com.crypticbit.f2f.db.wrappers;

import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.NodeTypes;
import com.crypticbit.f2f.db.RelTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class extends ObjectNode, but requires a call to updateNodes before
 * calls to any method that uses _children, so in practice we will typically
 * wrap it in a dynamic class that knows to call the method before all other
 * calls.
 * 
 * @author leo
 * 
 */
public abstract class MapNodeAdapter extends ObjectNode implements
	JsonNodeGraphAdapter {

    protected OutwardFacingJsonNodeGraphAdapter graphParent;

    public MapNodeAdapter(JsonNodeFactory nc, Node node) {
	super(nc);
	this.graphParent = new JsonNodeGraphAdapterImpl(node);

    }

    @Override
    public void updateNodes() {
	_children = new TreeMap<String, JsonNode>();
	for (Relationship r : getDatabaseNode().getRelationships(RelTypes.MAP,
		Direction.OUTGOING)) {

	    Node endNode = r.getEndNode();
	    Node chosenNode = endNode;
	    if (endNode.hasRelationship(Direction.OUTGOING,
		    RelTypes.INCOMING_VERSION))
		chosenNode = endNode
			.getRelationships(Direction.OUTGOING,
				RelTypes.INCOMING_VERSION).iterator().next()
			.getEndNode();
	    _children.put((String) r.getProperty("name"),
		    NodeTypes.wrapAsJsonNode(chosenNode));

	}

    }
    
    public OutwardFacingJsonNodeGraphAdapter getGraphParent() {
	return graphParent;
    }
}