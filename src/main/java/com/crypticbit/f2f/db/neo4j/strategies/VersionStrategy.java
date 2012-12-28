package com.crypticbit.f2f.db.neo4j.strategies;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.types.RelationshipTypes;
import com.fasterxml.jackson.databind.JsonNode;

public interface VersionStrategy {

    public void addElementToMap(Context context, Node parent, JsonNode json, String key) ;
    public void addElementToArray(Context context, Node parent, JsonNode json);
    public void addElementToArray(Context context, Node parent, JsonNode json, int index);
    
    
    
    public Node createNewNode(Context context, JsonNode jsonNode);

    
    
    
    public Relationship replaceNode(Context context, Relationship oldRelationship, JsonNode values);
    public void replaceRelationship(Relationship oldRelationship, Relationship newRelationship);
}
