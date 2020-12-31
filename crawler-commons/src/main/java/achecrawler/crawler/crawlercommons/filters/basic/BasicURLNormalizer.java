package achecrawler.crawler.crawlercommons.filters.basic;

/**
 * Copyright 2016 Crawler-Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Code borrowed from Apache Nutch. Converts URLs to a normal form:
 * <ul>
 * <li>remove dot segments in path: <code>/./</code> or <code>/../</code></li>
 * <li>remove default ports, e.g. 80 for protocol <code>http://</code></li>
 * <li>normalize <a href=
 * "https://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_in_a_URI">
 * percent-encoding</a> in URL paths</li>
 * </ul>
 */
public class BasicURLNormalizer {
	
    public static final Logger LOG = LoggerFactory.getLogger(BasicURLNormalizer.class);

    /**
     * Pattern to detect whether a URL path could be normalized. Contains one of
     * /. or ./ /.. or ../ //
     */
    private final static Pattern hasNormalizablePathPattern = Pattern.compile("/[./]|[.]/");

    /**
     * Nutch 1098 - finds URL encoded parts of the URL
     */
    private final static Pattern unescapeRulePattern = Pattern.compile("%([0-9A-Fa-f]{2})");

    // charset used for encoding URLs before escaping
    private final static Charset utf8 = Charset.forName("UTF-8");
    
    private static final Pattern thirtytwobithash = Pattern.compile("[a-fA-F\\d]{32}");

    /** look-up table for characters which should not be escaped in URL paths */
    private final static boolean[] unescapedCharacters = new boolean[128];
    
    private final static Comparator<NameValuePair> parametersComparator = new Comparator<NameValuePair>() {
    	@Override
    	public int compare(NameValuePair p1, NameValuePair p2) {
    		return p1.getName().compareTo(p2.getName());
    	}
    };

    static {
        for (int c = 0; c < 128; c++) {
            /*
             * https://tools.ietf.org/html/rfc3986#section-2.2 For consistency,
             * percent-encoded octets in the ranges of ALPHA (%41-%5A and
             * %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E), underscore
             * (%5F), or tilde (%7E) should not be created by URI producers and,
             * when found in a URI, should be decoded to their corresponding
             * unreserved characters by URI normalizers.
             */
            if ((0x41 <= c && c <= 0x5A) || (0x61 <= c && c <= 0x7A) || (0x30 <= c && c <= 0x39) || c == 0x2D || c == 0x2E || c == 0x5F || c == 0x7E) {
                unescapedCharacters[c] = true;
            } else {
                unescapedCharacters[c] = false;
            }
        }
    }
    
    final Set<String> queryElementsToRemove;
    final boolean removeHashes;
    
    public BasicURLNormalizer() {
    	this(new TreeSet<>(), false);
    }
    
    public BasicURLNormalizer(Set<String> queryElementsToRemove, boolean removeHashes) {
    	this.queryElementsToRemove = new TreeSet<>(queryElementsToRemove);
    	this.removeHashes = removeHashes;
    }

    public String filter(String urlString) {

        if ("".equals(urlString)) // permit empty
            return urlString;

        urlString = urlString.trim(); // remove extra spaces
        
        urlString = processQueryElements(urlString);

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOG.info("Malformed URL {}", urlString);
            return null;
        }


        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String file = url.getFile();

        boolean changed = false;

        if (!urlString.startsWith(protocol)) // protocol was lowercased
            changed = true;

        if ("http".equals(protocol) || "https".equals(protocol) || "ftp".equals(protocol)) {

            if (host != null) {
                String newHost = host.toLowerCase(Locale.ROOT); // lowercase
                                                                // host
                if (!host.equals(newHost)) {
                    host = newHost;
                    changed = true;
                }
            }

            if (port == url.getDefaultPort()) { // uses default port
                port = -1; // so don't specify it
                changed = true;
            }

            if (file == null || "".equals(file)) { // add a slash
                file = "/";
                changed = true;
            }

            if (url.getRef() != null) { // remove the ref
                changed = true;
            }

            // check for unnecessary use of "/../", "/./", and "//"
            String file2 = null;
            try {
                file2 = getFileWithNormalizedPath(url);
            } catch (MalformedURLException e) {
                LOG.info("Malformed URL {}", url);
                return null;
            }
            if (!file.equals(file2)) {
                changed = true;
                file = file2;
            }
            
            
        }

        // properly encode characters in path/file using percent-encoding
        String file2 = unescapePath(file);
        file2 = escapePath(file2);
        if (!file.equals(file2)) {
            changed = true;
            file = file2;
        }

        if (changed) {
            try {
                urlString = new URL(protocol, host, port, file).toString();
            } catch (MalformedURLException e) {
                LOG.info("Malformed URL {}{}{}{}", protocol, host, port, file);
                return null;
            }
        }
        
