package focusedCrawler.util;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

import focusedCrawler.util.Page;

import java.util.regex.Pattern;

public class LangDetection {
    private String[] extList = {".de", ".vn"};
    private int maxHeaderSize;
    private Pattern langPattern;
    private Pattern titlePattern;
    public LangDetection(){
      maxHeaderSize = 20;
      langPattern = Pattern.compile("lang=\"(.*?)\"");
      titlePattern = Pattern.compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);      
    }

    public void init(String profileDirectory){
      try{
        DetectorFactory.loadProfile(profileDirectory);
      }
      catch(Exception e){
        System.out.println("Error in detect language");
      }
    }
    
    public void init(URI profileURI){
    	try {
			DetectorFactory.loadProfile(new File(profileURI));
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in detect language");
			e.printStackTrace();
		}
    }

    public Boolean detect_text(String content){
        try {

            Detector detector = DetectorFactory.create();
            detector.append(content);
            ArrayList<Language> langs = detector.getProbabilities();
            if (langs.size() == 0){
                return false;
            }
            for (Language l: langs){
                if (l.lang.equals("en"))
                    return true;
            }
            return false;
        } 
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println(">>>>Exception in language detection");
            return false;
        }
    }

    public Boolean detect_page(Page page){
      //Return False if the page is not Enlgish
      try {
        String content = page.getCleanContent();
        return detect_text(content);
      }
      catch(Exception e){
        System.out.println("Exception in detect_page");
        return false;
      }
    }
    
    public static void main(String[] args)
    {
      LangDetection ld = new LangDetection();
    }
}
