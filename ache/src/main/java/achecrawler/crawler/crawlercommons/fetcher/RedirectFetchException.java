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

package achecrawler.crawler.crawlercommons.fetcher;

@SuppressWarnings({ "serial" })
public class RedirectFetchException extends BaseFetchException {

    // Possible redirect exception types.

    public enum RedirectExceptionReason {
        TOO_MANY_REDIRECTS, // - Request for original URL tried too many hops.
        PERM_REDIRECT_DISALLOWED, // - RedirectMode disallows a permanent
                                  // redirect.
        TEMP_REDIRECT_DISALLOWED, // - RedirectMode disallows a temp redirect.
        SEE_OTHER_DISALLOWED // - to handle 303 redirects, usually after POST
                             // method & RESTful applications
    }

    private String _redirectedUrl;
    private RedirectExceptionReason _reason;

    public RedirectFetchException() {
        super();
    }

    public RedirectFetchException(String url, String redirectedUrl, RedirectExceptionReason reason) {
        super(url, "Too many redirects");
        _redirectedUrl = redirectedUrl;
        _reason = reason;
    }

    public String getRedirectedUrl() {
        return _redirectedUrl;
    }

    public RedirectExceptionReason getReason() {
        return _reason;
    }

}
