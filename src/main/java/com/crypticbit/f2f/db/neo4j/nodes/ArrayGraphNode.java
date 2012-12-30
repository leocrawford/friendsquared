package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.DatabaseAbstractionLayer;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This class hold GraphNodes that represent array's. It provides conversions to
 * JsonNode.
 * 
 * @author leo
 * 
 */
public class ArrayGraphNode extends AbstractList<Neo4JGraphNode> implements
		Neo4JGraphNode {

	private Node node;
	private Neo4JGraphNode children[];
	private GraphNodeImpl virtualSuperclass;

	/**
	 * Create a node that represents this graph node
	 * 
	 * @param incomingRelationship
	 */
	public ArrayGraphNode(Node node, Relationship incomingRelationship) {
		this.node = node;
		this.virtualSuperclass = new GraphNodeImpl(this, incomingRelationship);
	}

	@Override
	public Neo4JGraphNode get(int index) {
		updateNodes();
		return children[index];
	}

	@Override
	public int size() {
		updateNodes();
		return children.length;
	}

	@Override
	public JsonNode toJsonNode() {
		return new ArrayNode(null, wrapChildrenAsJsonNode()) {
		};
	}

	/**
	 * Read the node's relationships to build the children list. This is
	 * typically done lazily
	 */
	public void updateNodes() {
		if (children == null) {
			Map<Integer, Neo4JGraphNode> map = new TreeMap<Integer, Neo4JGraphNode>();
			for (Relationship r : node.getRelationships(
					RelationshipTypes.ARRAY, Direction.OUTGOING)) {
				map.put((Integer) r.getProperty(RelationshipParameters.INDEX
						.name()), NodeTypes.wrapAsGraphNode(r.getEndNode(), r));
			}
			children = map.values().toArray(new Neo4JGraphNode[map.size()]);
		}
	}

	/**
	 * The children are exposed as a collection of GraphNode's. In order to
	 * build a JsonNode, they need to be converted to a collection of
	 * JsonNode's.
	 * 
	 * @return the children collection, exposed as a collection of JsonNode's
	 */
	private List<JsonNode> wrapChildrenAsJsonNode() {
		return new AbstractList<JsonNode>() {

			@Override
			public JsonNode get(int index) {
				return ArrayGraphNode.this.get(index).toJsonNode();
			}

			@Override
			public int size() {
				return ArrayGraphNode.this.size();
			}
		};
	}

	@Override
	public Node getDatabaseNode() {
		return node;
	}

	// delegate methods

	@Override
	public Neo4JGraphNode navigate(String path) {
		return virtualSuperclass.navigate(path);
	}

	@Override
	public String toJsonString() {
		return virtualSuperclass.toJsonString();
	}

	@Override
	public void overwrite(String values) throws IllegalJsonException,
			JsonPersistenceException {
		virtualSuperclass.overwrite(values);
	}

	@Override
	public List<History> getHistory() {
		return virtualSuperclass.getHistory();
	}

	@Override
	public long getTimestamp() {
		return virtualSuperclass.getTimestamp();
	}

	@Override
	public void put(String key, String json) throws JsonPersistenceException {
		throw new JsonPersistenceException(
				"It's not possible to add a map element to an array node");
	}

	@Override
	public void add(String json) throws IllegalJsonException,
			JsonPersistenceException {
		DatabaseAbstractionLayer db = getStrategy();
		db.beginTransaction();
		try {
			JsonNode values = new ObjectMapper().readTree(json);
			db.addElementToArray(virtualSuperclass.getIncomingRelationship(),
					values);
			db.successTransaction();
		} catch (JsonProcessingException jpe) {
			db.failureTransaction();
			throw new IllegalJsonException("The JSON string was badly formed: "
					+ json, jpe);
		} catch (IOException e) {
			db.failureTransaction();
			throw new JsonPersistenceException(
					"IOException whilst writing data to database", e);
		} finally {
			db.finishTransaction();
		}

	}

	@Override
	public DatabaseAbstractionLayer getStrategy() {
		return virtualSuperclass.getStrategy();
	}

}