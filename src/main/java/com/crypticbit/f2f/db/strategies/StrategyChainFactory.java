package com.crypticbit.f2f.db.strategies;

import java.lang.reflect.InvocationTargetException;

public class StrategyChainFactory {

    public VersionStrategy createVersionStrategies(Class<? extends VersionStrategy>... classes) {
	VersionStrategy parent = null;
	for (int i = classes.length - 1; i >= 0; i--) {
	    try {
		parent = classes[i].getConstructor(VersionStrategy.class).newInstance(parent);
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return parent;
    }

}
