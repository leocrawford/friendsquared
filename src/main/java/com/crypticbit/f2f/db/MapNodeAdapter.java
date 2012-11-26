package com.crypticbit.f2f.db;

import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class extends ObjectNode, but requires a call to updateNodes before calls
 * to any method that uses _children, so in practice we will typically wrap it
 * in a dynamic class that knows to call the method before all other calls.
 * 
 * @author leo
 * 
 */
class MapNodeAdapter extends ObjectNode implements JsonNodeGraphAdapter {

	/** The database node that backs this element */
	private Node node;

	public MapNodeAdapter(JsonNodeFactory nc, Node node) {
		super(nc);
		this.node = node;

	}

	public void updateNodes() {
		_children = new TreeMap<String, JsonNode>();
		for (Relationship r : node.getRelationships(RelTypes.MAP,
				Direction.OUTGOING)) {
			_children.put((String) r.getProperty("name"),
					NodeTypes.wrapAsJsonNode(r.getEndNode()));

		}

	}

	public Node getDatabaseNode() {
		return node;
	}
}