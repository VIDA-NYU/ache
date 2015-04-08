package focusedCrawler.target.detector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import focusedCrawler.util.Page;
/**
* Detect whether a site is relevant to a topic.
*/

public class RegexBasedDetector{

	private Pattern pattern;

	public RegexBasedDetector(){
		pattern = Pattern.compile(".*(ebola).*", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    }

	public RegexBasedDetector(String regex){
        regex = ".*" + regex + ".*";
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

    public String get_title(Page page){
        String title = page.getPageURL().titulo();

        return title;
    }

    public boolean detect(Page page) {
        return detect_by_title(page);
    }

	public boolean detect_by_title(Page page){
        String title = this.get_title(page);
        if (title != null){
            System.out.println(title);
		    Matcher matcher = this.pattern.matcher(title);
            if (matcher.matches()){
			    return true;
			}
		    else {
			    return false;
			}
        }
        else
            return false;
		
	}

	public static void main(String[] args){
        try{
	    	RegexBasedDetector sd = new RegexBasedDetector();
            File file = new File(args[0]);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            String s = new String(data, "UTF-8");
            //boolean res = sd.detect(s);
            //System.out.println("Result: " + res);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
	}
}