        return urlString;
    }
    
    /**
     * Basic filter to remove query parameters from urls so parameters that
     * don't change the content of the page can be removed. An example would be
     * a google analytics query parameter like "utm_campaign" which might have
     * several different values for a url that points to the same content. This
     * is also called when removing attributes where the value is a hash.
     */
    private String processQueryElements(String urlToFilter) {
        try {
            // Handle illegal characters by making a url first
            // this will clean illegal characters like |
            URL url = new URL(urlToFilter);

            String path = url.getPath();
            String query = url.getQuery();

            // check if the last element of the path contains parameters
            // if so convert them to query elements
            if (path.contains(";")) {
                String[] pathElements = path.split("/");
                String last = pathElements[pathElements.length - 1];
                // replace last value by part without params
                int semicolon = last.indexOf(";");
                if (semicolon != -1) {
                    pathElements[pathElements.length - 1] = last.substring(0,
                            semicolon);
                    String params = last.substring(semicolon + 1).replaceAll(
                            ";", "&");
                    if (query == null) {
                        query = params;
                    } else {
                        query += "&" + params;
                    }
                    // rebuild the path
                    StringBuilder newPath = new StringBuilder();
                    for (String p : pathElements) {
                        if (StringUtils.isNotBlank(p)) {
                            newPath.append("/").append(p);
                        }
                    }
                    path = newPath.toString();
                }
            }

            if (StringUtils.isEmpty(query)) {
                return urlToFilter;
            }

            List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
            Iterator<NameValuePair> pairsIterator = pairs.iterator();
            while (pairsIterator.hasNext()) {
                NameValuePair param = pairsIterator.next();
                if (queryElementsToRemove.contains(param.getName())) {
                    pairsIterator.remove();
                } else if (removeHashes && param.getValue() != null) {
                    Matcher m = thirtytwobithash.matcher(param.getValue());
                    if (m.matches()) {
                        pairsIterator.remove();
                    }
                }
            }

            StringBuilder newFile = new StringBuilder();
            if (StringUtils.isNotBlank(path)) {
                newFile.append(path);
            }
            if (!pairs.isEmpty()) {
                Collections.sort(pairs, parametersComparator);
                String newQueryString = URLEncodedUtils.format(pairs,
                        StandardCharsets.UTF_8);
                newFile.append('?').append(newQueryString);
            }
            if (url.getRef() != null) {
                newFile.append('#').append(url.getRef());
            }

            return new URL(url.getProtocol(), url.getHost(), url.getPort(),
                    newFile.toString()).toString();
        } catch (MalformedURLException e) {
            LOG.warn("Invalid urlToFilter {}. {}", urlToFilter, e);
            return null;
        }
    }

    private String getFileWithNormalizedPath(URL url) throws MalformedURLException {
        String file;

        if (hasNormalizablePathPattern.matcher(url.getPath()).find()) {
            // only normalize the path if there is something to normalize
            // to avoid needless work
            try {
                file = url.toURI().normalize().toURL().getFile();
                // URI.normalize() does not normalize leading dot segments,
                // see also http://tools.ietf.org/html/rfc3986#section-5.2.4
                int start = 0;
                while (file.startsWith("/../", start)) {
                    start += 3;
                }
                if (start > 0) {
                    file = file.substring(start);
                }
            } catch (URISyntaxException e) {
                file = url.getFile();
            }
        } else {
            file = url.getFile();
        }

        // if path is empty return a single slash
        if (file.isEmpty()) {
            file = "/";
        }

        return file;
    }

    /**
     * Remove % encoding from path segment in URL for characters which should be
     * unescaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>.
     */
    private String unescapePath(String path) {
        StringBuilder sb = new StringBuilder();

        Matcher matcher = unescapeRulePattern.matcher(path);

        int end = -1;
        int letter;

        // Traverse over all encoded groups
        while (matcher.find()) {
            // Append everything up to this group
            sb.append(path.substring(end + 1, matcher.start()));

            // Get the integer representation of this hexadecimal encoded
            // character
            letter = Integer.valueOf(matcher.group().substring(1), 16);

            if (letter < 128 && unescapedCharacters[letter]) {
                // character should be unescaped in URLs
                sb.append(new Character((char) letter));
            } else {
                // Append the encoded character as uppercase
                sb.append(matcher.group().toUpperCase(Locale.ROOT));
            }

            end = matcher.start() + 2;
        }

        letter = path.length();

        // Append the rest if there's anything
        if (end <= letter - 1) {
            sb.append(path.substring(end + 1, letter));
        }

        // Ok!
        return sb.toString();
    }

    /**
     * Convert path segment of URL from Unicode to UTF-8 and escape all
     * characters which should be escaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>..
     */
    private String escapePath(String path) {
        StringBuilder sb = new StringBuilder(path.length());

        // Traverse over all bytes in this URL
        for (byte b : path.getBytes(utf8)) {
            // Is this a control character?
            if (b < 33 || b == 91 || b == 93) {
                // Start escape sequence
                sb.append('%');

                // Get this byte's hexadecimal representation
                String hex = Integer.toHexString(b & 0xFF).toUpperCase(Locale.ROOT);

                // Do we need to prepend a zero?
                if (hex.length() % 2 != 0) {
                    sb.append('0');
                    sb.append(hex);
                } else {
                    // No, append this hexadecimal representation
                    sb.append(hex);
                }
            } else {
                // No, just append this character as-is
                sb.append((char) b);
            }
        }

        return sb.toString();
    }

}
