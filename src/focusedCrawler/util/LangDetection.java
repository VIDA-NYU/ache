package focusedCrawler.util;
import java.util.ArrayList;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.Language;
import focusedCrawler.util.Page;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class LangDetection {
    private String[] extList = {".de", ".vn"};

    public LangDetection(){
      //this.extList = {".de", ".vn"};
    }

    public void init(String profileDirectory){
      try{
        DetectorFactory.loadProfile(profileDirectory);
      }
      catch(Exception e)
      {
        System.out.println("Error in detect language");
      }
    }

    public String detect_text(String text){
      try
      {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
      }
      catch(Exception e)
      {
        System.out.println("Error in detect language");
        return null;
      }

    }

    public Boolean detect_page(Page page){
      //Return False if the page is not Enlgish
      //Check the url extension:
      //if (check_ext(page.getURL().getHost())){
      try{
        String text = ArticleExtractor.INSTANCE.getText(page.getContent());
        if (check_content(text)){
          return true;
        }
        else{
          return false;
        }
      }
      catch(Exception e){
        System.out.println("Exception in detect_page");
        return false;
      }
    }
    public Boolean check_content(String content)
    {
      if (detect_text(content).equals("en")){
        return true;
      }
      else{
        return false;
      }
    }

    public Boolean check_ext(String url){
      String ext = url.substring(url.length() - 3);
      for (int i=0; i<extList.length; i++){
        if (ext.equals(extList[i])){
          return false;
        }
      }
      return true;
    }

    public Boolean check_header(String content){
      return true;
    }

    public ArrayList<Language> detectLangs(String text){
      try
      {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
      }
      catch(Exception e)
      {
        System.out.println("Error in detect language");
        return null;
      }     
    }
}
