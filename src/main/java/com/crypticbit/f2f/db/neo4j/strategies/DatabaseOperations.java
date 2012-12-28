package com.crypticbit.f2f.db.neo4j.strategies;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public class DatabaseOperations implements ADO {

    private ADO actualDatabase = this;
    private GraphDatabaseService grapgDb;
    private Transaction tx;

    public DatabaseOperations(GraphDatabaseService grapgDb) {
	this.grapgDb = grapgDb;
    }

    public enum Properties {
	KEY, INDEX, TYPE, VALUE
    };
    
    public void beginTransaction() {
	tx = grapgDb.beginTx();
    }
    
    public void successTransaction() {
	tx.success();
    }
    
    public void finishTransaction() {
	tx.finish();
    }
    
    public void failureTransaction() {
	tx.failure();
    }

    public void addElementToMap(Relationship relationshipToParent, final String key, final JsonNode json) {
	// this is a create, and an update (on the parent)
	actualDatabase.update(relationshipToParent, false, new Operation() {
	    @Override
	    public void updateElement(Node node) {
		addElementToMap(node, key, json);
	    }
	});
    }

    Node addElementToMap(Node node, final String key, JsonNode json) {
	Node newNode = actualDatabase.createNewNode();
	populateWithJson(newNode, json);
	node.createRelationshipTo(newNode, RelationshipTypes.MAP).setProperty(Properties.KEY.name(), key);
	return newNode;
    }

    public void addElementToArray(Relationship relationshipToParent, final JsonNode json) {
	// this is a create, and an update (on the parent)
	actualDatabase.update(relationshipToParent, false, new Operation() {
	    @Override
	    public void updateElement(Node node) {
		addElementToArray(node, findNextUnusedIndex(node), json);
	    }
	});
    }

    Node addElementToArray(Node node, int index, JsonNode json) {
	Node newNode = actualDatabase.createNewNode();
	populateWithJson(newNode, json);
	node.createRelationshipTo(newNode, RelationshipTypes.ARRAY).setProperty(Properties.INDEX.name(), index);
	return newNode;
    }

    private void populateWithJson(Node graphNode, JsonNode jsonNode) {
	if (jsonNode.isContainerNode()) {
	    if (jsonNode.isArray()) {
		graphNode.setProperty(Properties.TYPE.name(), NodeTypes.ARRAY.toString());
		for (int loop = 0; loop < jsonNode.size(); loop++) {
		    addElementToArray(graphNode, loop, jsonNode.get(loop));
		}
	    }
	    if (jsonNode.isObject()) {
		graphNode.setProperty(Properties.TYPE.name(), NodeTypes.MAP.toString());
		Iterator<String> fieldNamesIterator = jsonNode.fieldNames();
		while (fieldNamesIterator.hasNext()) {
		    String f = fieldNamesIterator.next();
		    addElementToMap(graphNode, f, jsonNode.get(f));
		}
	    }
	} else {
	    graphNode.setProperty(Properties.TYPE.name(), NodeTypes.VALUE.toString());
	    graphNode.setProperty(Properties.VALUE.name(), jsonNode.toString());
	}
    }

    public void removeElementFromMap(Relationship relationshipToParent, final String key) {
	// this is a delete (on node) and update (on parent)
	actualDatabase.update(relationshipToParent, false, new Operation() {
	    @Override
	    public void updateElement(Node node) {
		for (Relationship relationshipToNodeToDelete : node.getRelationships(Direction.OUTGOING,
			RelationshipTypes.MAP))
		    if (relationshipToNodeToDelete.getProperty(Properties.KEY.name()).equals(key))
			actualDatabase.delete(relationshipToNodeToDelete);
	    }
	});
    }

    public void removeElementFromArray(Relationship relationshipToParent, final int index) {
	// this is a delete (on node) and update (on parent)
	actualDatabase.update(relationshipToParent, false, new Operation() {
	    @Override
	    public void updateElement(Node node) {
		for (Relationship relationshipToNodeToDelete : node.getRelationships(Direction.OUTGOING,
			RelationshipTypes.ARRAY))
		    if (relationshipToNodeToDelete.getProperty(Properties.INDEX.name()).equals(index))
			actualDatabase.delete(relationshipToNodeToDelete);
	    }
	});
    }

    public void overwriteElement(Relationship relationshipToNode, final JsonNode json) {
	actualDatabase.update(relationshipToNode, true, new Operation() {
	    @Override
	    public void updateElement(Node node) {
		populateWithJson(node, json);
	    }
	});

    }

    public void getElementsFromArray() {
    }

    public void getElementsFromMap() {
    }

    private int findNextUnusedIndex(Node parent) {
	int max = 0;
	for (Relationship a : parent.getRelationships(Direction.OUTGOING, RelationshipTypes.ARRAY)) {
	    if ((int) a.getProperty(Properties.INDEX.name()) > max)
		max = (int) a.getProperty(Properties.INDEX.name());
	}
	return max + 1;
    }

    // ADO

    @Override
    public Node createNewNode() {
	return grapgDb.createNode();
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, Operation o) {
	if (removeEverything) {
	    removeRelationships(relationshipToParent.getEndNode(), RelationshipTypes.ARRAY, RelationshipTypes.MAP);
	    removeProperties(relationshipToParent.getEndNode(), Properties.values());
	}
	o.updateElement(relationshipToParent.getEndNode());
    }

    private void removeProperties(Node node, Properties[] values) {
	for (Properties key : values) {
	    node.removeProperty(key.name());
	}

    }

    private void removeRelationships(Node node, RelationshipTypes... types) {
	for (Relationship relationship : node.getRelationships(Direction.OUTGOING, types)) {
	    relationship.delete();
	}
	// FIXME - what do we do at other end? Actually delete (and possibly
	// screw up history, or garbage collect?
    }

    @Override
    public Node read(Relationship r) {
	return r.getEndNode();
    }

    @Override
    public void delete(Relationship relationshipToNodeToDelete) {
	Node nodeAtOtherEnd = relationshipToNodeToDelete.getEndNode();
	relationshipToNodeToDelete.delete();
	nodeAtOtherEnd.delete();
    }
}

interface ADO {

    public Node createNewNode();

    void update(Relationship relationshipToParent, boolean removeEverything, Operation o);

    Node read(Relationship r);

    void delete(Relationship relationshipToNodeToDelete);

    public interface Operation {
	void updateElement(Node node);
    }
}
