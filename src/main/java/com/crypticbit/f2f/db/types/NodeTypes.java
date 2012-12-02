package com.crypticbit.f2f.db.types;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.wrappers.ArrayGraphNode;
import com.crypticbit.f2f.db.wrappers.GraphNode;
import com.crypticbit.f2f.db.wrappers.MapGraphNode;
import com.crypticbit.f2f.db.wrappers.ValueGraphNode;

public enum NodeTypes {
    ARRAY() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new ArrayGraphNode(graphNode);
	}

    },
    MAP() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new MapGraphNode(graphNode);
	}
    },
    VALUE() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new ValueGraphNode(graphNode);
	}
    };
    public static GraphNode wrapAsGraphNode(Node graphNode) {
	if (graphNode.hasProperty("type"))
	    return valueOf((String) graphNode.getProperty("type"))._wrapAsGraphNode(graphNode);
	else
	    // Let's do our best to make it a value node - especially for the
	    // default root element
	    return VALUE._wrapAsGraphNode(graphNode);
    }

    abstract GraphNode _wrapAsGraphNode(Node graphNode);
}