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
    private Transaction tx;

    public SimpleFdoAdapter(GraphDatabaseService graphDb) {
	this.graphDb = graphDb;
	System.out.println("Starting transaction");
	tx = graphDb.beginTx();
    }

    public void commit() {
	tx.success();
	tx.finish();
	tx = null;
	System.out.println("Closed transaction");
    }

    public void rollback() {
	tx.failure();
	tx.finish();
	tx = null;
    }

    protected void finalize() {
	assert tx == null;
    }

    @Override
    public Node createNewNode() {
	checkWithinTransaction();
	return graphDb.createNode();
    }

    private void checkWithinTransaction() {
	if (tx == null)
	    throw new Error("Tried to do operation outside of a transaction");

    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation o) {
	checkWithinTransaction();
	if (removeEverything) {
	    removeRelationships(relationshipToParent.getEndNode(), RelationshipTypes.ARRAY, RelationshipTypes.MAP);
	    removeProperties(relationshipToParent.getEndNode(), RelationshipParameters.values());
	}
	o.updateElement(relationshipToParent.getEndNode());
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
	checkWithinTransaction();
	Node nodeAtOtherEnd = relationshipToNodeToDelete.getEndNode();
	relationshipToNodeToDelete.delete();
	nodeAtOtherEnd.delete();
    }
}
