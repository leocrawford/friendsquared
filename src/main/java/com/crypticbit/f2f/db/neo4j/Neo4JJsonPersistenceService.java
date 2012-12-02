package com.crypticbit.f2f.db.neo4j;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.JsonPersistenceService;
import com.crypticbit.f2f.db.neo4j.nodes.GraphNode;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides persistence for Json objects (depicted using Jackson JsonNode) with
 * lookup functions using Jackson-JsonPaths.
 * 
 * @author leo
 * 
 */
public class Neo4JJsonPersistenceService implements JsonPersistenceService {

    /**
     * Registers a shutdown hook for the Neo4j instance so that it shuts down
     * nicely when the VM exits (even if you "Ctrl-C" the running example before
     * it's completed)
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		graphDb.shutdown();
	    }
	});
    }

    /**
     * The location of the database (this is actually a directory)
     */
    private File file;
    private transient GraphDatabaseService graphDb;
    private transient Node referenceNode;

    /** Use (or create if not present) the neo4j database at this location */
    public Neo4JJsonPersistenceService(File file) {
	this.file = file;
	setup();
    }

    /**
     * Delete the loaded database, and recreate an empty one at the same
     * location
     * 
     * @param iAgreeThisisVeryDangerous
     *            if you don't agree - it won't delete
     */
    public void empty(boolean iAgreeThisisVeryDangerous) {
	if (iAgreeThisisVeryDangerous) {
	    if (graphDb != null) {
		graphDb.shutdown();
	    }
	    file.delete();
	    setup();
	}
    }

    /**
     * Get the root of the tree - which could be pretty big, but lucily
     * everything is lazily loaded
     */
    private GraphNode getRootGraphNode() {
	return NodeTypes.wrapAsGraphNode(getDatabaseNode());
    }

    /** Get the root of the graph */
    public Node getDatabaseNode() {
	return referenceNode;
    }

    /** Do everything that's needed to actually create the database */
    private void setup() {
	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(file.getAbsolutePath());
	registerShutdownHook(graphDb);
	referenceNode = graphDb.getReferenceNode();

    }

    @Override
    public GraphNode navigate(String jsonPath) {
return getRootGraphNode().navigate(jsonPath);
    }

    @Override
    public void put(String json) throws IllegalJsonException, JsonPersistenceException {
	getRootGraphNode().put(json);
	
    }

    @Override
    public JsonNode toJsonNode() {
	return getRootGraphNode().toJsonNode();
    }

    @Override
    public String toJsonString() {
	return getRootGraphNode().toJsonString();
    }

 

}
