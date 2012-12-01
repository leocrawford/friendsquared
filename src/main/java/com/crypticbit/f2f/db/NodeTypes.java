package com.crypticbit.f2f.db;

import org.neo4j.graphdb.Node;

import com.crypticbit.f2f.db.wrappers.ArrayNodeAdapter;
import com.crypticbit.f2f.db.wrappers.MapNodeAdapter;
import com.crypticbit.f2f.db.wrappers.NodeAdapterWrapperFactory;
import com.crypticbit.f2f.db.wrappers.WrapValueNodeAdapter;
import com.fasterxml.jackson.databind.JsonNode;

public enum NodeTypes {
    ARRAY() {
	@Override
	JsonNode _wrapAsJsonNode(Node graphNode) {
	    return NodeAdapterWrapperFactory.createDynamicWrapperForNode(
		    graphNode, ArrayNodeAdapter.class);
	}

    },
    MAP() {
	@Override
	JsonNode _wrapAsJsonNode(Node graphNode) {
	    return NodeAdapterWrapperFactory.createDynamicWrapperForNode(
		    graphNode, MapNodeAdapter.class);
	}
    },
    VALUE() {
	@Override
	JsonNode _wrapAsJsonNode(Node graphNode) {
	    return NodeAdapterWrapperFactory.createDynamicWrapperForNode(
		    graphNode, WrapValueNodeAdapter.class);
	}
    };
    public static JsonNode wrapAsJsonNode(Node graphNode) {
	return valueOf((String) graphNode.getProperty("type"))._wrapAsJsonNode(
		graphNode);
    }

    abstract JsonNode _wrapAsJsonNode(Node graphNode);
}