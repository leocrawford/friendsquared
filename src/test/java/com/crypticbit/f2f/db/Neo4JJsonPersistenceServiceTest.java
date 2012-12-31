package com.crypticbit.f2f.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	assertEquals(mapper.readTree("{\"id\":\"sd1 p\"}"), mapper.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));

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

	assertTrue("Check most recent history comes first", ps.getRootNode().getHistory().get(0).getTimestamp() > ps.getRootNode().getHistory()
		.get(3).getTimestamp());

	assertEquals(mapper.readTree("\"new value 2\""),
		mapper.readTree(ps.getRootNode().getHistory().get(2).getVersion().toJsonString()));
    }

    @Test
    public void getHistoryOfAdd() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = createNewService();

	Neo4JGraphNode rootNode = ps.getRootNode();
	rootNode.write(jsonText);
	rootNode.navigate("second").add().write("\"new value 1\"");
	rootNode.getStrategy().commit();
		
//	 ps.startWebServiceAndWait();
	
	assertEquals(2, ps.getRootNode().navigate("second").getHistory().size());

	ps.getRootNode().navigate("second").add().write("\"new value 2\"");

	assertEquals(3, ps.getRootNode().navigate("second").getHistory().size());
	
	
    }

    @Test
    public void testNavigateWithWildcards() throws IOException, JsonPersistenceException, IllegalJsonException {
	// these are currently not supported

    }

    private Neo4JJsonPersistenceService createNewService() throws IOException {
	return new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test").toFile());
    }

}
