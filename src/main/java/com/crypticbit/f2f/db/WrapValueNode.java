package com.crypticbit.f2f.db;

import java.io.IOException;

import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class WrapValueNode extends ValueNode implements JsonNodeGraphAdapter{

	private ValueNode delegate;
	private Node graphNode;

	public WrapValueNode(Node graphNode) {
		this.graphNode = graphNode;
		this.delegate = new TextNode((String) graphNode.getProperty("value"));
	}

	
	@Override
	public JsonToken asToken() {
		return delegate.asToken();
	}

	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		delegate.serialize(jgen, provider);

	}

	@Override
	public String asText() {
		return delegate.asText();
	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public void updateNodes() {
		// do nothing
		
	}

	@Override
	public Node getDatabaseNode() {
		return graphNode;
	}

}
