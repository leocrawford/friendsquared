package com.crypticbit.f2f.db;

import static org.junit.Assert.assertEquals;

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
    public void testFindFromRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
	File directory = Files.createTempDirectory("neo4j_test")
		.toFile();
	
	System.out.println("Directory: "+directory);
	
	Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(directory);
	
	ps.overwrite(jsonText);
	System.out.println(ps.toJsonString());
	ps.navigate("second").overwrite("\"blah blah\"");
	System.out.println(ps.toJsonString());
	ps.navigate("second").overwrite("\"blah 1\"");	
	System.out.println(ps.toJsonString());
	System.out.println(ps.navigate("second").getHistory());
	
//	ps.startWebService();
//	try {
//	    Thread.sleep(1000 * 60 * 60);
//	} catch (InterruptedException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//	ps.stopWebService();
//	
	// ps.put(JsonPath.compile("second[0]"),
	// ps.get(JsonPath.compile("second[4]")));
	// System.out.println(ps.getRootJsonNode());
	// System.out.println(ps.get(JsonPath.compile("second[0]")));
	// System.out.println(ps.get(JsonPath.compile("$.second[0]")));
	// System.out.println(ps.get(JsonPath.compile("second[4]")));
	ps.close();
	
    }

    @Test
    public void testGetRootJsonNode() throws IOException, JsonPersistenceException, IllegalJsonException {
	Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test")
		.toFile());

	ps.overwrite(jsonText); // we have to
							     // convert our
							     // return value to
							     // get rid of
							     // spaces, etc. and
	// ensure we do type comparison
	System.out.println(ps.toJsonString());
	assertEquals(mapper.readTree(ps.toJsonString()), mapper.readTree(jsonText));
    }

}
