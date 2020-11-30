package achecrawler.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

public class LangDetectionTest {
    
    static LangDetection langDetect;
    
    @BeforeClass
    public static  void setUpClass() {
        langDetect = new LangDetection();
//        langDetect.init("libs/profiles/");
    }

    @Test
    public void shouldDetectEnglishLanguage() {
        // given
        String textInEnglish = "This text is written in plain regular english language";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textInEnglish);
        
        // then
        assertThat(isEnglish, is(true));
    }
    
    @Test
    public void shouldDetectWhenTextIsNotEnglishLanguage() {
        // given
        String textNotInEnglish = "Este texto está escrito em lingua portuguesa.";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }
    
    @Test
    public void shouldReturnFalseForNull() {
        // given
        String textNotInEnglish = null;
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }
    
    @Test
    public void shouldReturnFalseForEmptyString() {
        // given
        String textNotInEnglish = "";
        
        // when
        Boolean isEnglish = langDetect.isEnglish(textNotInEnglish);
        
        // then
        assertThat(isEnglish, is(false));
    }


}