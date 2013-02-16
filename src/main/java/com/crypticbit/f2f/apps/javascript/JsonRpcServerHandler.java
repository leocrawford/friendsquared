package com.crypticbit.f2f.apps.javascript;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRpcServerHandler extends IoHandlerAdapter {

    private F2FNode f2f;

    JsonRpcServerHandler(F2FNode f2f) {
	this.f2f = f2f;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    Rpc rpc = mapper.readValue(message.toString(), Rpc.class);

	    System.out.println("Received: " + rpc);
	    f2f.execute(rpc);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}