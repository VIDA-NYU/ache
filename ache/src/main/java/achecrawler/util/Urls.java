package achecrawler.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.DomainValidator.ArrayType;
import org.apache.commons.validator.routines.UrlValidator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import achecrawler.crawler.crawlercommons.filters.basic.BasicURLNormalizer;
import okhttp3.HttpUrl;

public class Urls {

    static {
        // This static block updates the list of top-level domain names from the commons-validator
        // library, which is used to verify if URLs are valid.
        // To update this arrays, the tool GenerateTLDLists can be used to automatically find the
        // new TLDs not included in default list.

        // Recent Country Code TLDs absent in commons-validator
        String[] recentCountryCodeTLDs = new String[]{
                "an",
                "bl",
                "bq",
                "eh",
                "mf",
                "ss",
                "tp",
                "um",
                "xn--2scrj9c",
                "xn--3hcrj9c",
                "xn--45br5cyl",
                "xn--90ae",
                "xn--h2breg3eve",
                "xn--h2brj9c8c",
                "xn--mgbah1a3hjkrd",
                "xn--mgbai9azgqp6j",
                "xn--mgbbh1a",
                "xn--mgbgu82a",
                "xn--rvc1e0am3e"};

        // Recent Generic TLDs absent in commons-validator
        String[] recentGenericTLDs = new String[]{
                "africa",
                "arab",
                "charity",
                "doosan",
                "etisalat",
                "flsmidth",
                "grocery",
                "hotels",
                "iinet",
                "inc",
                "llc",
                "map",
                "merckmsd",
                "mutuelle",
                "phd",
                "rugby",
                "search",
                "sport",
                "xn--mgbaakc7dvf",
                "xn--ngbrx",
                "xn--otu796d"};

        // Recent Sponsored TLDs absent in commons-validator
        String[] recentSponsoredTLDs = new String[]{};

        //# END

        // Create TLD arrays
        List<String> newGenericDomains = new ArrayList<>();
        newGenericDomains.addAll(Arrays.asList(recentGenericTLDs));
        newGenericDomains.addAll(Arrays.asList(recentSponsoredTLDs));
        newGenericDomains.add("onion"); // we also want accept links from to TOR network
        String[] gericPlusArray = newGenericDomains.toArray(new String[newGenericDomains.size()]);
        // Finally, update commons-validator
        DomainValidator.updateTLDOverride(ArrayType.GENERIC_PLUS, gericPlusArray);
        DomainValidator.updateTLDOverride(ArrayType.COUNTRY_CODE_PLUS, recentCountryCodeTLDs);
    }

    private static final String[] ALLOWED_SCHEMES = {"http", "https"};

    private static final UrlValidator VALIDATOR = new UrlValidator(ALLOWED_SCHEMES);

    private static final List<String> INVALID_QUERY_PARAMETERS = Arrays.asList(
            "sid",
            "phpsessid",
            "sessionid",
            "jsessionid");

    private static final BasicURLNormalizer URL_NORMALIZER =
            new BasicURLNormalizer(new TreeSet<>(INVALID_QUERY_PARAMETERS), false);


    public static boolean isValid(String url) {
        return VALIDATOR.isValid(url);
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
