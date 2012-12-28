package com.crypticbit.f2f.db.neo4j.types;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.ArrayGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.MapGraphNode;
import com.crypticbit.f2f.db.neo4j.nodes.ValueGraphNode;

public enum NodeTypes {
    ARRAY() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship) {
	    return new ArrayGraphNode(graphNode, incomingRelationship);
	}

    },
    MAP() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship) {
	    return new MapGraphNode(graphNode, incomingRelationship);
	}
    },
    VALUE() {
	@Override
	Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship) {
	    return new ValueGraphNode(graphNode, incomingRelationship);
	}
    };
    public static Neo4JGraphNode wrapAsGraphNode(Node graphNode, Relationship incomingRelationship) {
	if (graphNode.hasProperty(RelationshipParameters.TYPE.name()))
	    return valueOf((String) graphNode.getProperty(RelationshipParameters.TYPE.name()))._wrapAsGraphNode(graphNode,incomingRelationship);
	else
	    // Let's do our best to make it a value node - especially for the
	    // default root element
	    return VALUE._wrapAsGraphNode(graphNode, incomingRelationship);
    }

    abstract Neo4JGraphNode _wrapAsGraphNode(Node graphNode, Relationship incomingRelationship);
}