package focusedCrawler.util;

import java.io.IOException;
import java.util.List;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

public class LangDetection {

    private static LanguageDetector languageDetector;
    private static TextObjectFactory textObjectFactory;

    static {
        try {
            // load all languages
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

            // build language detector
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles).build();

            // create a text object factory
            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

        } catch (IOException e) {
            throw new RuntimeException("Problem while initializing language detector.");
        }
    }

    public Boolean isEnglish(String content) {
        try {
            if(content == null || content.isEmpty()) {
                return false;
            }
            
            TextObject text = textObjectFactory.forText(content);

            List<DetectedLanguage> langs = languageDetector.getProbabilities(text);
            if (langs.size() == 0) {
                return false;
            }
            for (DetectedLanguage lang : langs) {
                if (lang.getLocale().getLanguage().equals("en")) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            throw new RuntimeException("Problem while detecting language in text: " + content);
        }
    }

    public Boolean isEnglish(Page page) {
        // Return False if the page is not Enlgish
        try {
            String content = page.getCleanContent();
            return isEnglish(content);
        } catch (Exception e) {
            throw new RuntimeException("Problem while detecting language in Page: " + page.getIdentifier());
        }
    }
    
}
