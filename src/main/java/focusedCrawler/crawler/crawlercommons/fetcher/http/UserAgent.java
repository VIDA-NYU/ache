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

package focusedCrawler.crawler.crawlercommons.fetcher.http;

import java.io.Serializable;
import java.util.Locale;

import focusedCrawler.crawler.crawlercommons.CrawlerCommons;

/**
 * User Agent enables us to describe characteristics of any Crawler Commons
 * agent. There are a number of constructor options to describe the following:
 * <ol>
 * <li><tt>_agentName</tt>: Primary agent name.</li>
 * <li><tt>_emailAddress</tt>: The agent owners email address.</li>
 * <li><tt>_webAddress</tt>: A web site/address representing the agent owner.</li>
 * <li><tt>_browserVersion</tt>: Broswer version used for compatibility.</li>
 * <li><tt>_crawlerVersion</tt>: Version of the user agents personal crawler. If
 * this is not set, it defaults to the crawler commons maven artifact version.</li>
 * </ol>
 * 
 */
@SuppressWarnings("serial")
public class UserAgent implements Serializable {

    public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
    public static final String DEFAULT_CRAWLER_VERSION = CrawlerCommons.getVersion();

    private final String _agentName;
    private final String _emailAddress;
    private final String _webAddress;
    private final String _browserVersion;
    private final String _crawlerVersion;

    /**
     * Set user agent characteristics
     * 
     * @param agentName
     * @param emailAddress
     * @param webAddress
     */
    public UserAgent(String agentName, String emailAddress, String webAddress) {
        this(agentName, emailAddress, webAddress, DEFAULT_BROWSER_VERSION);
    }

    /**
     * Set user agent characteristics
     * 
     * @param agentName
     * @param emailAddress
     * @param webAddress
     * @param browserVersion
     */
    public UserAgent(String agentName, String emailAddress, String webAddress, String browserVersion) {
        this(agentName, emailAddress, webAddress, browserVersion, DEFAULT_CRAWLER_VERSION);
    }

    /**
     * Set user agent characteristics
     * 
     * @param agentName
     * @param emailAddress
     * @param webAddress
     * @param browserVersion
     * @param crawlerVersion
     */
    public UserAgent(String agentName, String emailAddress, String webAddress, String browserVersion, String crawlerVersion) {
        _agentName = agentName;
        _emailAddress = emailAddress;
        _webAddress = webAddress;
        _browserVersion = browserVersion;
        _crawlerVersion = (crawlerVersion == null ? "" : "/" + crawlerVersion);
    }

    /**
     * Obtain the just the user agent name
     * 
     * @return User Agent name (String)
     */
    public String getAgentName() {
        return _agentName;
    }

    /**
     * Obtain a String representing the user agent characteristics.
     * 
     * @return User Agent String
     */
    public String getUserAgentString() {
        // Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com;
        // mycrawler@mydomain.com)
        return String.format(Locale.getDefault(), "%s (compatible; %s%s; +%s; %s)", _browserVersion, getAgentName(), _crawlerVersion, _webAddress, _emailAddress);
    }
}
