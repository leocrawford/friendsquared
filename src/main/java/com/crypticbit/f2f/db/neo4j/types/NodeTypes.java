package com.crypticbit.f2f.db.neo4j.types;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.ArrayGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.EmptyGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.MapGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.ValueGraphNode;
import com.crypticbit.f2f.db.neo4j.strategies.FundementalDatabaseOperations;

public enum NodeTypes {
    ARRAY() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship, FundementalDatabaseOperations fdo) {
	    return new ArrayGraphNode(graphNode, incomingRelationship, fdo);
	}

    },
    MAP() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship, FundementalDatabaseOperations fdo) {
	    return new MapGraphNode(graphNode, incomingRelationship, fdo);
	}
    },
    VALUE() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship, FundementalDatabaseOperations fdo) {
	    return new ValueGraphNode(graphNode, incomingRelationship, fdo);
	}
    };
    public static Neo4JGraphNode wrapAsGraphNode(Node graphNode, Relationship incomingRelationship, FundementalDatabaseOperations fdo) {
	if (graphNode.hasProperty(RelationshipParameters.TYPE.name()))
	    return valueOf((String) graphNode.getProperty(RelationshipParameters.TYPE.name()))._wrapAsGraphNode(graphNode,incomingRelationship, fdo);
	else
	  throw new Error("Found node with no type: "+graphNode.getId());
	// FIXME throw exceptiom
    }

    abstract Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship, FundementalDatabaseOperations fdo);
}