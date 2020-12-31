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

import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.Arrays;

import org.apache.tika.metadata.Metadata;

public class FetchedResult {
    private final String _baseUrl;
    private final String _fetchedUrl;
    private final long _fetchTime;
    private final byte[] _content;
    private final String _contentType;
    private final int _responseRate;
    private final Metadata _headers;
    private final String _newBaseUrl;
    private final int _numRedirects;
    private final String _hostAddress;
    private final int _statusCode; // HTTP status code
    private final String _reasonPhrase; // HTTP reason phrase, or null
    private Payload _payload;

    public FetchedResult(String baseUrl, String redirectedUrl, long fetchTime, Metadata headers, byte[] content, String contentType, int responseRate, Payload payload, String newBaseUrl,
                    int numRedirects, String hostAddress, int statusCode, String reasonPhrase) {
        _payload = payload;

        if (baseUrl == null) {
            throw new InvalidParameterException("baseUrl cannot be null");
        }

        if (redirectedUrl == null) {
            throw new InvalidParameterException("redirectedUrl cannot be null");
        }

        if (headers == null) {
            throw new InvalidParameterException("headers cannot be null");
        }

        if (content == null) {
            throw new InvalidParameterException("content cannot be null");
        }

        if (contentType == null) {
            throw new InvalidParameterException("contentType cannot be null");
        }

        if (hostAddress == null) {
            throw new InvalidParameterException("hostAddress cannot be null");
        }

        _baseUrl = baseUrl;
        _fetchedUrl = redirectedUrl;
        _fetchTime = fetchTime;
        _content = content;
        _contentType = contentType;
        _responseRate = responseRate;
        _headers = headers;
        _newBaseUrl = newBaseUrl;
        _numRedirects = numRedirects;
        _hostAddress = hostAddress;
        _statusCode = statusCode;
        _reasonPhrase = reasonPhrase;
    }

    public Payload getPayload() {
        return _payload;
    }

    public void setPayload(Payload payload) {
        _payload = payload;
    }

    public String getBaseUrl() {
        return _baseUrl;
    }

    public String getFetchedUrl() {
        return _fetchedUrl;
    }

    public long getFetchTime() {
        return _fetchTime;
    }

    public byte[] getContent() {
        return _content;
    }

    public int getContentLength() {
        return _content.length;
    }

    public String getContentType() {
        return _contentType;
    }

    public int getResponseRate() {
        return _responseRate;
    }

    public Metadata getHeaders() {
        return _headers;
    }

    public String getNewBaseUrl() {
        return _newBaseUrl;
    }

    public int getNumRedirects() {
        return _numRedirects;
    }

    public String getHostAddress() {
        return _hostAddress;
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public String getReasonPhrase() {
        return _reasonPhrase;
    }

    /**
     * Produces a neat report containing everything from a {@link FetchedResult}
     * . The order of the report is based on the logical population of
     * FetchedReport entities as per a non-public algorithm within
     * {@link crawlercommons.fetcher.http.SimpleHttpFetcher}.
     * 
     * @return returns a String report of the FetchedResult.
     */
    public String report() {
        StringBuilder report = new StringBuilder();
        report.append("FetchedResult Report:\n");
        report.append("*********************\n");
        report.append("    BaseUrl       : " + getBaseUrl() + "\n");
        report.append("    Headers       : __\n"); // Map Tika Metadata to
                                                   // individual string entries
        for (String header : getHeaders().names()) {
            String mdString = getHeaders().get(header) + Arrays.toString(getHeaders().getValues(header));
            report.append("                   " + mdString + "\n");
        }
        report.append("    StatusCode    : " + getStatusCode() + "\n");
        report.append("    ReasonPhrase  : " + getReasonPhrase() + "\n");
        report.append("    NumRedirects  : " + getNumRedirects() + "\n");
        report.append("    NewBaseUrl    : " + getNewBaseUrl() + "\n");
        report.append("    HostAddress   : " + getHostAddress() + "\n");
        report.append("    ResponseRate  : " + getResponseRate() + "\n");
        report.append("    PayLoad       : __\n"); // Map Keysets to individual
                                                   // string entries
        for (String payLoad : getPayload().keySet()) {
            String payString = payLoad + getPayload().get(payLoad);
            report.append("                   " + payString + "\n");
        }
        report.append("    FetchTime     : " + getFetchTime() + "\n");
        report.append("    FetchedUrl    : " + getFetchedUrl() + "\n");
        report.append("    ContentType   : " + getContentType() + "\n");
        report.append("    ContentLength : " + getContentLength() + "\n");
        report.append("    Content       : " + new String(getContent(), Charset.defaultCharset()) + "\n"); // byte
                                                                                 // array
                                                                                 // to
                                                                                 // string
        report.append("*********************\n");
        report.append("End of Report:\n");
        return report.toString();

    }
}
