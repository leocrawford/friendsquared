package com.crypticbit.f2f.db.strategies;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.Context;
import com.fasterxml.jackson.databind.JsonNode;

public interface VersionStrategy {

    void replaceNode(Context context, Node graphNode, JsonNode values);

}
