package com.crypticbit.f2f.apps.javascript;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.crypticbit.javelin.neo4j.Neo4JJsonPersistenceService;

public class F2FNode {

    public F2FNode(int port) throws IOException, ScriptException {
	// create a script engine manager
	ScriptEngineManager factory = new ScriptEngineManager();
	// create a JavaScript engine
	ScriptEngine engine = factory.getEngineByName("JavaScript");
	// evaluate JavaScript code from String

	createServer(port);

	final Bindings bindings = engine.createBindings();
	bindings.put("local", createNewService().getRootNode());
	bindings.put("remote", createNewClient());
	engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

	Reader r = new InputStreamReader(F2FNode.class.getClassLoader().getResourceAsStream("readAndWriteJson.js"));
	engine.eval(r);
    }

    public static void main(String args[]) throws Exception, Exception {
	new F2FNode(9020);
//	new F2FNode(9021);
    }

    public Client createNewClient() {
	return new ClientImpl();
    }

    public void createServer(int port) throws IOException {

	IoAcceptor acceptor = new NioSocketAcceptor();

	acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	acceptor.getFilterChain().addLast("codec",
		new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

	acceptor.setHandler(new JsonRpcServerHandler());
	acceptor.bind(new InetSocketAddress(port));

    }

    protected Neo4JJsonPersistenceService createNewService(String identity) throws IOException {
	return new Neo4JJsonPersistenceService(Files.createTempDirectory("neo4j_test").toFile(), identity);
    }

    protected Neo4JJsonPersistenceService createNewService() throws IOException {
	return createNewService("unidentified-identity");
    }
}
