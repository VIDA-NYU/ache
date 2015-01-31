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

import java.io.IOException;

import focusedCrawler.util.Log;
import focusedCrawler.util.storage.Storage;







class ServerConnectionListener  extends Thread {



    private Storage storage;

    private String storageName;

    private int serverPort;

    private boolean running = true;

    private ServerSocket serverSocket;



    private int connectionCount=0;

    private int concurrentAccess=0;

    private int maxConcurrentAccess=128;



    private synchronized int incomeConnection() {

//        while(concurrentAccess > maxConcurrentAccess) {

//            try {

//                Log.log(toString(), storage.toString(), "locking conections queue is full " + concurrentAccess);

//                wait();

//            } catch(InterruptedException e) {

//                e.printStackTrace();

//            }

//        }

        connectionCount++;

        concurrentAccess++;

        return connectionCount;

    }



    synchronized void outgoingConnection() {

        concurrentAccess--;

//        notifyAll();

    }



    synchronized int getConcurrentLevel() {

        return concurrentAccess;

    }



    public ServerConnectionListener(Storage storage, int port)  throws IOException{

        super();

        this.storage = storage;

        storageName = storage.getClass().toString();

        this.serverPort = port;

        serverSocket = new ServerSocket(port);

        setName(this.toString());

        Log.log(this.toString(), storage.toString(), "listening on port " + port);

    }



    public void setStorage(Storage _storage) {

        storage = _storage;

        storageName = storage.getClass().toString();

    }



    public Storage getStorage() {

        return storage;

    }



    public String toString() {

        return "ServerConnectionListener:"+serverPort;

    }



    public void run() {

        while(running) {

            try {

                Socket clientSocket = serverSocket.accept();

                if (!running) {

                    clientSocket.close();

                    break;

                }

                int number = incomeConnection();

                ServerConnectionHandler handler;

                handler = new ServerConnectionHandler(number, storageName, storage, clientSocket, this);

                handler.start();

            } catch(Exception  e) {

                Log.log(this.toString(), storage.toString(), "server error : " + e.getMessage());

            }

        }

        Log.log(this.toString(), storage.toString(), "server died");

    }



    public void close() throws IOException {

        running = false;

        if (serverSocket != null) {

            serverSocket.close();

            serverSocket = null;

        }

    }



}