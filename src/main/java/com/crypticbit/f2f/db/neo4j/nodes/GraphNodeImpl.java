package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.crypticbit.f2f.db.GraphNode;
import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.Context;
import com.crypticbit.f2f.db.neo4j.strategies.DatabaseOperations;
import com.crypticbit.f2f.db.neo4j.strategies.StrategyChainFactory;
import com.crypticbit.f2f.db.neo4j.strategies.TimestampVersionStrategy;
import com.crypticbit.f2f.db.neo4j.strategies.UnversionedVersionStrategy;
import com.crypticbit.f2f.db.neo4j.strategies.VersionStrategy;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class GraphNodeImpl implements Neo4JGraphNode {

    private Neo4JGraphNode graphNode;
    private Relationship incomingRelationship;

    public GraphNodeImpl(Neo4JGraphNode graphNode, Relationship incomingRelationship) {
	this.graphNode = graphNode;
	this.incomingRelationship = incomingRelationship;
    }

    public Neo4JGraphNode navigate(String path) {
	return JsonPath.compile(path).read(graphNode);
    }

    public void overwrite(String json) throws IllegalJsonException, JsonPersistenceException {
	DatabaseOperations db = getStrategy();
	db.beginTransaction();
	try {
	    JsonNode values = new ObjectMapper().readTree(json);
	    getStrategy().overwriteElement(incomingRelationship, values);
	    db.successTransaction();
	} catch (JsonProcessingException jpe) {
	    db.failureTransaction();
	    throw new IllegalJsonException("The JSON string was badly formed: " + json, jpe);
	} catch (IOException e) {
	    db.failureTransaction();
	    throw new JsonPersistenceException("IOException whilst writing data to database", e);
	} finally {
	    db.finishTransaction();
	}

    }
    
    public Relationship getIncomingRelationship() {
	return incomingRelationship;
    }

    public DatabaseOperations getStrategy() {
	return new DatabaseOperations(getDatabaseService());
    }

    private GraphDatabaseService getDatabaseService() {
	return graphNode.getDatabaseNode().getGraphDatabase();
    }

    public String toJsonString() {
	return graphNode.toJsonNode().toString();
    }

    @Override
    public JsonNode toJsonNode() {
	throw new UnsupportedOperationException("toJsonNode is to be provided locally");
    }

    @Override
    public Node getDatabaseNode() {
	throw new UnsupportedOperationException("getDatabaseNode is to be provided locally");
    }

    private List<History> history = null;

    private static final String DATE_FORMAT = "H:mm:ss.SSS yy-MM-dd";
    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    @Override
    public List<History> getHistory() {
	System.out.println("Called history on " + graphNode.getDatabaseNode().getId());
	if (history == null) {
	    history = new LinkedList<History>();
	    history.add(new History() {

		@Override
		public long getTimestamp() {
		    return graphNode.getTimestamp();
		}

		public String toString() {
		    return sdf.format(new Date(getTimestamp()));
		}

		@Override
		public GraphNode getVersion() {
		    return graphNode;
		}
	    });
	    for (Relationship r : graphNode.getDatabaseNode().getRelationships(RelationshipTypes.HISTORY,
		    Direction.OUTGOING)) {

		System.out.println("Found "+r+ " between "+r.getStartNode()+","+r.getEndNode());
		
		final Neo4JGraphNode endNode = NodeTypes.wrapAsGraphNode(r.getEndNode(), r);
		history.addAll(endNode.getHistory());
	    }
	}
	return history;

    }

    @Override
    public long getTimestamp() {
	return (long) graphNode.getDatabaseNode().getProperty("timestamp");
    }

    @Override
    public void put(String key, String json) throws IllegalJsonException, JsonPersistenceException {
	throw new UnsupportedOperationException("put is to be provided locally");

    }

    @Override
    public void add(String json) throws IllegalJsonException, JsonPersistenceException {
	throw new UnsupportedOperationException("add is to be provided locally");

    }

}
