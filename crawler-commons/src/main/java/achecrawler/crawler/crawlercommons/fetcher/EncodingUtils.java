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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtils.class);

    private static final int EXPECTED_GZIP_COMPRESSION_RATIO = 5;
    private static final int EXPECTED_DEFLATE_COMPRESSION_RATIO = 5;
    private static final int BUF_SIZE = 4096;

    public static class ExpandedResult {
        private byte[] _expanded;
        private boolean _isTruncated;

        public ExpandedResult(byte[] expanded, boolean isTruncated) {
            super();
            _expanded = expanded;
            _isTruncated = isTruncated;
        }

        public byte[] getExpanded() {
            return _expanded;
        }

        public void setExpanded(byte[] expanded) {
            _expanded = expanded;
        }

        public boolean isTruncated() {
            return _isTruncated;
        }

        public void setTruncated(boolean isTruncated) {
            _isTruncated = isTruncated;
        }
    }

    public static byte[] processGzipEncoded(byte[] compressed) throws IOException {
        return processGzipEncoded(compressed, Integer.MAX_VALUE).getExpanded();
    }

    public static ExpandedResult processGzipEncoded(byte[] compressed, int sizeLimit) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_GZIP_COMPRESSION_RATIO * compressed.length);
        GZIPInputStream inStream = new GZIPInputStream(new ByteArrayInputStream(compressed));

        boolean isTruncated = false;
        byte[] buf = new byte[BUF_SIZE];
        int written = 0;
        while (true) {
            try {
                int size = inStream.read(buf);
                if (size <= 0) {
                    break;
                }

                if ((written + size) > sizeLimit) {
                    isTruncated = true;
                    outStream.write(buf, 0, sizeLimit - written);
                    break;
                }

                outStream.write(buf, 0, size);
                written += size;
            } catch (Exception e) {
                LOGGER.trace("Exception unzipping content", e);
                break;
            }
        }

        safeClose(outStream);
        return new ExpandedResult(outStream.toByteArray(), isTruncated);
    }

    // TODO KKr The following routines are designed to support the deflate
    // compression standard (RFC 1250) for HTTP 1.1 (RFC 2616). However,
    // I was unable to verify that they really work correctly, so I've
    // removed deflate from SimpleHttpFetcher.DEFAULT_ACCEPT_ENCODING.

    public static byte[] processDeflateEncoded(byte[] content) throws IOException {
        return processDeflateEncoded(content, Integer.MAX_VALUE);
    }

    public static byte[] processDeflateEncoded(byte[] compressed, int sizeLimit) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_DEFLATE_COMPRESSION_RATIO * compressed.length);

        // "true" because HTTP does not provide zlib headers
        Inflater inflater = new Inflater(true);
        InflaterInputStream inStream = new InflaterInputStream(new ByteArrayInputStream(compressed), inflater);

        byte[] buf = new byte[BUF_SIZE];
        int written = 0;
        while (true) {
            try {
                int size = inStream.read(buf);
                if (size <= 0) {
                    break;
                }

                if ((written + size) > sizeLimit) {
                    outStream.write(buf, 0, sizeLimit - written);
                    break;
                }

                outStream.write(buf, 0, size);
                written += size;
            } catch (Exception e) {
                LOGGER.trace("Exception inflating content", e);
                break;
            }
        }

        safeClose(outStream);
        return outStream.toByteArray();
    }

    private static void safeClose(OutputStream os) {
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException e) {
            LOGGER.warn("IOException closing input stream", e);
        }
    }

}
