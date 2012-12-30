package com.crypticbit.f2f.db.neo4j.strategies;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class CompoundFdoAdapter implements FundementalDatabaseOperations {

    private List<FundementalDatabaseOperations> adapters = new ArrayList<FundementalDatabaseOperations>();

    public CompoundFdoAdapter(GraphDatabaseService graphDb) {
	adapters.add(new Neo4JSimpleFdoAdapter(graphDb));
    }

    @Override
    public Node createNewNode() {
	Node result = null;
	for (FundementalDatabaseOperations adapter : adapters)
	    result = adapter.createNewNode();
	return result;
    }

    @Override
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation operation) {
	for (FundementalDatabaseOperations adapter : adapters)
	    adapter.update(relationshipToParent, removeEverything, operation);

    }

    @Override
    public Node read(Relationship relationshipToNode) {
	Node result = null;
	for (FundementalDatabaseOperations adapter : adapters)
	    result = adapter.read(relationshipToNode);
	return result;
    }

    @Override
    public void delete(Relationship relationshipToNodeToDelete) {
	for (FundementalDatabaseOperations adapter : adapters) {
	    adapter.delete(relationshipToNodeToDelete);
	}

    }

    @Override
    public void commit() {
	for (FundementalDatabaseOperations adapter : adapters) {
	    adapter.commit();
	}
	
    }

    @Override
    public void rollback() {
	for (FundementalDatabaseOperations adapter : adapters) {
	    adapter.rollback();
	}
	
    }

}
