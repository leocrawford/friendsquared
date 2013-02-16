package com.crypticbit.f2f.apps.javascript;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public final class ClientImpl implements Client {

    private String unit;

    public ClientImpl(String unit) {
	this.unit = unit;
    }

    @Override
    public void send(final int port, final String function, final String json) {
	IoConnector connector = new NioSocketConnector();
	connector.getSessionConfig().setReadBufferSize(2048);

	connector.getFilterChain().addLast("logger", new LoggingFilter());
	connector.getFilterChain().addLast("codec",
		new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

	connector.setHandler(new JsonRpcClientHandler(new Rpc(unit, function, json)));
	ConnectFuture future = connector.connect(new InetSocketAddress("localhost", port));
	future.awaitUninterruptibly();

	if (!future.isConnected()) {
	    System.out.println("Not connected");
	    return;
	}
	IoSession session = future.getSession();
	session.getConfig().setUseReadOperation(true);
	session.getCloseFuture().awaitUninterruptibly();

	System.out.println("After Writing");
	connector.dispose();

    }
}