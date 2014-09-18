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
package focusedCrawler.util.storage.distribution;

import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.AbstractStorageBinder;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageBinderException;


public class StorageBinder extends AbstractStorageBinder {

    public StorageBinder() {
        super();
    }

    public StorageBinder(ParameterFile config) {
        super(config);
    }

    public void bind(Storage storage) throws StorageBinderException {
        if (getConfig() != null) {
            try {
                focusedCrawler.util.storage.StorageBinder binder = (focusedCrawler.util.storage.StorageBinder) Class.forName(getConfig().getParam("STORAGE_BINDER_CLASSNAME")).newInstance();
                 //System.out.println("Storage Binder class : " + getConfig().getParam("STORAGE_BINDER_CLASSNAME"));
                binder.setConfig(getConfig());
                binder.bind(storage);

            } //try
            catch(ClassNotFoundException error) {
                throw new StorageBinderException(error);
            } //catch
            catch(IllegalAccessException error) {
                throw new StorageBinderException(error);
            } //catch
            catch(InstantiationException error) {
                throw new StorageBinderException(error);
            } //catch
        } //if
        else {
            throw new StorageBinderException("config not set!");
        } //else
    } //bind
}
