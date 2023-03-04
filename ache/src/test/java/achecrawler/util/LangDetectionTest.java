package achecrawler.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LangDetectionTest {
    
    static LangDetection langDetect;

    @BeforeAll
    static  void setUpClass() {
        langDetect = new LangDetection();
//        langDetect.init("libs/profiles/");
    }

    @Test
    void shouldDetectEnglishLanguage() {
        // given
        String textInEnglish = "This text is written in plain regular english language";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textInEnglish);
        
        // then
        assertThat(isEnglish, is(true));
    }

    @Test
    void shouldDetectWhenTextIsNotEnglishLanguage() {
        // given
        String textNotInEnglish = "Este texto está escrito em lingua portuguesa.";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }

    @Test
    void shouldReturnFalseForNull() {
        // given
        String textNotInEnglish = null;
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }

    @Test
    void shouldReturnFalseForEmptyString() {
        // given
        String textNotInEnglish = "";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }


}