package com.crypticbit.f2f.db.wrappers;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.NodeTypes;
import com.crypticbit.f2f.db.RelTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * This class extends ArrayNode, but requires a call to updateNodes before calls
 * to any method that uses _children, so in practice we will typically wrap it
 * in a dynamic class that knows to call the method before all other calls.
 * 
 * @author leo
 * 
 */
public abstract class ArrayNodeAdapter extends ArrayNode implements
	JsonNodeGraphAdapter {

    private OutwardFacingJsonNodeGraphAdapter graphParent;

    public ArrayNodeAdapter(JsonNodeFactory nc, Node node) {
	super(nc);
	this.graphParent = new JsonNodeGraphAdapterImpl(node);
    }

    @Override
    public void updateNodes() {
	if (_children == null) {
	    Map<Integer, JsonNode> map = new TreeMap<Integer, JsonNode>();
	    for (Relationship r : getDatabaseNode().getRelationships(
		    RelTypes.ARRAY, Direction.OUTGOING)) {
		map.put((Integer) r.getProperty("index"),
			NodeTypes.wrapAsJsonNode(r.getEndNode()));
	    }
	    _children = new ArrayList<JsonNode>(map.values());
	}
    }
    
    public OutwardFacingJsonNodeGraphAdapter getGraphParent() {
	return graphParent;
    }

}