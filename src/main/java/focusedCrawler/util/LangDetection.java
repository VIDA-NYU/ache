package focusedCrawler.util;
import java.util.ArrayList;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.Language;
import focusedCrawler.util.Page;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import java.io.*;
import java.util.regex.Matcher;
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

    public String get_title(String source){
        String title = null;
        Matcher title_matcher = this.titlePattern.matcher(source);
        if (title_matcher.find()){
            title = title_matcher.group(1);
        }
        return title;
    }


    public void init(String profileDirectory){
      try{
        DetectorFactory.loadProfile(profileDirectory);
      }
      catch(Exception e){
        System.out.println("Error in detect language");
      }
    }


    public Boolean detectByHeader(String source)
    {
      //Return False if the page is not english
      int max = 5000;
      if (source.length() < max)
        max = source.length();
      String head = source.substring(0, max);
      //System.out.println("NUMBER OF LINES: " + head.split("\n").length);
      try
      {
        //Extract the tag lang in the header of the page. Check if this tag is 'en'
        Matcher lang_match = langPattern.matcher(head);
        if(lang_match.find())
        {
          //System.out.println(m.group(1));
          if (lang_match.group(1).toLowerCase().substring(0, 2).equals("en")){
            return true;
          }
          else{ 
            System.out.println("CODE: " + lang_match.group(1));
            return false;
          }
        }

        return true;
      } catch (Exception ex){
        ex.printStackTrace();
        System.out.println(">>>>Exception in language detection");
        return false;
      }

    }

    public Boolean detectByHeaderAndTitle(String source)
    {
      //Return False if the page is not english
      int max = 5000;
      if (source.length() < max)
        max = source.length();
      String head = source.substring(0, max);
      //System.out.println("NUMBER OF LINES: " + head.split("\n").length);
      try
      {
        //Extract the tag lang in the header of the page. Check if this tag is 'en'
        Matcher lang_match = langPattern.matcher(head);
        if(lang_match.find())
        {
          //System.out.println(m.group(1));
          if (lang_match.group(1).toLowerCase().substring(0, 2).equals("en")){
            return true;
          }
          else{ 
            return false;
          }
        }

        //If can not extract tag lang, extract the title then check if title is in English
        //Note that title is usually short, therefore the detection is prone to error. However extracting text is expensive 
        String title = get_title(head);
        if (title != null){
            if (title.split(" ").length < 20)//langdetect does not work correctly if text is small
                return true;
            Detector detector = DetectorFactory.create();
            detector.append(title);
            ArrayList<Language> langs = detector.getProbabilities();
            if (langs.size() == 0){
                System.out.println("TITLE: " + title);
                return false;
            }
            for (Language l: langs){
                if (l.lang.equals("en"))
                    return true;
            }
            System.out.println("TITLE: " + title);
            return false;
            /*
            ArrayList<Language> langs = detector.detect();
            String lang = detector.detect();
            if (lang.equals("en")){
                return true;
            }
            else{
                return false;
            }*/
        }
        //If both title and lang tag can not be extracted, assume this page is in English
        return true;
      } catch (Exception ex){
        ex.printStackTrace();
        System.out.println(">>>>Exception in language detection");
        return false;
      }
    }

    public Boolean detect_page(Page page){
      //Return False if the page is not Enlgish
      try{
        String source = page.getContent();
        if (detectByHeaderAndTitle(source)){
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
    
    public static void main(String[] args)
    {
      LangDetection ld = new LangDetection();
    }
}
