package com.crypticbit.f2f.apps;

import com.crypticbit.javelin.JsonPersistenceService;


/**
 * The Application Manager manages all the applications, including the system
 * applications. Providing loose coupling between them
 */

public class ApplicationManager implements Application {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

    private JsonPersistenceService jps;

    public ApplicationManager() {

    }

    private <P> P getService(Class<P> clazz) {
	// We need to do some bootstrapping
	if (jps == null) {
	    // jps = new Neo4JJsonPersistenceService();
	}
	// String className = jps.find("app.system[" + clazz
	// + "].default.className");
	// try {
	// return (P) Class.forName(className).newInstance();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// return null;
	// }
	return null;

    }

}
