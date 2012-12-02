package com.crypticbit.f2f.db;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.wrappers.ArrayNodeAdapter;
import com.crypticbit.f2f.db.wrappers.MapNodeAdapter;
import com.crypticbit.f2f.db.wrappers.MyGraphNode;
import com.crypticbit.f2f.db.wrappers.WrapValueNodeAdapter;

public enum NodeTypes {
    ARRAY() {
	@Override
	MyGraphNode _wrapAsJsonNode(Node graphNode) {
	    return new ArrayNodeAdapter(graphNode);
	}

    },
    MAP() {
	@Override
	MyGraphNode _wrapAsJsonNode(Node graphNode) {
	    return new MapNodeAdapter(graphNode);
	}
    },
    VALUE() {
	@Override
	MyGraphNode _wrapAsJsonNode(Node graphNode) {
	    return new WrapValueNodeAdapter(graphNode);
	}
    };
    public static MyGraphNode wrapAsJsonNode(Node graphNode) {
	if (graphNode.hasProperty("type"))
	    return valueOf((String) graphNode.getProperty("type"))
		    ._wrapAsJsonNode(graphNode);
	else
	    return VALUE._wrapAsJsonNode(graphNode);
    }

    abstract MyGraphNode _wrapAsJsonNode(Node graphNode);
}