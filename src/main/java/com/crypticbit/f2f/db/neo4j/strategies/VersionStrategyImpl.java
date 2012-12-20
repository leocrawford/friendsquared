package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class VersionStrategyImpl implements VersionStrategy {

    private VersionStrategyImpl successor;
    private VersionStrategyImpl root;


    public VersionStrategyImpl(VersionStrategyImpl successor) {
	this.successor = successor;
    }

    public VersionStrategyImpl getSuccessor() {
	return successor;
    }


    public VersionStrategyImpl getRoot() {
	return root;
    }

    public void setRoot(VersionStrategyImpl root) {
	this.root = root;
    }

    // delegates
    
    public void addElementToMap(Context context, Node parent, JsonNode json, String key) {
	getSuccessor().addElementToMap(context, parent, json, key);
    }
    public void addElementToArray(Context context, Node parent, JsonNode json){
	getSuccessor().addElementToArray(context, parent, json);
    }
    public void addElementToArray(Context context, Node parent, JsonNode json, int index){
	getSuccessor().addElementToArray(context, parent, json, index);
    }
    public Node createNewNode(Context context, JsonNode jsonNode){
	return getSuccessor().createNewNode(context, jsonNode);
    }
    public Relationship replaceNode(Context context, Relationship oldRelationship, JsonNode values){
	return getSuccessor().replaceNode(context, oldRelationship, values);
    }
    public void replaceRelationship(Relationship oldRelationship, Relationship newRelationship){
	getSuccessor().replaceRelationship(oldRelationship, newRelationship);
    }

    public Node creatCopyOfNode(Context context, Node parent) {
	return getSuccessor().creatCopyOfNode(context, parent);
    }

    
}