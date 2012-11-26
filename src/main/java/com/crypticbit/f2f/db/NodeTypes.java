package com.crypticbit.f2f.db;

import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

enum NodeTypes {
	ARRAY() {
		@Override
		JsonNode _wrapAsJsonNode(Node graphNode) {
			return Neo4JJsonPersistenceService.createDynamicWrapperForNode(graphNode, ArrayNodeAdapter.class);
		}

	},
	MAP() {
		@Override
		JsonNode _wrapAsJsonNode(Node graphNode) {
			return Neo4JJsonPersistenceService.createDynamicWrapperForNode(graphNode, MapNodeAdapter.class);
		}
	},
	VALUE() {
		@Override
		JsonNode _wrapAsJsonNode(Node graphNode) {
			return new WrapValueNode(graphNode);
		}
	};
	abstract JsonNode _wrapAsJsonNode(Node graphNode);
	public static JsonNode wrapAsJsonNode(Node graphNode) {
		return valueOf((String)graphNode.getProperty("type"))._wrapAsJsonNode(graphNode);
	}
}