package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class Context {

    private Transaction tx;
    private GraphDatabaseService graphDb;

    public Context(Transaction tx, GraphDatabaseService graphDb) {
	this.tx = tx;
	this.graphDb = graphDb;
    }

    public GraphDatabaseService getGraphDb() {
	return graphDb;
    }

    public Transaction getTransaction() {
	return tx;
    }

}
