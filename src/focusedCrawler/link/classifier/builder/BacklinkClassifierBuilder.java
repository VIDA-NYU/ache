package focusedCrawler.link.classifier.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import focusedCrawler.util.parser.SimpleWrapper;
import focusedCrawler.link.classifier.builder.BacklinkSurfer;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.util.ClassifierRecreator;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;


public class BacklinkClassifierBuilder {

		private BacklinkSurfer surfer;
		private String outputFile;
		private String[] urls;
		private StopList stoplist;
		private int levels;
		
		public BacklinkClassifierBuilder(ParameterFile config, String outputFile) throws IOException{
			this.outputFile = outputFile;
		    FileWriter out = new FileWriter(config.getParam("OUTPUT_FIELDS"));
		    StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
		    SimpleWrapper wrapper = new SimpleWrapper(config.getParam("PATTERN_INI"),
		                                                config.getParam("PATTERN_END"));
		    int conTimeout = config.getParamInt("CONNECT_TIMEOUT");
		    int numBacklink = config.getParamInt("NUM_BACKLINK");
		    int readTimeout = config.getParamInt("READ_TIMEOUT");
		    this.surfer = new BacklinkSurfer(stoplist, wrapper,
		                                                 config.getParam("BACKLINK"),
		                                                 out, conTimeout, readTimeout,
		                                                 numBacklink);
		    this.urls = config.getParam("INITIAL_URLS", " ");
		    this.levels = config.getParamInt("DEEP_BACKLINK");
		    this.stoplist = stoplist;
		}
		
		public void execute() throws MalformedURLException, IOException{
			System.out.println(">>>Expanding frontier...");
			surfer.surfingBackwards(urls,0,levels);
			Vector[] lns = surfer.getLNs();
			for (int i = 0; i < lns.length; i++) {
				System.out.println("LEVEL:"+i);
				Vector<LinkNeighborhood> ln = lns[i];
				System.out.println("SIZE:"+ln.size());
//				for (int j = 0; j < ln.size(); j++) {
//					System.out.println(ln.elementAt(j).toString());
//				}
			}
			WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
			ClassifierRecreator classifierBuilder = new ClassifierRecreator(wrapper, stoplist);
			classifierBuilder.execute(lns, 3, new File(outputFile));			
		}

		public static void main(String[] args) {
			try {
				ParameterFile config = new ParameterFile(args[0]);
				BacklinkClassifierBuilder builder = new BacklinkClassifierBuilder(config, args[1]);
				builder.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
}
