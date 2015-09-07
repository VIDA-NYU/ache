package focusedCrawler.util;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;

public class YMLParameterFileTest {
    
    public YMLParameterFileTest() {
        
    }

    @Test
    public void parseYMLFile() throws FileNotFoundException, YamlException {
        
        ParameterFile yFile = new ParameterFile("/Users/rajatpawar/Documents/acheLibChange/master_2/ache/config/sample_model/pageclassifier.features");
        Iterator it = yFile.getParameters();
//        System.out.println(yFile.getParam("ATTRIBUTES"));
        
        int count=0;
        while(it.hasNext()){
         String nextVal = (String)it.next();
            System.out.println( nextVal + " -> " + yFile.getParam(nextVal));
            
         count++;
        }
        System.out.println("total " + count + " numbers.");
    }
}
