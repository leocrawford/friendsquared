package com.crypticbit.f2f.apps.javascript;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRpcServerHandler extends IoHandlerAdapter {

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
	try {
	ObjectMapper mapper = new ObjectMapper();
	Rpc rpc = mapper.readValue(message.toString(), Rpc.class);

	System.out.println("Received: " + rpc); } catch (Exception e) {
	    e.printStackTrace();
	}

    }

}