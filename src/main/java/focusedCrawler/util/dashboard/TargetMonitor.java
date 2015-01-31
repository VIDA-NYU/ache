package focusedCrawler.util.dashboard;
import java.util.List;
import java.lang.String;
import java.io.*;

public class TargetMonitor
{
	private PrintWriter fCrawledPages;
	private PrintWriter fRelevantPages;
	private PrintWriter fNonRelevantPages;
	private PrintWriter fHarvestInfo;
	private PrintWriter fPointer;

  public TargetMonitor(String fileCrawledPages, String fileRelevantPages, String fileHarvestInfo, String fileNonRelevantPages)
	{
		try
		{
  		//Should we reopen then close file each team we call export function?
  		fCrawledPages = new PrintWriter(fileCrawledPages, "UTF-8");
  		fRelevantPages = new PrintWriter(fileRelevantPages, "UTF-8");
  		fHarvestInfo = new PrintWriter(fileHarvestInfo, "UTF-8");
  		fNonRelevantPages = new PrintWriter(fileNonRelevantPages, "UTF-8");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void export(List<String> list)
	{
		 for(String item: list)
		{
			fPointer.println(item);
		} 
		fPointer.flush();
	}

  public void exportHarvestInfo(List<String> list)
  {
		this.fPointer = this.fHarvestInfo;
		export(list);
  }

  public void exportCrawledPages(List<String> list)
  {
		this.fPointer = fCrawledPages;
		export(list);
  }

  public void exportRelevantPages(List<String> list)
  {
		this.fPointer = this.fRelevantPages;
		export(list);
  }

  public void exportNonRelevantPages(List<String> list)
  {
		this.fPointer = this.fNonRelevantPages;
		export(list);
  }

  static public void main(String[] args) {
    //Test here
  }
}
