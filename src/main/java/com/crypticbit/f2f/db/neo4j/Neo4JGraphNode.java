package com.crypticbit.f2f.db.neo4j;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.HistoryGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.Neo4JSimpleFdoAdapter;

public interface Neo4JGraphNode extends HistoryGraphNode {

    public Node getDatabaseNode();
    FundementalDatabaseOperations getStrategy();
    


}
