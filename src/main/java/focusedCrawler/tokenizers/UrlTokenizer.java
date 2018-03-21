package focusedCrawler.tokenizers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class UrlTokenizer implements Tokenizer {

    public List<String> tokenize(String urlStr) {
        try {
            Set<String> terms = new TreeSet<>();
            URL urlObj = new URL(urlStr);
            String query = urlObj.getQuery();
            if (query != null && query.length() > 0) {
                String[] split = query.split("&");
                for (int i = 0; i < split.length; i++) {
                    terms.add(split[i]);
                    String[] kv = split[i].split("=");
                    if (kv != null && kv.length > 0) {
                        terms.add(kv[0] + "=");
                    }
                }
            }
            String file = urlObj.getFile();
            if (file != null && file.length() > 0) {
                String[] split = file.split("\\?");
                if (split.length > 0) {
                    terms.add(split[0]);
                }
            }
            return new ArrayList<>(terms);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid url found: " + urlStr, e);
        }
    }

}
