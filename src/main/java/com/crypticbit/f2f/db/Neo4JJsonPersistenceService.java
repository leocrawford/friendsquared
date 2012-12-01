package com.crypticbit.f2f.db;

import java.io.File;
import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.crypticbit.f2f.db.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.strategies.VersionStrategy;
import com.crypticbit.f2f.db.wrappers.JsonNodeGraphAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jsonpath.JsonPath;

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

    /** Return the subset of the tree that is defined by this path */
    @Override
    public JsonNode get(JsonPath path) {
	return path.read(getRootJsonNode());
    }

    /**
     * Get the root of the tree - which could be pretty big, but lucily
     * everything is lazily loaded
     */
    public JsonNode getRootJsonNode() {
	return toJsonUsingAdapter(getRootGraphNode());
    }

    /**
     * Put the values at the location depicted by the path. Path must be to an
     * existing and valid node (which will be overwritten). To add to an
     * existing node use add.
     * 
     * @see add
     */
    public void put(JsonPath path, JsonNode values, VersionStrategy strategy,
	    Context context) throws JsonPersistenceException {
	if (path == null) {
	    strategy.replaceNode(context, getRootGraphNode(), values);
	} else {
	    if (!path.isPathDefinite())
		throw new JsonPersistenceException(
			"The path \"+path+\" is ambiguous.");
	    JsonNode node = get(path);
	    if (node instanceof JsonNodeGraphAdapter) {
		Node graphNode = ((JsonNodeGraphAdapter) node)
			.getDatabaseNode();

		strategy.replaceNode(context, graphNode, values);
	    } else
		throw new JsonPersistenceException(
			"Found a node that isn't backed by the db");
	}
    }

    /** Get the root of the graph */
    private Node getRootGraphNode() {
	return referenceNode;
    }

    /** Do everything that's needed to actually create the database */
    private void setup() {
	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(file
		.getAbsolutePath());
	registerShutdownHook(graphDb);
	referenceNode = graphDb.getReferenceNode();

    }

    private JsonNode toJsonUsingAdapter(Node graphNode) {
	return NodeTypes.wrapAsJsonNode(graphNode);
    }

    public void put(JsonPath path, JsonNode readTree) {
	Transaction tx = graphDb.beginTx();
	try {
	    put(path,
		    readTree,
		    new StrategyChainFactory()
			    .createVersionStrategies(UnversionedVersionStrategy.class),
		    new Context(tx, graphDb));
	    tx.success();
	} catch (JsonPersistenceException e) {
	    tx.failure();
	    e.printStackTrace();
	} finally {
	    tx.finish();
	}
    }

}
