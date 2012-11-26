package com.crypticbit.f2f.db;

import java.io.IOException;

import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ValueNode;

public class WrapValueNode extends ValueNode implements JsonNodeGraphAdapter {

	private JsonNode delegate;
	private Node graphNode;

	public WrapValueNode(Node graphNode) {
		this.graphNode = graphNode;
		try {
			// FIXME factor out object mapper
			this.delegate = new ObjectMapper().readTree((String) graphNode
					.getProperty("value"));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public JsonToken asToken() {
		return delegate.asToken();
	}

	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		((ValueNode) delegate).serialize(jgen, provider);

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

	@Override
	public String toString() {
		return delegate.toString();
	}
	
}
