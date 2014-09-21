package focusedCrawler.util.dashboard;
import java.util.List;
import java.lang.String;
import java.io.*;

public class LinkMonitor
{
	private String fPath;
	
  public LinkMonitor(String fileFrontierPages)
	{
		fPath = fileFrontierPages;

	}

	private void export(List<String> list)
	{
		try
		{
  		FileWriter f = new FileWriter(fPath, false);
			for(String item: list)
			{
				f.write(item + "\n");
			}
			f.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	  	
	}

  public void exportFrontierPages(List<String> list)
  {
		export(list);
  }
  
  static public void main(String[] args) {
    //Test here
  }
}
