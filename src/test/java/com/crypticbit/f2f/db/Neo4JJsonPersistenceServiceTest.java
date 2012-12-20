package com.crypticbit.f2f.db;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import org.junit.Test;

import com.crypticbit.f2f.db.neo4j.Neo4JJsonPersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Neo4JJsonPersistenceServiceTest {

    private static String jsonText = "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}";
    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBasicWriteFromRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	assertEquals(mapper.readTree(jsonText), mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testOverwriteOfNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.navigate("second").overwrite("\"new value\"");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));

	ps.navigate("second").overwrite("[0,1,2]");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": [0,1,2], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testPutOfNewNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.put("new", "\"new value\"");

	assertEquals(
		mapper.readTree("{\"new\":\"new value\",\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testAddOfNewNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.navigate("second").add("\"new value\"");

	assertEquals(
		mapper.readTree("{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123},\"new value\"], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testPutOfExistingNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.put("second", "\"new value\"");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testOverwriteOfNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.navigate("second[0]").overwrite("\"new value 1\"");

	assertEquals(
		mapper.readTree("{\"first\": 123, \"second\": [\"new value 1\", 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void testNavigate() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);

	assertEquals(mapper.readTree("{\"k1\":{\"id\":\"sd1 p\"}}"),
		mapper.readTree(ps.navigate("second[0]").toJsonString()));

	assertEquals(mapper.readTree("{\"id\":\"sd1 p\"}"), mapper.readTree(ps.navigate("second[0].k1").toJsonString()));

    }

    @Test
    public void testOverwriteRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	ps.overwrite("\"new value 1\"");

	assertEquals(mapper.readTree("\"new value 1\""), mapper.readTree(ps.toJsonString()));
    }

    @Test
    public void getHistorySimpleReplacement() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite("\"new value 1\"");
	ps.overwrite("\"new value 2\"");
	ps.overwrite("\"new value 3\"");

	assertEquals(3, ps.getHistory().size());

	ps.overwrite("\"new value 4\"");

	assertEquals(4, ps.getHistory().size());

	assertTrue("Check most recent history comes first", ps.getHistory().get(0).getTimestamp() > ps.getHistory()
		.get(3).getTimestamp());

	assertEquals(mapper.readTree("\"new value 2\""),
		mapper.readTree(ps.getHistory().get(2).getVersion().toJsonString()));
    }
    
    @Test
    public void getHistoryOfAdd() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.overwrite(jsonText);
	
	ps.navigate("second").add("\"new value 1\"");

	assertEquals(2, ps.navigate("second").getHistory().size());
	
	ps.navigate("second").add("\"new value 2\"");
	
	assertEquals(3, ps.navigate("second").getHistory().size());
    }

    @Test
    public void testNavigateWithWildcards() throws IOException, JsonPersistenceException, IllegalJsonException {
	// these are currently not supported

    }

    private Neo4JJsonPersistenceService createNewService() throws IOException {
	return new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test").toFile());
    }

}
