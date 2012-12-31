package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;

public abstract class CompoundFdoAdapter implements FundementalDatabaseOperations {

    private FundementalDatabaseOperations nextAdapter;
    private GraphDatabaseService graphDb;

    public CompoundFdoAdapter(GraphDatabaseService graphDb, FundementalDatabaseOperations nextAdapter) {
	this.nextAdapter = nextAdapter;
	this.graphDb = graphDb;
    }

    protected GraphDatabaseService getGraphDB() {
	return graphDb;
    }

    @Override
    public Node createNewNode() {
	return nextAdapter.createNewNode();
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation operation) {
	nextAdapter.update(relationshipToParent, removeEverything, operation);

    }

    @Override
    public Node read(Relationship relationshipToNode) {
	return nextAdapter.read(relationshipToNode);
    }

    @Override
    public void delete(Relationship relationshipToNodeToDelete) {
	nextAdapter.delete(relationshipToNodeToDelete);

    }

    @Override
    public void commit() {
	nextAdapter.commit();

    }

    @Override
    public void rollback() {
	nextAdapter.rollback();

    }

}
