package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * The CRUD Operations a database needs to implement - and which can be
 * intercepted, to change behaviour
 * 
 * @author leo
 * 
 */
public interface FundementalDatabaseOperations {

    /**
     * Create a new node - with no content
     * 
     * @return the new node
     */
    public Node createNewNode();

    /**
     * Update a the node at the end of the relationship, by applying the
     * operation (Sort of command pattern).
     * 
     * @param relationshipToParent
     *            the relationship to the node that wants to be changed
     * @param removeEverything
     *            whether to remove all properties and relationships before the
     *            operation. The only ones to be removed are those known by the
     *            strategies implemented, so they should preserve any unknown
     *            properties or relationships
     * @param operation
     */
    public void update(Relationship relationshipToParent, boolean removeEverything, UpdateOperation operation);

    /**
     * Read this relationship, and return the node at the other end
     * 
     * @param relationshipToNode
     */

    public Node read(Relationship relationshipToNode);

    /**
     * Delete the relationship and the node at the end of it
     * 
     * @param relationshipToNodeToDelete
     *            the relationship to remove, together with the node at the end
     *            of it
     */
    public void delete(Relationship relationshipToNodeToDelete);

    /**
     * The operation (command pattern) that is to be executed to perform an
     *s update
     * 
     * @author leo
     * 
     */
    public interface UpdateOperation {
	void updateElement(SimpleFdoAdapter dal, Node node);
    }

    public void commit();

    public void rollback();
}