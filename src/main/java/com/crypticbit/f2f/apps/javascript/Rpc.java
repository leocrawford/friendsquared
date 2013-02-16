package com.crypticbit.f2f.apps.javascript;

public class Rpc {

    private String unit;
    private String method;
    private String data;
    public Rpc() {
	
    }
    public Rpc(String unit, String function, String json) {
	this.unit = unit;
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
	return unit+"."+method+"("+data+")";
    }
    public String getUnit() {
	return unit;
    }
    public void setUnit(String unit) {
	this.unit = unit;
    }
    
    
}
