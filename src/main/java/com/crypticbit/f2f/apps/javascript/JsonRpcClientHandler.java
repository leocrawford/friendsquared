package com.crypticbit.f2f.apps.javascript;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRpcClientHandler extends IoHandlerAdapter {

    private Rpc rpc;

    public JsonRpcClientHandler(Rpc rpc) {
	this.rpc = rpc;
    }

    public void sessionOpened(IoSession session) {
	ObjectMapper mapper = new ObjectMapper();

	String send;
	try {
	    send = mapper.writeValueAsString(rpc);
	    session.write(send);
	    System.out.println("sent:"+send);
	} catch (JsonProcessingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
	session.close();
    }

}