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

public enum AbortedFetchReason {
    // WARNING - adding new reasons requires changes to
    // AbortedFetchException.mapToUrlStatus

    SLOW_RESPONSE_RATE, // Response rate back from server was below minimum.
    INVALID_MIMETYPE, // FetcherPolicy doesn't specify this as a valid mime-type
    INTERRUPTED, // Fetch was interrupted (typically by FetchBuffer calling
                 // executor.terminate())
    CONTENT_SIZE, // Content exceeds Fetcher.getMaxContentSize()
}
