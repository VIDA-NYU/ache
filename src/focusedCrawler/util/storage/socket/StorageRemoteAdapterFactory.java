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
package focusedCrawler.util.storage.socket;



import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.FactoryException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.AbstractStorageFactory;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactory;
import focusedCrawler.util.storage.StorageFactoryException;













/**

 * Fabrica de StorageRemoteAdapter

 */

public class StorageRemoteAdapterFactory extends AbstractStorageFactory {



    private String remoteHost;

    private int remotePort;



    public StorageRemoteAdapterFactory() {

        super();

    }



    public StorageRemoteAdapterFactory(ParameterFile config) throws StorageFactoryException {

        super(config);

    }



    private void initParams() throws StorageFactoryException {

        remoteHost = getConfig().getParam("RMI_STORAGE_SERVER_HOST").trim();

        remotePort = new Integer(getConfig().getParam("RMI_STORAGE_SERVER_PORT")).intValue();

    } //initParams



    public synchronized Storage produce() throws StorageFactoryException {

        initParams();

        focusedCrawler.util.Log.log("SocketAdapterFactory","produce",remoteHost+":"+remotePort);

        return new StorageRemoteAdapter(remoteHost, remotePort);

    }



} //class