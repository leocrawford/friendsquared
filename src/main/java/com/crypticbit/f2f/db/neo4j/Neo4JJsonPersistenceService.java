package com.crypticbit.f2f.db.neo4j;

import java.io.File;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.server.WrappingNeoServerBootstrapper;

import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.JsonPersistenceService;
import com.crypticbit.f2f.db.neo4j.strategies.VersionStrategy;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides persistence for Json objects (depicted using Jackson JsonNode) with
 * lookup functions using Jackson-JsonPaths.
 * 
 * @author leo
 * 
 */
public class Neo4JJsonPersistenceService implements JsonPersistenceService, Neo4JGraphNode {

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
     * Get the root of the tree - which could be pretty big, therefore
     * everything is lazily loaded
     */
    private Neo4JGraphNode getRootGraphNode() {
	// this extra step (root of the root) is so we can readily change it
	// later, and normal logic keeps working
	return (Neo4JGraphNode) getReferenceGraphNode().navigate("root");
    }

    /**
     * This is the real root of the tree, but by exposing a node of this as the
     * conceptual root, we can do operations that require a parent without any
     * special code
     */
    private Neo4JGraphNode getReferenceGraphNode() {
	return NodeTypes.wrapAsGraphNode(getDatabaseNode(), null);
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
	Transaction tx = graphDb.beginTx();
	try {
	    referenceNode.setProperty("type", NodeTypes.MAP.toString());
	    tx.success();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    tx.finish();
	}

    }

    // only for server
    private WrappingNeoServerBootstrapper srv;

    public void startWebService() {
	srv = new WrappingNeoServerBootstrapper((AbstractGraphDatabase) graphDb);
	srv.start();
	System.out.println("Started server: "+srv.getServer().toString());
    }

    public void stopWebService() {
	srv.stop();
    }

    @Override
    public Neo4JGraphNode navigate(String jsonPath) {
	return (Neo4JGraphNode) getRootGraphNode().navigate(jsonPath);
    }

    @Override
    public void overwrite(String json) throws IllegalJsonException, JsonPersistenceException {
	getReferenceGraphNode().put("root", json);

    }

    @Override
    public JsonNode toJsonNode() {
	return getRootGraphNode().toJsonNode();
    }

    @Override
    public String toJsonString() {
	return getRootGraphNode().toJsonString();
    }

    @Override
    public List<History> getHistory() {
	return getRootGraphNode().getHistory();
    }

    @Override
    public long getTimestamp() {
	return getRootGraphNode().getTimestamp();
    }

    @Override
    public void put(String key, String json) throws IllegalJsonException, JsonPersistenceException {
	getRootGraphNode().put(key, json);

    }

    @Override
    public void add(String json) throws IllegalJsonException, JsonPersistenceException {
	getRootGraphNode().add(json);

    }

    @Override
    public VersionStrategy getStrategy() {
	return getRootGraphNode().getStrategy();
    }

    public void close() {
	graphDb.shutdown();
    }

}
