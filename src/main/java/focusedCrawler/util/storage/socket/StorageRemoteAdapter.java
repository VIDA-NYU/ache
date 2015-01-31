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

import java.net.Socket;

import java.io.*;



import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.Log;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;










public class StorageRemoteAdapter implements Storage {



    private String remoteHost;

    private int remotePort;



    public StorageRemoteAdapter(String remoteHost, int remotePort) {

        this.remoteHost = remoteHost;

        this.remotePort = remotePort;

    }



    class RemoteChannel {

        Socket socket;

        DataInputStream in;

        DataOutputStream out;

        ByteArrayOutputStream bout;

        byte[] buffer;

    }



    private RemoteChannel getSocket() throws IOException {

        // connect to server

        RemoteChannel rc = new RemoteChannel();

        rc.socket = new Socket(remoteHost, remotePort);

        rc.in = new DataInputStream(new BufferedInputStream(rc.socket.getInputStream()));

        rc.out = new DataOutputStream(new BufferedOutputStream(rc.socket.getOutputStream()));

        rc.bout = new ByteArrayOutputStream();

        return rc;

    }



    private void serializeParamObject(RemoteChannel rc, Object obj) throws IOException {

        // convert to byte array

        rc.bout.reset();

        ObjectOutputStream oout = new ObjectOutputStream(rc.bout);

        oout.writeObject(obj);

        oout.flush();

    }



    private void sendRequestObject(RemoteChannel rc, int method) throws IOException {

        // send data throw the socket

        rc.out.writeByte(method);

        rc.out.writeInt(rc.bout.size());

        rc.bout.writeTo(rc.out);

        rc.out.flush();

    }



    private void readResultData(RemoteChannel rc) throws IOException {

        // read the result object

        int resultSize = rc.in.readInt();

        rc.buffer = new byte[resultSize];

        rc.in.readFully(rc.buffer);

    }



    private Object buildResultObject(RemoteChannel rc) throws IOException, ClassNotFoundException {

        // mount the serialized object

        ByteArrayInputStream bin = new ByteArrayInputStream(rc.buffer);

        ObjectInputStream oin = new ObjectInputStream(bin);

        Object result = oin.readObject();

        oin.close();

        return result;

    }



    private void releaseSocket(RemoteChannel rc) throws IOException {

        // close connection

        rc.socket.close();

    }



    private Object defaultMethod(int method_id, Object obj) throws StorageException, DataNotFoundException, CommunicationException {

        long t1=System.currentTimeMillis();

        long t2=0, t3=0, t4=0, t5=0, t6=0, t7=0;

        try {

            if(Log.log) {

                Log.log("adapter"+remoteHost+"_"+remotePort,

                        "method["+method_id+"] call_method",

                        Thread.currentThread().getName());

            }

            Object response=null;

            RemoteChannel socket = getSocket();

            t2 = System.currentTimeMillis();

            try {

                serializeParamObject(socket, obj);

                t3 = System.currentTimeMillis();

                sendRequestObject(socket, method_id);

                t4 = System.currentTimeMillis();

                int responseCode = socket.in.read();

                t5 = System.currentTimeMillis();

                readResultData(socket);

                t5 = System.currentTimeMillis();

                response = buildResultObject(socket);

                t6 = System.currentTimeMillis();

                switch(responseCode) {

                    case CommunicationConstants.RETURN_OK:

                        return response;

                    case CommunicationConstants.RETURN_STORAGE_EXCEPTION:

                        throw new StorageException("remote error: " + response);

                    case CommunicationConstants.RETURN_DATA_NOT_FOUND:

                        throw new DataNotFoundException("remote error " + response);

                    default:

                        throw new CommunicationException("protocol error " + response);

                }

            } finally {

                releaseSocket(socket);

                t7 = System.currentTimeMillis();

            }

        } catch(ClassNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        } catch(IOException e) {

            throw new CommunicationException(e.getMessage(), e);

        } finally {

            long tt=System.currentTimeMillis()-t1;

            t7-=t6; t6-=t5; t5-=t4; t4-=t3; t3-=t2; t2-=t1;

            if(Log.log) {

                Log.log("StorageRemoteAdapter_"+remoteHost+"_"+remotePort,

                        "method["+method_id+"] tempo="+tt,

                        "t2="+t2+", t3="+t3+", t4="+t4+", t5="+t5+", t6="+t6+", t7="+t7+" "+Thread.currentThread().getName());

            }

        }

    }





    public Object insert(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_INSERT, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object[] insertArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return (Object[]) defaultMethod(CommunicationConstants.METHOD_INSERT_ARRAY, objs);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        return defaultMethod(CommunicationConstants.METHOD_SELECT, obj);

    }



    public Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        return (Object[]) defaultMethod(CommunicationConstants.METHOD_SELECT_ARRAY, objs);

    }



    public Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        return (Enumeration) defaultMethod(CommunicationConstants.METHOD_SELECT_ENUMERATION, obj);

    }



    public Object update(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_UPDATE, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object[] updateArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return (Object[]) defaultMethod(CommunicationConstants.METHOD_UPDATE_ARRAY, objs);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object remove(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_REMOVE, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object[] removeArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return (Object[]) defaultMethod(CommunicationConstants.METHOD_REMOVE_ARRAY, objs);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object addResource(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_ADD_RESOURCE, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object[] addResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return (Object[]) defaultMethod(CommunicationConstants.METHOD_ADD_RESOURCE_ARRAY, objs);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object removeResource(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_REMOVE_RESOURCE, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object[] removeResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return (Object[]) defaultMethod(CommunicationConstants.METHOD_REMOVE_RESOURCE_ARRAY, objs);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object commit(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_COMMIT, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object rollback(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_ROLLBACK, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object finalize(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_FINALIZE, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }



    public Object ping(Object obj) throws StorageException,CommunicationException {

        try {

            return defaultMethod(CommunicationConstants.METHOD_PING, obj);

        } catch(DataNotFoundException e) {

            throw new CommunicationException(e.getMessage(), e);

        }

    }

}