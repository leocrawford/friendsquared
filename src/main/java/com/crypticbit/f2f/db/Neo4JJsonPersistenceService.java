package com.crypticbit.f2f.db;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.jsonpath.JsonPath;

/**
 * Provides persistence for Json objects (depicted using Jackson JsonNode) with
 * lookup functions using Jackson-JsonPaths.
 * 
 * @author leo
 * 
 */
public class Neo4JJsonPersistenceService implements JsonPersistenceService {

	/**
	 * The location of the database (this is actually a directory)
	 */
	private File file;
	private transient GraphDatabaseService graphDb;
	private transient Node referenceNode;
	private static JsonNodeFactory jnf = JsonNodeFactory.instance;

	/** Use (or create if not present) the neo4j database at this location */
	public Neo4JJsonPersistenceService(File file) {
		this.file = file;
		setup();
	}

	/**
	 * Delete the loaded database, and recreate an empty one at the same
	 * location
	 * 
	 * @param iAgreeThisisVeryDangerous
	 *            if you don't agree - it won't delete
	 */
	public void empty(boolean iAgreeThisisVeryDangerous) {
		if (iAgreeThisisVeryDangerous) {
			if (graphDb != null)
				graphDb.shutdown();
			file.delete();
			setup();
		}
	}

	/** Do everything that's needed to actually create the database */
	private void setup() {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(file
				.getAbsolutePath());
		registerShutdownHook(graphDb);
		referenceNode = graphDb.getReferenceNode();

	}

	/** Get the root of the graph */
	private Node getRootGraphNode() {
		return referenceNode;
	}

	/**
	 * Get the root of the tree - which could be pretty big, but lucily
	 * everything is lazily loaded
	 */
	public JsonNode getRootJsonNode() {
		return toJsonUsingAdapter(getRootGraphNode());
	}

	/** Return the subset of the tree that is defined by this path */
	public JsonNode get(JsonPath path) {
		return path.read(getRootJsonNode());
	}

	private JsonNode toJsonUsingAdapter(Node graphNode) {
		return NodeTypes.wrapAsJsonNode(graphNode);
	}

	/** Put the values at the location depicted by the path */
	public void put(JsonPath path, JsonNode values)
			throws JsonPersistenceException {
		if (path == null)
			writeNode(getRootGraphNode(), values);
		else {
			if (!path.isPathDefinite())
				throw new JsonPersistenceException(
						"The path \"+path+\" is ambiguous.");
			JsonNode node = get(path);
			if (node instanceof JsonNodeGraphAdapter) {
				Node graphNode = ((JsonNodeGraphAdapter) node)
						.getDatabaseNode();
				writeNode(graphNode, values);
			} else throw new JsonPersistenceException("Found a node that isn't backed by the db");
		}
	}
	
	
	public void add(JsonPath path, JsonNode values) {
		
	}

	private void writeNode(Node graphNode, JsonNode values) {
		Transaction tx = graphDb.beginTx();
		try {
			copyJsonToGraph(graphNode, values);
			tx.success();
		} finally {
			tx.finish();
		}
	}

	private void copyJsonToGraph(Node graphNode, JsonNode jsonNode) {
		if (jsonNode.isContainerNode()) {
			if (jsonNode.isArray()) {
				graphNode.setProperty("type", NodeTypes.ARRAY.toString());
				for (int loop = 0; loop < jsonNode.size(); loop++) {
					Node newNode = graphDb.createNode();
					graphNode.createRelationshipTo(newNode, RelTypes.ARRAY)
							.setProperty("index", loop);
					copyJsonToGraph(newNode, jsonNode.get(loop));
				}
			}
			if (jsonNode.isObject()) {
				graphNode.setProperty("type", NodeTypes.MAP.toString());
				Iterator<String> fieldNamesIterator = jsonNode.fieldNames();
				while (fieldNamesIterator.hasNext()) {
					String f = fieldNamesIterator.next();
					JsonNode e = jsonNode.get(f);
					Node newNode = graphDb.createNode();
					graphNode.createRelationshipTo(newNode, RelTypes.MAP)
							.setProperty("name", f);
					;
					copyJsonToGraph(newNode, e);
				}
			}
		} else {
			graphNode.setProperty("type", NodeTypes.VALUE.toString());
			graphNode.setProperty("value", jsonNode.asText());
		}

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	static <T> T createDynamicWrapperForNode(Node node, Class<T> clazz) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(clazz);
		factory.setFilter(new MethodFilter() {

			public boolean isHandled(java.lang.reflect.Method method) {
				return !method.getName().equals("updateNodes");
			}
		});

		MethodHandler handler = new MethodHandler() {

			public Object invoke(Object self, Method thisMethod,
					Method proceed, Object[] args) throws Throwable {
				((JsonNodeGraphAdapter) self).updateNodes();
				return proceed.invoke(self, args);
			}

		};

		try {
			return (T) factory.create(new Class<?>[] { JsonNodeFactory.class,
					Node.class, }, new Object[] { jnf, node }, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

}
