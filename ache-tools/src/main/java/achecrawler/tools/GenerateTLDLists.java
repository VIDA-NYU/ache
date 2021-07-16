package achecrawler.tools;

import achecrawler.crawler.async.fetcher.OkHttpFetcher;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.http.UserAgent;
import achecrawler.crawler.crawlercommons.fetcher.http.UserAgent.Builder;
import achecrawler.util.CliTool;
import io.airlift.airline.Command;
import java.net.IDN;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.DomainValidator.ArrayType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Command(name = "GenerateTLDLists", description = "")
public class GenerateTLDLists extends CliTool {

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new GenerateTLDLists());
    }

    @Override
    public void execute() throws Exception {

        UserAgent userAgent = new Builder().setAgentName("ACHE").build();
        OkHttpFetcher fetcher = new OkHttpFetcher(userAgent);
        fetcher.setDefaultMaxContentSize(10 * 1024 * 1024);
        FetchedResult result = fetcher.get("https://www.iana.org/domains/root/db");
        String html = new String(result.getContent());

        Map<String, TreeSet<String>> tlds = extractTLDsFromHTML(result, html);

        checkCategoryExists(tlds, "country-code");
        checkCategoryExists(tlds, "infrastructure");
        checkCategoryExists(tlds, "test");
        checkCategoryExists(tlds, "sponsored");
        checkCategoryExists(tlds, "generic");

        System.out.println();

        System.out.println("// Recent Country Code TLDs absent in commons-validator");
        printMissing("recentCountryCodeTLDs", tlds.get("country-code"), ArrayType.COUNTRY_CODE_RO);

        System.out.println("// Recent Generic TLDs absent in commons-validator");
        printMissing("recentGenericTLDs", tlds.get("generic"), ArrayType.GENERIC_RO);

        System.out.println("// Recent Sponsored TLDs absent in commons-validator");
        // sponsored category is stored in array type GENERIC_RO in commons-validator
        printMissing("recentSponsoredTLDs", tlds.get("sponsored"), ArrayType.GENERIC_RO);
    }

    private Map<String, TreeSet<String>> extractTLDsFromHTML(FetchedResult result, String html) {
        Document dom = Jsoup.parse(html, result.getFetchedUrl());

        Map<String, TreeSet<String>> tlds = new HashMap<>();
        Elements tr = dom.select("#tld-table tbody > tr");
        for (Element element : tr) {
            Elements children = element.children();
            if (children.size() != 3) {
                System.err.println(
                        "WARN: Found a table row (tr) not with 3 children. The HTML template may have changed.");
                continue;
            }
            String tld = children.get(0).text();
            String type = children.get(1).text();

            tld = normalizeTld(tld);
            if (tld == null) {
                continue;
            }

            TreeSet<String> tldList = tlds.get(type);
            if (tldList == null) {
                tldList = new TreeSet<>();
                tlds.put(type, tldList);
            }

            tldList.add(tld);
        }

        System.out.println("Found TLDs per category:");
        for (Map.Entry<String, TreeSet<String>> entry : tlds.entrySet()) {
            String type = entry.getKey();
            System.out.print(type + ": ");
            System.out.println(entry.getValue().size());
        }
        return tlds;
    }

    private String normalizeTld(final String tld) {
        int lastChar = tld.length() - 1;
        if (!(
                (tld.charAt(0) == '.') ||
                        (tld.charAt(0) == '\u200F' && tld.charAt(1) == '.'
                                && tld.charAt(lastChar) == '\u200E')
        )
                ) {
            System.err.printf("WARN: Found a TLD without leading dot: [%s]."
                    + " The HTML template may have changed.\n", tld);
        }
        String normalized = null;
        if (tld.charAt(0) == '\u200F' && tld.charAt(1) == '.' && tld.charAt(lastChar) == '\u200E') {
            normalized = tld.substring(2, tld.length() - 1);
        }
        if (tld.charAt(0) == '.') {
            normalized = tld.substring(1);
        }
        try {
            normalized = IDN.toASCII(normalized);
        } catch (Exception e) {
            System.err.printf("WARN: Failed to convert normalized string [%s]"
                    + " from TLD [%s] to punnycode.\n", normalized, tld);
            return null;
        }
        return normalized;
    }

    private void printMissing(String variableName, TreeSet<String> tlds, ArrayType tldType) {
        List<String> countryCode = Arrays.asList(DomainValidator.getTLDEntries(tldType));
        StringJoiner str = new StringJoiner(",");
        for (String tld : tlds) {
            if (!countryCode.contains(tld)) {
                str.add("\n\"" + tld + "\"");
            }
        }
        System.out.printf("String[] " + variableName + " = new String[]{" + str.toString() + "};\n\n");
    }

    private void checkCategoryExists(Map<String, TreeSet<String>> tlds, String tldCategory) {
        if (!tlds.containsKey(tldCategory)) {
            System.out.println("WARN: TLD category not found: " + tldCategory
                    + ". Site template may have changed.");
        }
    }

}
