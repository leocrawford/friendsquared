package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface FundementalDatabaseOperations {

    public Node createNewNode();

    void update(Relationship relationshipToParent, boolean removeEverything, Operation o);

    Node read(Relationship r);

    void delete(Relationship relationshipToNodeToDelete);

    public interface Operation {
	void updateElement(DatabaseAbstractionLayer dal, Node node);
    }
}