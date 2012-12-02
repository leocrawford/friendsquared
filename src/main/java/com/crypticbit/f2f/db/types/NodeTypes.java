package com.crypticbit.f2f.db.types;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.wrappers.ArrayNodeAdapter;
import com.crypticbit.f2f.db.wrappers.MapNodeAdapter;
import com.crypticbit.f2f.db.wrappers.GraphNode;
import com.crypticbit.f2f.db.wrappers.WrapValueNodeAdapter;

public enum NodeTypes {
    ARRAY() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new ArrayNodeAdapter(graphNode);
	}

    },
    MAP() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new MapNodeAdapter(graphNode);
	}
    },
    VALUE() {
	@Override
	GraphNode _wrapAsGraphNode(Node graphNode) {
	    return new WrapValueNodeAdapter(graphNode);
	}
    };
    public static GraphNode wrapAsGraphNode(Node graphNode) {
	if (graphNode.hasProperty("type"))
	    return valueOf((String) graphNode.getProperty("type"))
		    ._wrapAsGraphNode(graphNode);
	else
	    // Let's do our best to make it a value node - especially for the default root element
	    return VALUE._wrapAsGraphNode(graphNode);
    }

    abstract GraphNode _wrapAsGraphNode(Node graphNode);
}