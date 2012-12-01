package com.crypticbit.f2f.db.strategies;

import java.lang.reflect.InvocationTargetException;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.databind.JsonNode;

public class StrategyChainFactory {

    public VersionStrategy createVersionStrategies(Class<? extends VersionStrategy>... classes) {
	VersionStrategy parent = null;
	for(int i=classes.length-1;i>=0;i--){
	    try {
		parent = classes[i].getConstructor(VersionStrategy.class).newInstance(parent);
	    } catch (InstantiationException | IllegalAccessException
		    | IllegalArgumentException | InvocationTargetException
		    | NoSuchMethodException | SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return parent;
    }
    

    
}
