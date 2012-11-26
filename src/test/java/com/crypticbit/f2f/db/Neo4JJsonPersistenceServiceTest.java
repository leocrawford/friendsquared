package com.crypticbit.f2f.db;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jsonpath.JsonPath;

public class Neo4JJsonPersistenceServiceTest {

	private String jsonText = "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"id1\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"id\": null}";

	@Test
	public void testGetRootJsonNode() throws IOException,
			JsonPersistenceException {
		Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(Files
				.createTempDirectory("neo4j_test").toFile());
		ObjectMapper mapper = new ObjectMapper();
		ps.put(null, mapper.readTree(jsonText));
		System.out.println(ps.getRootJsonNode());
		ps.put(JsonPath.compile("second[3]"), mapper.readTree("\"blah blah\""));
		System.out.println(ps.getRootJsonNode());
		ps.put(JsonPath.compile("second[0]"), mapper.readTree(jsonText));
		System.out.println(ps.getRootJsonNode());
		System.out.println(ps.get(JsonPath.compile("second[0]")));
		System.out.println(ps.get(JsonPath.compile("$.second[0]")));
		System.out.println(ps.get(JsonPath.compile("second[4]")));
		System.out.println(ps.get(JsonPath.compile("second[*]")));
	}

	@Test
	public void testFindFromRoot() {

	}

	@Test
	public void testToJsonUsingAdapter() {
	}

}
