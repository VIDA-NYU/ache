package achecrawler.util;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.Language;

import achecrawler.target.model.Page;

public class LangDetection {
    
    private static final Logger logger = LoggerFactory.getLogger(LangDetection.class);
    
    /**
     *  Loads language profiles from resources folder
     */
    static {
        String[] languages = { "af", "ar", "bg", "bn", "cs", "da", "de", "el", "en", "es", "et",
                "fa", "fi", "fr", "gu", "he", "hi", "hr", "hu", "id", "it", "ja", "kn", "ko", "lt",
                "lv", "mk", "ml", "mr", "ne", "nl","no", "pa", "pl", "pt", "ro", "ru", "sk", "sl",
                "so", "sq", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi", "zh-cn",
                "zh-tw" };
        try {
            List<String> profiles = new ArrayList<String>();
            
            for (String language : languages) {
                String filename = "profiles/"+language;
                InputStream is = LangDetection.class.getClassLoader().getResourceAsStream(filename);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String jsonProfile = br.readLine();
                profiles.add(jsonProfile);
            }
            
            DetectorFactory.loadProfile(profiles);
            
        } catch (Exception e) {
            throw new IllegalStateException("Could not load language profiles.");
        }
    }

    /**
     * Try to detect the language of the text in the String.
     * 
     * @param page
     * @return true if the String contains English language, false otherwise
     */
    public Boolean isEnglish(String content) {
        try {

            if (content == null || content.isEmpty()) {
                return false;
            }

            Detector detector = DetectorFactory.create();
            detector.append(content);
            ArrayList<Language> langs = detector.getProbabilities();

            if (langs.size() == 0) {
                return false;
            }

            for (Language l : langs) {
                if (l.lang.equals("en")) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            logger.warn("Problem while detecting language in text: " + content, ex);
            return false;
        }
    }

    /**
     * Try to detect the language of contents of the page.
     * 
     * @param page
     * @return true if the page is in English language, false otherwise
     */
    public Boolean isEnglish(Page page) {
        try {
            return isEnglish(page.getParsedData().getCleanText());
        } catch (Exception e) {
            System.out.println("Exception in detect_page");
            return false;
        }
    }

}
