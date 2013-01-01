package com.crypticbit.f2f.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import com.crypticbit.f2f.db.neo4j.Neo4JGraphNode;
import com.crypticbit.f2f.db.neo4j.Neo4JJsonPersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Neo4JJsonPersistenceServiceTest {

    private static String jsonText = "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}";
    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBasicWriteFromRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	assertEquals(mapper.readTree(jsonText), mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testOverwriteOfNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().navigate("second").write("\"new value\"");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));

	ps.getRootNode().navigate("second").write("[0,1,2]");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": [0,1,2], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testPutOfNewNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);

	ps.getRootNode().put("new").write("\"new value\"");

	assertEquals(
		mapper.readTree("{\"new\":\"new value\",\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testAddOfNewNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().navigate("second").add().write("\"new value\"");

	assertEquals(
		mapper.readTree("{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123},\"new value\"], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testPutOfExistingNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().put("second").write("\"new value\"");

	assertEquals(mapper.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testOverwriteOfNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().navigate("second[0]").write("\"new value 1\"");

	assertEquals(
		mapper.readTree("{\"first\": 123, \"second\": [\"new value 1\", 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
		mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void testNavigate() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);

	assertEquals(mapper.readTree("{\"k1\":{\"id\":\"sd1 p\"}}"),
		mapper.readTree(ps.getRootNode().navigate("second[0]").toJsonString()));

	assertEquals(mapper.readTree("{\"id\":\"sd1 p\"}"),
		mapper.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));

    }

    @Test
    public void testNavigateToNewNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().navigate("second[0].k1.newNode").write("\"very new stuff\"");

	assertEquals(mapper.readTree("{\"newNode\":\"very new stuff\",\"id\":\"sd1 p\"}"),
		mapper.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));

	ps.getRootNode().navigate("second[0].k1.a1.a2").write("\"even newer stuff\"");
	assertEquals(mapper.readTree("{\"id\":\"sd1 p\", \"newNode\":\"very new stuff\", \"a1\":{\"a2\":\"even newer stuff\"}}"),
		mapper.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));

	ps.getRootNode().navigate("second[0].k1.a1.a3[0].b.c").write("\"at end of newly created chain\"");
	assertEquals(mapper.readTree("{\"a3\":[{\"b\":{\"c\":\"at end of newly created chain\"}}], \"a2\":\"even newer stuff\"}"),
		mapper.readTree(ps.getRootNode().navigate("second[0].k1.a1").toJsonString()));

    }

    @Test
    public void testOverwriteRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write(jsonText);
	ps.getRootNode().write("\"new value 1\"");

	assertEquals(mapper.readTree("\"new value 1\""), mapper.readTree(ps.getRootNode().toJsonString()));
    }

    @Test
    public void getHistorySimpleReplacement() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	ps.getRootNode().write("\"new value 1\"");
	ps.getRootNode().write("\"new value 2\"");
	ps.getRootNode().write("\"new value 3\"");

	assertEquals(3, ps.getRootNode().getHistory().size());

	ps.getRootNode().write("\"new value 4\"");

	assertEquals(4, ps.getRootNode().getHistory().size());

	assertTrue("Check most recent history comes first", ps.getRootNode().getHistory().get(0).getTimestamp() > ps
		.getRootNode().getHistory().get(3).getTimestamp());

	assertEquals(mapper.readTree("\"new value 2\""),
		mapper.readTree(ps.getRootNode().getHistory().get(2).getVersion().toJsonString()));
    }

    @Test
    public void getHistoryOfAdd() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	Neo4JGraphNode rootNode = ps.getRootNode();
	rootNode.write(jsonText);
	rootNode.navigate("second").add().write("\"new value 1\"");

	assertEquals(2, ps.getRootNode().navigate("second").getHistory().size());

	ps.getRootNode().navigate("second").add().write("\"new value 2\"");

	assertEquals(3, ps.getRootNode().navigate("second").getHistory().size());
    }

    @Test
    public void testNavigateWithWildcards() throws IOException, JsonPersistenceException, IllegalJsonException {
	// these are currently not supported

    }

    @Test
    public void testPersistenceBetweenSessions() throws IOException, JsonPersistenceException, IllegalJsonException {
	File file = Files.createTempDirectory("neo4j_test").toFile();
	Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(file);

	Neo4JGraphNode rootNode = ps.getRootNode();
	rootNode.write(jsonText);
	assertEquals(mapper.readTree(jsonText), mapper.readTree(ps.getRootNode().toJsonString()));
	ps.close();

	ps = new Neo4JJsonPersistenceService(file);

	rootNode = ps.getRootNode();
	assertEquals(mapper.readTree(jsonText), mapper.readTree(ps.getRootNode().toJsonString()));
	ps.close();

    }

    private Neo4JJsonPersistenceService createNewService() throws IOException {
	return new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test").toFile());
    }

}
