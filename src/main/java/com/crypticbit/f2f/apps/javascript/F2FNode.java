package com.crypticbit.f2f.apps.javascript;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.crypticbit.javelin.IllegalJsonException;
import com.crypticbit.javelin.JsonPersistenceException;
import com.crypticbit.javelin.neo4j.Neo4JJsonPersistenceService;

public class F2FNode {

    private Map<String, Executor> executors = new HashMap<>();
    Neo4JJsonPersistenceService persistService = createNewService();

    public F2FNode(int port, String... defaultApps) throws IOException, ScriptException, NoSuchMethodException, IllegalJsonException, JsonPersistenceException {
	createServer(port);
	persistService.getRootNode().write("{\"initial\":\"value\"}");
	for (String app : defaultApps)
	    addExecutor("127.0.0.1", port, app);
	for (String app : defaultApps)
	    executors.get(app).run();
    }

    private void addExecutor(String server, int port, String name) throws IOException, ScriptException, IllegalJsonException, JsonPersistenceException {
	executors.put(name, new Executor(persistService, server, port, name));
    }

    public static void main(String args[]) throws Exception, Exception {
	new F2FNode(9020, "readAndWriteJson.js");
	new F2FNode(9030, "setFriendTo9020.js","readAndWriteJson.js");
    }

    public void createServer(int port) throws IOException {
	IoAcceptor acceptor = new NioSocketAcceptor();

	acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	acceptor.getFilterChain().addLast("codec",
		new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

	acceptor.setHandler(new JsonRpcServerHandler(this));
	acceptor.bind(new InetSocketAddress(port));

    }

    protected Neo4JJsonPersistenceService createNewService(String identity) throws IOException {
	return new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test").toFile(), identity);
    }

    protected Neo4JJsonPersistenceService createNewService() throws IOException {
	return createNewService("unidentified-identity");
    }

    public void execute(Rpc rpc) throws NoSuchMethodException, ScriptException {
	Executor e = executors.get(rpc.getUnit());
	System.out.println("Executor = " + e + ":" + rpc.getUnit() + "," + executors.keySet());
	if (e == null)
	    throw new Error("Can not find " + rpc.getUnit());
	else
	    e.run(rpc.getMethod(), rpc.getData());

    }
}
