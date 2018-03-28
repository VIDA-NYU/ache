package focusedCrawler.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import focusedCrawler.crawler.crawlercommons.filters.basic.BasicURLNormalizer;
import okhttp3.HttpUrl;

public class Urls {

    private static final String[] ALLOWED_SCHEMES = {"http", "https"};

    private static final UrlValidator VALIDATOR = new UrlValidator(ALLOWED_SCHEMES);

    // .onion links aren't accepted by the validator
    // Regex ".[^.]+" --> any string of at least 1 char without dot
    private static final Pattern ONION_PATTERN = Pattern.compile("https?://.[^.]+\\.onion.*");

    private static final List<String> INVALID_QUERY_PARAMETERS = Arrays.asList(
            "sid",
            "phpsessid",
            "sessionid",
            "jsessionid");

    private static final BasicURLNormalizer URL_NORMALIZER =
            new BasicURLNormalizer(new TreeSet<>(INVALID_QUERY_PARAMETERS), false);


    public static boolean isValid(String url) {
        return VALIDATOR.isValid(url) || ONION_PATTERN.matcher(url).matches();
    }

    public static String normalize(String url) {
        return URL_NORMALIZER.filter(url);
    }

    public static String removeFragmentsIfAny(String url) {
        int fragmentPosition = url.indexOf('#');
        if (fragmentPosition != -1) {
            return url.substring(0, fragmentPosition);
        }
        return url;
    }

    public static String resolveHttpLink(HttpUrl base, String link) {
        HttpUrl resolvedUrl;
        try {
            if (base == null) {
                resolvedUrl = HttpUrl.parse(link);
            } else {
                resolvedUrl = base.resolve(link);
            }
        } catch (Exception e) {
            // The link is invalid or malformed
            resolvedUrl = null;
        }
        if (resolvedUrl == null) {
            return null;
        } else {
            return resolvedUrl.toString();
        }
    }

    public static URL toJavaURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provided: " + url, e);
        }
    }

    /**
     * URL deserializer class for use with Jackson library
     * 
     */
    public static class UrlDeserializer extends JsonDeserializer<URL> {
        @Override
        public URL deserialize(JsonParser parser, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode node = parser.getCodec().readTree(parser);
            return new URL(node.asText());
        }
    }

}
