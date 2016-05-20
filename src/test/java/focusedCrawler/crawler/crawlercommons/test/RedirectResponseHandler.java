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

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

/**
 * Response handler that can be used to do redirects..
 * 
 */
@SuppressWarnings("serial")
public class RedirectResponseHandler extends AbstractHttpHandler {

    private String _originalPath;
    private String _redirectUrl;
    private boolean _doRedirects;

    public RedirectResponseHandler() {
        _doRedirects = false;
    }

    public RedirectResponseHandler(String originalPath, String redirectUrl) {
        _originalPath = originalPath;
        _redirectUrl = redirectUrl;
        _doRedirects = true;
    }

    @Override
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        if (_doRedirects && pathInContext.equalsIgnoreCase(_originalPath)) {
            response.sendRedirect(_redirectUrl);
        } else if (_doRedirects && _redirectUrl.contains(pathInContext)) {
            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/plain");

            String content = "redirected content";
            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes("UTF-8"));
        } else {
            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/plain");

            String content = "other content";
            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes("UTF-8"));
        }
    }
}
