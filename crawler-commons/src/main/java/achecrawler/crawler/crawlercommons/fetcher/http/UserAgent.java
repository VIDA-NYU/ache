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

import java.io.Serializable;

import achecrawler.crawler.crawlercommons.CrawlerCommons;

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

    public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0";
    public static final String DEFAULT_CRAWLER_VERSION = CrawlerCommons.getVersion();

    private final String _agentName;
    private final String _userAgentString;

    private UserAgent(Builder builder) {
        this._agentName = builder._agentName;
        this._userAgentString = builder._userAgentString;
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
        return _userAgentString;
    }

    /**
     * Builds a user agent with custom characteristics
     * 
     */
    public static class Builder {

        private String _agentName;
        private String _emailAddress;
        private String _webAddress;
        private String _browserVersion = DEFAULT_BROWSER_VERSION;
        private String _crawlerVersion = DEFAULT_CRAWLER_VERSION;
        private String _userAgentString;

        public Builder() {}

        public Builder setAgentName(String _agentName) {
            this._agentName = _agentName;
            return this;
        }

        public Builder setEmailAddress(String _emailAddress) {
            this._emailAddress = _emailAddress;
            return this;
        }

        public Builder setWebAddress(String _webAddress) {
            this._webAddress = _webAddress;
            return this;
        }

        public Builder setBrowserVersion(String _browserVersion) {
            this._browserVersion = _browserVersion;
            return this;
        }

        public Builder setCrawlerVersion(String _crawlerVersion) {
            this._crawlerVersion = _crawlerVersion;
            return this;
        }

        public Builder setUserAgentString(String _userAgentString) {
            this._userAgentString = _userAgentString;
            return this;
        }

        /**
         * Creates a string representing the user agent characteristics.
         * 
         * @return User Agent String
         */
        private String createUserAgentString() {
            // Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com;
            // mycrawler@mydomain.com)
            StringBuilder sb = new StringBuilder();
            sb.append(_browserVersion);
            sb.append(" (compatible; ");
            sb.append(_agentName);
            sb.append("/");
            sb.append(_crawlerVersion);
            if (_webAddress != null && !_webAddress.isEmpty()) {
                sb.append("; +");
                sb.append(_webAddress);
            }
            if (_emailAddress != null && !_emailAddress.isEmpty()) {
                if(_webAddress == null || _webAddress.isEmpty()) {
                    sb.append(";");
                }
                sb.append(" ");
                sb.append(_emailAddress);
            }
            sb.append(")");
            return sb.toString();
        }

        public UserAgent build() {
            if (_userAgentString == null) {
                _userAgentString = createUserAgentString();
            }
            return new UserAgent(this);
        }

    }

}
