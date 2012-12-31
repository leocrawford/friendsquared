package com.crypticbit.f2f.db.neo4j;

import java.io.File;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.server.WrappingNeoServerBootstrapper;

import com.crypticbit.f2f.db.JsonPersistenceService;
import com.crypticbit.f2f.db.neo4j.nodes.EmptyGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.EmptyGraphNode.PotentialRelationship;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations.NullUpdateOperation;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations.UpdateOperation;
import com.crypticbit.f2f.db.neo4j.strategies.SimpleFdoAdapter;
import com.crypticbit.f2f.db.neo4j.strategies.TimeStampedHistoryAdapter;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;

/**
 * Provides persistence for Json objects (depicted using Jackson JsonNode) with
 * lookup functions using Jackson-JsonPaths.
 * 
 * @author leo
 * 
 */
public class Neo4JJsonPersistenceService implements JsonPersistenceService {

    private static final String ROOT = "ROOT";

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

    // only for server
    private WrappingNeoServerBootstrapper srv;

    public void startWebService() {
	srv = new WrappingNeoServerBootstrapper((AbstractGraphDatabase) graphDb);
	srv.start();
    }

    public void stopWebService() {
	srv.stop();
    }

    public synchronized void startWebServiceAndWait() {
	startWebService();
	try {
	    this.wait();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public Neo4JGraphNode getRootNode() {
	final FundementalDatabaseOperations fdo = createDatabase();
	if (getDatabaseNode().hasRelationship(RelationshipTypes.MAP, Direction.OUTGOING)) {
	    Relationship r = getDatabaseNode().getRelationships(RelationshipTypes.MAP, Direction.OUTGOING).iterator()
		    .next();
	    return NodeTypes.wrapAsGraphNode(r.getEndNode(), r,fdo);
	} else {
	    
	    return new EmptyGraphNode(new PotentialRelationship() {
		@Override
		public Relationship create(UpdateOperation createOperation) {
		    Node newNode = fdo.createNewNode(createOperation);
		    return getDatabaseNode().createRelationshipTo(newNode, RelationshipTypes.MAP);
		}
	    },fdo);
	}
    }

    private FundementalDatabaseOperations createDatabase() {
	    TimeStampedHistoryAdapter fdo = new TimeStampedHistoryAdapter(graphDb, new SimpleFdoAdapter(graphDb));
	    fdo.setTopFdo(fdo);
	    return fdo;

    }

    public void close() {
	graphDb.shutdown();
    }

}
