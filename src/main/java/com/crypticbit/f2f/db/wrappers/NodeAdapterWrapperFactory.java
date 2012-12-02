package com.crypticbit.f2f.db.wrappers;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class NodeAdapterWrapperFactory {

    static JsonNodeFactory jnf = JsonNodeFactory.instance;

    public static <T> T createDynamicWrapperForNode(Node node, Class<T> clazz) {
	ProxyFactory factory = new ProxyFactory();
	factory.setSuperclass(clazz);
	factory.setFilter(new MethodFilter() {

	    @Override
	    public boolean isHandled(java.lang.reflect.Method method) {
		return !method.getName().equals("updateNodes")
			&& !method.getName().equals("getGraphParent");
	    }
	});

	MethodHandler handler = new MethodHandler() {

	    @Override
	    public Object invoke(Object self, Method thisMethod,
		    Method proceed, Object[] args) throws Throwable {
		return null;
//		if (Modifier.isAbstract(thisMethod.getModifiers())) {
//		    OutwardFacingJsonNodeGraphAdapter t = ((JsonNodeGraphAdapter) self)
//			    .getGraphParent();
//		     return t.getClass().getMethod(thisMethod.getName(),thisMethod.getParameterTypes()).invoke(t, args);
////		    System.out.println("executing "+thisMethod+" on "+t.getClass());
////		    return thisMethod.invoke(t, args);
//		} else {
//		    ((JsonNodeGraphAdapter) self).updateNodes();
//		    return proceed.invoke(self, args);
//		}
	    }

	};

	try {
	    return (T) factory.create(new Class<?>[] { JsonNodeFactory.class,
		    Node.class, }, new Object[] { jnf, node }, handler);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;

	}
    }

}