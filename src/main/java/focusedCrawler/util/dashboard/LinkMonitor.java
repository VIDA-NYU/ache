package focusedCrawler.util.dashboard;
import java.util.List;
import java.lang.String;
import java.io.*;

public class LinkMonitor
{
	private String fPath;
	private PrintWriter fOutLinks;
	
  public LinkMonitor(String fileFrontierPages, String fileOutLinks)
	{
		try
		{
			fPath = fileFrontierPages;
			fOutLinks = new PrintWriter(fileOutLinks, "UTF-8");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void exportOutLinks(List<String> list)
	{
		for(String item: list)
			fOutLinks.println(item);
		fOutLinks.flush();
	}

  public void exportFrontierPages(List<String> list)
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
  
  static public void main(String[] args) {
    //Test here
  }
}
