package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;

public class DatabaseAbstractionLayer implements FundementalDatabaseOperations {

    private GraphDatabaseService grapgDb;
    private Transaction tx;

    public DatabaseAbstractionLayer(GraphDatabaseService grapgDb) {
	this.grapgDb = grapgDb;
    }

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

    @Override
    public Node createNewNode() {
	return grapgDb.createNode();
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, Operation o) {
	if (removeEverything) {
	    removeRelationships(relationshipToParent.getEndNode(), RelationshipTypes.ARRAY, RelationshipTypes.MAP);
	    removeProperties(relationshipToParent.getEndNode(), RelationshipParameters.values());
	}
	o.updateElement(this, relationshipToParent.getEndNode());
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
	Node nodeAtOtherEnd = relationshipToNodeToDelete.getEndNode();
	relationshipToNodeToDelete.delete();
	nodeAtOtherEnd.delete();
    }
}
