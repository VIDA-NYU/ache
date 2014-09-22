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
    private Pattern searchPattern;
    public LangDetection(){
      //this.extList = {".de", ".vn"};
      maxHeaderSize = 10;
      searchPattern = Pattern.compile("lang=\"(..)\"");
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

    public String detectByBody(String text){
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

    public Boolean detectByHeader(String text)
    {
      try
      {
        BufferedReader bufReader = new BufferedReader(new StringReader(text));
        String line=null;
        int count = 0;
        while( (line=bufReader.readLine()) != null )
        {
          Matcher m = searchPattern.matcher(line);
          if(m.find())
          {
            //System.out.println(m.group(1));
            if (m.group(1).toLowerCase().equals("en"))
            {
              return true;
            }
            else
            {
              return false;
            }
          }
          count += 1;
          if (count == maxHeaderSize)
            break;
        }   
        return true;
      } catch (Exception ex)
      {
        ex.printStackTrace();
        System.out.println(">>>>Exception in language detection");
        return false;
      }
    }

    public Boolean detect_page(Page page){
      //Return False if the page is not Enlgish
      //Check the url extension:
      //if (check_ext(page.getURL().getHost())){
      try{
        //String text = ArticleExtractor.INSTANCE.getText(page.getContent());
        String text = page.getContent();
        if (detectByHeader(text)){
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
      if (detectByHeader(content).equals("en")){
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
    
    public static void main(String[] args)
    {
      LangDetection ld = new LangDetection();
    }
}
