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

package focusedCrawler.crawler.crawlercommons.test;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpServer;

public class SimulationWebServer {

    private HttpServer _server;

    public HttpServer startServer(HttpHandler handler, int port) throws Exception {
        _server = new HttpServer();
        _server.addListener(":" + port);
        HttpContext context = _server.getContext("/");
        context.addHandler(handler);
        _server.start();
        return _server;
    }

    public void stopServer() throws InterruptedException {
        if (_server != null) {
            _server.stop();
        }
    }

    public HttpServer getServer() {
        return _server;
    }

}
