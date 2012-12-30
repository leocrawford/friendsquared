package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;

public class Neo4JSimpleFdoAdapter implements FundementalDatabaseOperations {

    private GraphDatabaseService graphDb;
    private Transaction tx;

    public Neo4JSimpleFdoAdapter(GraphDatabaseService graphDb) {
	this.graphDb = graphDb;
	tx = graphDb.beginTx();
    }

    public void commit() {
	tx.success();
	tx.finish();
	tx = null;
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
	return graphDb.createNode();
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation o) {
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
