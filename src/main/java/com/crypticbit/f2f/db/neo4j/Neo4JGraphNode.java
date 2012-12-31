package com.crypticbit.f2f.db.neo4j;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.HistoryGraphNode;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.SimpleFdoAdapter;
import com.jayway.jsonpath.internal.PathToken;

public interface Neo4JGraphNode extends HistoryGraphNode {

    public Node getDatabaseNode();
    public FundementalDatabaseOperations getStrategy();
    public Neo4JGraphNode navigate(PathToken token) throws IllegalJsonException;
    


}
