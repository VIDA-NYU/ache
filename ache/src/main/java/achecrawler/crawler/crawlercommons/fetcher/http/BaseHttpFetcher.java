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

package achecrawler.crawler.crawlercommons.fetcher.http;

import achecrawler.crawler.crawlercommons.fetcher.BaseFetcher;

@SuppressWarnings("serial")
public abstract class BaseHttpFetcher extends BaseFetcher {

    public enum RedirectMode {
        FOLLOW_ALL, // Fetcher will try to follow all redirects
        FOLLOW_TEMP, // Temp redirects are automatically followed, but not
                     // permanent.
        FOLLOW_NONE // No redirects are followed.
    }

    public static final int NO_MIN_RESPONSE_RATE = Integer.MIN_VALUE;
    public static final int NO_REDIRECTS = 0;

    public static final int DEFAULT_MIN_RESPONSE_RATE = NO_MIN_RESPONSE_RATE;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final int DEFAULT_MAX_REDIRECTS = 20;
    public static final String DEFAULT_ACCEPT_LANGUAGE = "en-us,en-gb,en;q=0.7,*;q=0.3";
    public static final RedirectMode DEFAULT_REDIRECT_MODE = RedirectMode.FOLLOW_ALL;

    protected int _maxThreads;
    protected UserAgent _userAgent;
    protected String _userAgentString;
    protected int _maxRedirects = DEFAULT_MAX_REDIRECTS;
    protected int _maxConnectionsPerHost = DEFAULT_MAX_CONNECTIONS_PER_HOST;
    protected int _minResponseRate = DEFAULT_MIN_RESPONSE_RATE;
    protected String _acceptLanguage = DEFAULT_ACCEPT_LANGUAGE;
    protected RedirectMode _redirectMode = DEFAULT_REDIRECT_MODE;

    public BaseHttpFetcher(int maxThreads, UserAgent userAgent) {
        super();
        _maxThreads = maxThreads;
        _userAgent = userAgent;
        _userAgentString = userAgent.getUserAgentString();
    }

    public int getMaxThreads() {
        return _maxThreads;
    }

    public UserAgent getUserAgent() {
        return _userAgent;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        _maxConnectionsPerHost = maxConnectionsPerHost;
    }

    public int getMaxConnectionsPerHost() {
        return _maxConnectionsPerHost;
    }

    public void setMinResponseRate(int minResponseRate) {
        _minResponseRate = minResponseRate;
    }

    /**
     * Return the minimum response rate. If the speed at which bytes are being
     * returned from the server drops below this, the fetch of that page will be
     * aborted.
     * 
     * @return bytes/second
     */
    public int getMinResponseRate() {
        return _minResponseRate;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        _acceptLanguage = acceptLanguage;
    }

    public String getAcceptLanguage() {
        return _acceptLanguage;
    }

    public void setMaxRedirects(int maxRedirects) {
        _maxRedirects = maxRedirects;
    }

    public int getMaxRedirects() {
        return _maxRedirects;
    }

    public void setRedirectMode(RedirectMode mode) {
        _redirectMode = mode;
    }

    public RedirectMode getRedirectMode() {
        return _redirectMode;
    }

}
