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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ResourcesResponseHandler extends AbstractHandler {
    private String _testContext = "./";

    /**
     * Create an HTTP response handler that sends data back from files on the
     * classpath TODO KKr - use regular Jetty support for this, via setting up
     * HttpContext
     * 
     */
    public ResourcesResponseHandler() {
    }

    public ResourcesResponseHandler(String testContext) {
        _testContext = testContext;
    }

    @Override
    public void handle(String pathInContext, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the resource.
        URL path = ResourcesResponseHandler.class.getResource(_testContext + pathInContext);
        if (path == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + pathInContext);
            return;
        }

        try {
            File file = new File(path.getFile());
            byte[] bytes = new byte[(int) file.length()];
            @SuppressWarnings("resource")
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            in.readFully(bytes);

            response.setContentLength(bytes.length);
            if (file.getName().endsWith(".png")) {
                response.setContentType("image/png");
            } else {
                response.setContentType("text/html");
            }
            response.setStatus(200);

            OutputStream os = response.getOutputStream();
            os.write(bytes);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
