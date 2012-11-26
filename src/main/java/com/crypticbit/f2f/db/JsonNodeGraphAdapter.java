package com.crypticbit.f2f.db;

import org.neo4j.graphdb.Node;

/**
 * Interface that is implemented by Map and Array nodes that extend their
 * Jackson parents, and which provides methods to lazily load children, and
 * return the original datbase node
 */
public interface JsonNodeGraphAdapter {

	/**
	 * Navigate all the relationships in the database node, and populate
	 * _children with lazily loaded substitutes
	 */
	public void updateNodes();

	/** Return the underlying database node */
	public Node getDatabaseNode();
}