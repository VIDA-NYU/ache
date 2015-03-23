package focusedCrawler.target.detector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
/**
* Detect whether a site is relevant to a topic.
* TODO: This is a proof of concept class. This should be abstract: Create an AbstractSiteDetector, then TitlleDetector, etc.
*/

public class SiteDetector{

	private Pattern pattern;
	private Pattern title_pattern;

	public SiteDetector(){
		pattern = Pattern.compile(".*(ebola).*", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        title_pattern = Pattern.compile("\\<title>(.*?)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    }

	public SiteDetector(String regex){
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        title_pattern = Pattern.compile("\\<title>(.*?)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
	}

    public String get_title(String source){
        String title = null;
        Matcher title_matcher = this.title_pattern.matcher(source);
        if (title_matcher.find()){
            title = title_matcher.group(1);
        }

        return title;
    }

    public boolean detect(String source) {
        return detect_by_title(source);
    }

	public boolean detect_by_title(String source){
        System.out.println("Size: " + source.length());
        String title = this.get_title(source);
        if (title != null){
            System.out.println(title);
		    Matcher matcher = this.pattern.matcher(title);
            if (matcher.matches())
			    return true;
		    else
			    return false;
        }
        else
            return false;
		
	}

	public static void main(String[] args){
        try{
	    	SiteDetector sd = new SiteDetector();
            File file = new File(args[0]);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            String s = new String(data, "UTF-8");
            boolean res = sd.detect(s);
            System.out.println("Result: " + res);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
	}
}
