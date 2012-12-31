package com.crypticbit.f2f.db.neo4j.nodes;

import java.io.IOException;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import scala.actors.threadpool.Arrays;

import com.crypticbit.f2f.db.GraphNode;
import com.crypticbit.f2f.db.History;
import com.crypticbit.f2f.db.IllegalJsonException;
import com.crypticbit.f2f.db.JsonPersistenceException;
import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;
import com.crypticbit.f2f.db.neo4j.types.NodeTypes;
import com.crypticbit.f2f.db.neo4j.types.RelationshipParameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.internal.PathToken;

/**
 * This class hold GraphNodes that do not yet exist in the database.
 * 
 * @author leo
 * 
 */
public class EmptyGraphNode implements Neo4JGraphNode {

    private PotentialRelationship potentialRelationship;
    private Neo4JGraphNode node;
    private FundementalDatabaseOperations fdo;

    /**
     * Create a node that represents this graph node
     * 
     * @param incomingRelationship
     */
    public EmptyGraphNode(PotentialRelationship potentialRelationship, FundementalDatabaseOperations fdo) {
	this.potentialRelationship = potentialRelationship;
	this.fdo = fdo;
    }

    public interface PotentialRelationship {

	Relationship create();

    }

    @Override
    public List<History> getHistory() {
	return Arrays.asList(new History[] {});
    }

    @Override
    public long getTimestamp() {
	checkHaveDelegateNode();
	return node.getTimestamp();
    }

    private void checkHaveDelegateNode() {
	if (node == null)
	    throw new UnsupportedOperationException("Not possible to invoke this method on an EmptyGraphNode");
    }

    @Override
    public GraphNode navigate(String jsonPath) throws IllegalJsonException {
	checkHaveDelegateNode();
	return node.navigate(jsonPath);
    }

    @Override
    public void write(String json) throws IllegalJsonException, JsonPersistenceException {
	if (node == null) {
	    Relationship r = potentialRelationship.create();
	    try {
		GraphNodeImpl.populateWithJson(getStrategy(), r.getEndNode(), new ObjectMapper().readTree(json));
	    } catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    node = NodeTypes.wrapAsGraphNode(r.getEndNode(), r, getStrategy());
	}
	else
	    node.write(json);

    }

    private void makeRelationsipTangibleIfNotAlready(NodeTypes nodeType) {
	if (node == null) {
	    Relationship r = potentialRelationship.create();
	    node.getDatabaseNode().setProperty(RelationshipParameters.TYPE.name(), nodeType);
	    node = NodeTypes.wrapAsGraphNode(r.getEndNode(), r, getStrategy());
	}
	checkHaveDelegateNode();
    }

    @Override
    public Neo4JGraphNode put(String key) throws IllegalJsonException, JsonPersistenceException {
	makeRelationsipTangibleIfNotAlready(NodeTypes.MAP);
	return node.put(key);

    }

    @Override
    public EmptyGraphNode add() throws IllegalJsonException, JsonPersistenceException {
	makeRelationsipTangibleIfNotAlready(NodeTypes.ARRAY);
	return node.add();

    }

    @Override
    public JsonNode toJsonNode() {
	checkHaveDelegateNode();
	return node.toJsonNode();
    }

    @Override
    public String toJsonString() {
	checkHaveDelegateNode();
	return node.toJsonString();
    }

    @Override
    public Node getDatabaseNode() {
	checkHaveDelegateNode();
	return node.getDatabaseNode();
    }

    @Override
    public FundementalDatabaseOperations getStrategy() {
	return fdo;
    }

    @Override
    public Neo4JGraphNode navigate(PathToken token) throws IllegalJsonException {
	// TODO Auto-generated method stub
	return null;
    }

}