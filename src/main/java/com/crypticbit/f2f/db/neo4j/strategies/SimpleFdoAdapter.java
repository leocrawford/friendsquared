package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;

public class SimpleFdoAdapter implements FundementalDatabaseOperations {

    private GraphDatabaseService graphDb;
    private FundementalDatabaseOperations fdo;

    public SimpleFdoAdapter(GraphDatabaseService graphDb) {
	this.graphDb = graphDb;
    }

    @Override
    public Node createNewNode(UpdateOperation createOperation) {
	Transaction tx = graphDb.beginTx();
	try {
	    Node node = graphDb.createNode();
	    createOperation.updateElement(node, fdo);
	    tx.success();
	    return node;
	} finally {
	    tx.finish();
	}

    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation o) {
	Transaction tx = graphDb.beginTx();
	try {
	    if (removeEverything) {
		removeRelationships(relationshipToParent.getEndNode(), RelationshipTypes.ARRAY, RelationshipTypes.MAP);
		removeProperties(relationshipToParent.getEndNode(), RelationshipParameters.values());
	    }
	    o.updateElement(relationshipToParent.getEndNode(), fdo);
	    tx.success();
	} finally {
	    tx.finish();
	}
    }

    private void removeProperties(Node node, RelationshipParameters[] values) {
	for (RelationshipParameters key : values) {
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
	Transaction tx = graphDb.beginTx();
	try {
	    Node nodeAtOtherEnd = relationshipToNodeToDelete.getEndNode();
	    relationshipToNodeToDelete.delete();
	    nodeAtOtherEnd.delete();
	    tx.success();
	} finally {
	    tx.finish();
	}
    }

    @Override
    public void setTopFdo(FundementalDatabaseOperations fdo) {
	this.fdo = fdo;

    }
}
