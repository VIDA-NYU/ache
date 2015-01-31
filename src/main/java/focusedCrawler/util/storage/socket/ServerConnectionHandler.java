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


import java.net.ServerSocket;

import java.net.Socket;

import java.io.*;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.Log;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;


/**

 *

 */

class ServerConnectionHandler  extends Thread {



    private Storage storage;

    private Socket socket;

    private long startTime;

    private int inLength=0;

    private int outLength= 0;

    private ServerConnectionListener listener;



    private int methodId=-1;

    private DataInputStream din;

    private DataOutputStream dout;



    private byte[] buffer;

    private Object param;



    private int returnType=-1;

    private Object result;

    private ByteArrayOutputStream bout;



    public ServerConnectionHandler(int number, String storage_name, Storage storage, Socket socket, ServerConnectionListener listener)  {

        super();

        this.storage = storage;

        this.socket = socket;

        this.startTime = System.currentTimeMillis();

        this.listener = listener;

        setName("ServerConnectionHandler["+storage_name+"/"+number+"/"+listener.getConcurrentLevel()+"]-"+socket.getInetAddress()+":"+socket.getPort());

    }



    public String toString() {

        return getName();

    }



    private void readRequestData() throws IOException {

        // read request data

        methodId = din.read();

        inLength = din.readInt();

        buffer   = new byte[inLength];

        din.readFully(buffer);

    }



    private void buildRequestObject() throws ClassNotFoundException, IOException {

        // build serialized client request paramter object

        ByteArrayInputStream bin = new ByteArrayInputStream(buffer);

        ObjectInputStream oin = new ObjectInputStream(bin);

        param = oin.readObject();

        oin.close();

    }



    private void callStorage() throws StorageException, DataNotFoundException, CommunicationException {

        // call storage method

        switch(methodId) {

            case CommunicationConstants.METHOD_PING:

                result = storage.ping(param);

                break;

            case CommunicationConstants.METHOD_INSERT:

                result = storage.insert(param);

                break;

            case CommunicationConstants.METHOD_INSERT_ARRAY:

                result = storage.insertArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_SELECT:

                result = storage.select(param);

                break;

            case CommunicationConstants.METHOD_SELECT_ARRAY:

                result = storage.selectArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_SELECT_ENUMERATION:

                result = storage.selectEnumeration(param);

                break;

            case CommunicationConstants.METHOD_UPDATE:

                result = storage.update(param);

                break;

            case CommunicationConstants.METHOD_UPDATE_ARRAY:

                result = storage.updateArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_REMOVE:

                result = storage.remove(param);

                break;

            case CommunicationConstants.METHOD_REMOVE_ARRAY:

                result = storage.removeArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_ADD_RESOURCE:

                result = storage.addResource(param);

                break;

            case CommunicationConstants.METHOD_ADD_RESOURCE_ARRAY:

                result = storage.addResourceArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_REMOVE_RESOURCE:

                result = storage.removeResource(param);

                break;

            case CommunicationConstants.METHOD_REMOVE_RESOURCE_ARRAY:

                result = storage.removeResourceArray((Object[])param);

                break;

            case CommunicationConstants.METHOD_COMMIT:

                result = storage.commit(param);

                break;

            case CommunicationConstants.METHOD_ROLLBACK:

                result = storage.rollback(param);

                break;

            case CommunicationConstants.METHOD_FINALIZE:

                result = storage.finalize(param);

                break;

            default:

                throw new CommunicationException("protocol error: invalid method id " + methodId);

        }

    }



    private void serializeObject(Object obj) throws IOException {

        // serialize the result object or exception

        bout = new ByteArrayOutputStream();

        ObjectOutputStream oout = new ObjectOutputStream(bout);

        oout.writeObject(obj);

        oout.flush();

    }



    private void serializeException(Exception obj) throws IOException {

        System.out.println("serializando excecao...");

        obj.printStackTrace();

        // serialize the exception

        bout = new ByteArrayOutputStream();

        PrintStream pstream = new PrintStream(bout);

        obj.printStackTrace(pstream);

        pstream.flush();

        String exceptionString = bout.toString();

        serializeObject(exceptionString);

    }



    private void sendResult() throws IOException {

        // send data to client

        outLength = bout.size();

        dout.writeByte(returnType);

        dout.writeInt(bout.size());

        bout.writeTo(dout);

        dout.flush();

    }



    public void run() {

        long t1=0, t2=0, t3=0, t4=0, t5=0, t6=0;

        try {

            try {

                din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                dout = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                t1=System.currentTimeMillis();

                readRequestData();

                t2=System.currentTimeMillis();

                try {

                    buildRequestObject();

                    t3=System.currentTimeMillis();

                    callStorage();

                    t4=System.currentTimeMillis();

                    serializeObject(result);

                    returnType = CommunicationConstants.RETURN_OK;

                } catch(StorageException e) {

                    serializeException(e);

                    returnType = CommunicationConstants.RETURN_STORAGE_EXCEPTION;

                } catch(DataNotFoundException e) {

                    serializeException(e);

                    returnType = CommunicationConstants.RETURN_DATA_NOT_FOUND;

                } catch(CommunicationException e) {

                    serializeException(e);

                    returnType = CommunicationConstants.RETURN_COMMUNICATION_EXCEPTION;

                } catch(ClassNotFoundException e) {

                    serializeException(e);

                    returnType = CommunicationConstants.RETURN_COMMUNICATION_EXCEPTION;

                } catch(Exception e) {

                    serializeException(e);

                    returnType = CommunicationConstants.RETURN_COMMUNICATION_EXCEPTION;

                }

                t5=System.currentTimeMillis();

                sendResult();

                t6=System.currentTimeMillis();

            } catch(IOException e) {

                // ignore socket errors(closing and free client)

                Log.log(this.toString(), storage.toString(), "IO error:"+e);

            }

        } finally {

            try { socket.close(); }

            catch(IOException ioe) {

                Log.log(this.toString(), storage.toString(), "socket close error:"+ioe);

            }

            socket = null;

            din= null;

            dout = null;

            buffer = null;

            param = null;

            result = null;

            bout = null;

            t6-=t5; t5-=t4; t4-=t3; t3-=t2; t2-=t1; t1-=startTime;

            long tt = System.currentTimeMillis() - startTime;

            listener.outgoingConnection();

            if( Log.log ) {

                Log.log(toString(), getReturnType(returnType),

                        "time= "+tt+" dataIn= "+inLength+" dataOut= "+outLength+

                        " init= "+t1+" ,rr= "+t2+" ,bro= "+t3+" ,cs= "+t4+" ,so= "+t5+" ,sr= "+t6);

            }

        }

    }



    private String getReturnType(int t) {

        switch(t) {

            case CommunicationConstants.RETURN_OK:

                return "ok";

            case CommunicationConstants.RETURN_DATA_NOT_FOUND:

                return "data_not_found";

            case CommunicationConstants.RETURN_STORAGE_EXCEPTION:

                return "storage_exception";

            case CommunicationConstants.RETURN_COMMUNICATION_EXCEPTION:

                return "communication_exception";

            default:

                return "unknown_return_type";

        }

    }



}