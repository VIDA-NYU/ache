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

import achecrawler.crawler.crawlercommons.util.Headers;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lmcgibbn
 * 
 */
public class FetchedResultTest {
  
    private static final Logger LOG = LoggerFactory.getLogger(FetchedResultTest.class);

    /**
     * Test method for {@link achecrawler.crawler.crawlercommons.fetcher.FetchedResult#report()}.
     * This does not actually test anything but simply allows us to see what a
     * generated report would look like.
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testPrintReport() throws UnsupportedEncodingException {
        Headers headerMetadata = new Headers();
        headerMetadata.add(Headers.CONTENT_DISPOSITION, "This is content disposition");
        headerMetadata.add(Headers.CONTENT_ENCODING, "This is the encoding");
        headerMetadata.add(Headers.CONTENT_LANGUAGE, "This is some language");
        headerMetadata.add(Headers.CONTENT_LENGTH, "This is the length");

        Payload load = new Payload();
        load.put("Item 1", 1234);
        load.put("Item 2", 5678);
        load.put("Item 3", 1357);
        load.put("Item 4", 2468);

        FetchedResult result = new FetchedResult
        // (, , , headers, content, contentType, responseRate, payload,
        // newBaseUrl, numRedirects, hostAddress, statusCode, reasonPhrase)
        (
                        "http://en.wikipedia.org/wiki/Glasgow", // baseUrl
                        "http://en.wikipedia.org/wiki/Glasgow", // redirectedUrl
                        System.currentTimeMillis(), // fetchTime
                        headerMetadata, new String("Glasgow (/ˈɡlɑːzɡoʊ, ˈɡlæz-/;[4] Scots: Glesca; Scottish Gaelic: Glaschu) "
                                        + "is the largest city in Scotland, and the third largest in the United Kingdom.").getBytes("UTF-8"), "ScotsText", 2014, load, "http://en.wikipedia.org/wiki/Glasgow",
                        0, "wikipedia.org", 200, "");
        LOG.error(result.report());
    }
}