/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package achecrawler.crawler.crawlercommons.test;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class SimulationWebServer {

    private Server _server;

    public Server startServer(Handler handler, int port) throws Exception {
        _server = new Server(port);
        _server.setHandler(handler);
        _server.start();
        return _server;
    }

    public void stopServer() throws Exception {
        if (_server != null) {
            _server.stop();
        }
    }

    public Server getServer() {
        return _server;
    }

}
