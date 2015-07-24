package focusedCrawler.util.dashboard;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class LinkMonitor {
	
	private Path fPath;
	private PrintWriter fOutLinks;

	public LinkMonitor(Path dataMonitorDirectory) {
		File dataMonitorDir = dataMonitorDirectory.toFile();
		if (!dataMonitorDir.exists()) {
			dataMonitorDir.mkdirs();
		}
		this.fPath = dataMonitorDirectory.resolve("frontierpages.csv");
		try {
			Path outlinksPath = dataMonitorDirectory.resolve("outlinks.csv");
			fOutLinks = new PrintWriter(outlinksPath.toFile(), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void exportOutLinks(List<String> list) {
		for (String item : list) {
			fOutLinks.println(item);
		}
		fOutLinks.flush();
	}

	public void exportFrontierPages(List<String> list) {
		try {
			FileWriter f = new FileWriter(fPath.toFile(), false);
			for (String item : list) {
				f.write(item + "\n");
			}
			f.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
