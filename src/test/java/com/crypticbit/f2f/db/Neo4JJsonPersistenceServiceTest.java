package com.crypticbit.f2f.db;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jsonpath.JsonPath;

public class Neo4JJsonPersistenceServiceTest {

	private static String jsonText = "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}";
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testGetRootJsonNode() throws IOException,
			JsonPersistenceException {
		Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(Files
				.createTempDirectory("neo4j_test").toFile());

		ps.put(null, mapper.readTree(jsonText));
		// we have to convert our return value to get rid of spaces, etc. and ensure we do type comparison
		assertEquals (mapper.readTree(ps.getRootJsonNode().toString()), mapper.readTree(jsonText));
	}

	@Test
	public void testFindFromRoot() throws IOException, JsonPersistenceException {
		Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(Files
				.createTempDirectory("neo4j_test").toFile());
		ps.put(null, mapper.readTree(jsonText));
		System.out.println(ps.getRootJsonNode());
		ps.put(JsonPath.compile("second[3]"), mapper.readTree("\"blah blah\""));
		System.out.println(ps.getRootJsonNode());
		ps.put(JsonPath.compile("second[0]"), ps.get(JsonPath.compile("second[4]")));
		System.out.println(ps.getRootJsonNode());
		System.out.println(ps.get(JsonPath.compile("second[0]")));
		System.out.println(ps.get(JsonPath.compile("$.second[0]")));
		System.out.println(ps.get(JsonPath.compile("second[4]")));
	}


}
