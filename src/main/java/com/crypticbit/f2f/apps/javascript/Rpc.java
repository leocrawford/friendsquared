package com.crypticbit.f2f.apps.javascript;

public class Rpc {

    private String method;
    private String data;
    public Rpc() {
	
    }
    public Rpc(String function, String json) {
	this.method = function;
	this.data = json;
    }
    public String getMethod() {
	return method;
    }
    public void setMethod(String method) {
	this.method = method;
    }
    public String getData() {
	return data;
    }
    public void setData(String data) {
	this.data = data;
    }
    
    public String toString() {
	return method+"("+data+")";
    }
    
    
}
