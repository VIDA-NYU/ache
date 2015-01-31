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
package focusedCrawler.util.storage.thread;



import java.util.Enumeration;

import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;

public class StorageThread extends StorageDefault {



    // thread que armazenara os objetos

    private ThreadStorage thread;



    public StorageThread (ThreadStorage _Thread) {

        this.thread = _Thread;

        thread.start();

    } //Storage



    public Object[] insertArray(Object[] objs) throws StorageException, CommunicationException{

        thread.addResourceArray(objs);

        return null;

    } //insertArray



    public Object insert(Object obj) throws StorageException, CommunicationException{

        thread.addResource(obj);

        return null;

    } //insert



    public Object[] updateArray(Object[] objs) throws StorageException, CommunicationException{

        thread.addResourceArray(objs);

        return null;

    } //updateArray



    public Object update(Object obj) throws StorageException, CommunicationException{

        thread.addResource(obj);

        return null;

    } //update



    public Object[] removeArray(Object[] objs) throws StorageException, CommunicationException{

        thread.removeResourceArray(objs);

        return null;

    } //removeArray



    public Object remove(Object obj) throws StorageException, CommunicationException{

        thread.removeResource(obj);

        return null;

    } //remove



    public Object finalize(Object obj) throws StorageException, CommunicationException {

        thread.setToRun(false);

        return obj;

    } //finalize



    public Object commit(Object obj) throws StorageException, CommunicationException {

        System.out.println("StorageThread commit: " + obj);

        thread.commit();

        return obj;

    } //commit



    public Object rollback(Object obj) throws StorageException, CommunicationException {

        return obj;

    } //commit



}

