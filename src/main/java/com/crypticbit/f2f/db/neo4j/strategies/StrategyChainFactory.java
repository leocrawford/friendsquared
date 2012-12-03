package com.crypticbit.f2f.db.neo4j.strategies;

import java.lang.reflect.InvocationTargetException;

public class StrategyChainFactory {

    public VersionStrategy createVersionStrategies(Class<? extends VersionStrategyImpl>... classes) {
	VersionStrategyImpl successor = null;
	for (int i = classes.length - 1; i >= 0; i--) {
	    try {
		successor = classes[i].getConstructor(VersionStrategyImpl.class).newInstance(successor);
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	for(VersionStrategyImpl vs = successor; vs != null; vs = vs.getSuccessor())
	    vs.setRoot(successor);
	return successor;
    }

}
