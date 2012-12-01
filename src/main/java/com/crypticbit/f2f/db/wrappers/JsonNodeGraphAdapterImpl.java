package com.crypticbit.f2f.db.wrappers;

import org.neo4j.graphdb.Node;

/**
 * Interface that is implemented by Map and Array nodes that extend their
 * Jackson parents, and which provides methods to lazily load children, and
 * return the original datbase node
 */
public class  JsonNodeGraphAdapterImpl implements OutwardFacingJsonNodeGraphAdapter{

    private Node node;
    public JsonNodeGraphAdapterImpl(Node node) {
	this.node = node;
    }

    /** Return the underlying database node */
    public Node getDatabaseNode() {
	return node;
    }


}