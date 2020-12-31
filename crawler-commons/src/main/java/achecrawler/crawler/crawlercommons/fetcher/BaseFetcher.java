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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tika.mime.MediaType;

@SuppressWarnings("serial")
public abstract class BaseFetcher implements Serializable {

    public static final int DEFAULT_MAX_CONTENT_SIZE = 64 * 1024;

    protected Map<String, Integer> _maxContentSizes = new HashMap<String, Integer>();
    protected int _defaultMaxContentSize = DEFAULT_MAX_CONTENT_SIZE;
    protected Set<String> _validMimeTypes = new HashSet<String>();

    public BaseFetcher() {
    }

    public void setDefaultMaxContentSize(int defaultMaxContentSize) {
        _defaultMaxContentSize = defaultMaxContentSize;
    }

    public int getDefaultMaxContentSize() {
        return _defaultMaxContentSize;
    }

    public void setMaxContentSize(String mimeType, int maxContentSize) {
        _maxContentSizes.put(mimeType, maxContentSize);
    }

    public int getMaxContentSize(String mimeType) {
        Integer result = _maxContentSizes.get(mimeType);
        if (result == null) {
            result = getDefaultMaxContentSize();
        }

        return result;
    }

    public Set<String> getValidMimeTypes() {
        return _validMimeTypes;
    }

    public void setValidMimeTypes(Set<String> validMimeTypes) {
        _validMimeTypes = new HashSet<String>(validMimeTypes);
    }

    public void addValidMimeTypes(Set<String> validMimeTypes) {
        _validMimeTypes.addAll(validMimeTypes);
    }

    public void addValidMimeType(String validMimeType) {
        _validMimeTypes.add(validMimeType);
    }

    public FetchedResult get(String url) throws BaseFetchException {
        return get(url, null);
    }

    protected static String getMimeTypeFromContentType(String contentType) {
        String result = "";
        MediaType mt = MediaType.parse(contentType);
        if (mt != null) {
            result = mt.getType() + "/" + mt.getSubtype();
        }

        return result;
    }

    /**
     * Get the content stored in the resource referenced by <url>
     * 
     * @param url
     * @param payload
     * @return
     * @throws BaseFetchException
     */
    public abstract FetchedResult get(String url, Payload payload) throws BaseFetchException;

    /**
     * Terminate any async request being processed.
     * 
     */
    public abstract void abort();

}
