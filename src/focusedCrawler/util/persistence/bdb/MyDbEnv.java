/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package focusedCrawler.util.persistence.bdb;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;


public class MyDbEnv {

	 private Environment myEnv;
	 private EntityStore store;

	    // Our constructor does nothing
	    public MyDbEnv(File envHome, boolean readOnly) throws DatabaseException {

	        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
	        StoreConfig storeConfig = new StoreConfig();

//	        myEnvConfig.setTransactional(false);
//	        myEnvConfig.setTxnNoSync(true);

//	        myEnvConfig.setReadOnly(true);
//	        storeConfig.setReadOnly(true);

	        // If the environment is opened for write, then we want to be 
	        // able to create the environment and entity store if 
	        // they do not exist.
	        myEnvConfig.setAllowCreate(true);
//	        myEnvConfig.setTransactional(true);
	        storeConfig.setAllowCreate(true);

	        
	        // Open the environment and entity store
	        myEnv = new Environment(envHome, myEnvConfig);
	        store = new EntityStore(myEnv, "EntityStore", storeConfig);

	    }

	    // Return a handle to the entity store
	    public EntityStore getEntityStore() {
	        return store;
	    }

	    // Return a handle to the environment
	    public Environment getEnv() {
	        return myEnv;
	    }

	    // Close the store and environment.
	    public void close() {
	        if (store != null) {
	            try {
	                store.close();
	            } catch(DatabaseException dbe) {
	                System.err.println("Error closing store: " +
	                                    dbe.toString());
	               System.exit(-1);
	            }
	        }

	        if (myEnv != null) {
	            try {
	                // Finally, close the environment.
	                myEnv.close();
	            } catch(DatabaseException dbe) {
	                System.err.println("Error closing MyDbEnv: " +
	                                    dbe.toString());
	               System.exit(-1);
	            }
	        }
	    }

	
}
