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
package focusedCrawler.util.storage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageItem;
import focusedCrawler.util.storage.StorageItemEnumeration;


public class SelectedStorageItemEnumeration implements StorageItemEnumeration {
    private StorageItem result;
    private Storage     storage;
    public SelectedStorageItemEnumeration() {
    }
    public StorageItem getStorageItem() {
        return result;
    }
    public void setStorageItem(StorageItem newStorageItem) {
        result = newStorageItem;
    }
    public Storage getStorage() {
        return storage;
    }
    public void setStorage(Storage newStorage) {
        storage = newStorage;
    }
    public boolean hasNext() throws StorageException{
        try{
            result = (StorageItem) storage.select(result);
            return true;
        } //try
        catch(DataNotFoundException error){
            error.printStackTrace();
            return false;
        } //catch
        catch(CommunicationException error){
            error.printStackTrace();
            throw new StorageException(error);
        } //catch
    }
    public StorageItem next() throws StorageException {

        return result;

    }


    public void free() throws StorageException {

        try {

            storage.finalize(null);

        } //try

        catch(CommunicationException error){

            error.printStackTrace();

            throw new StorageException(error);

        } //catch

    }

}

