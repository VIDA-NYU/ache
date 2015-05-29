package focusedCrawler.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLIParser {
	
	private Options startCrawlOptions = new Options();
	private Options buildModelOptions = new Options();
	private Options addSeedsOptions = new Options();
	private Options startLinkStorageOptions = new Options();
	private Options startTargetStorageOptions = new Options();
	private Options startCrawlManagerOptions = new Options();
	
	private CommandLineParser parser = new DefaultParser();
	
	public CLIParser() {
		
		startCrawlOptions.addOption("o","outputDir", true, "data output path");
		startCrawlOptions.addOption("c","configDir", true, "config directory path");
		startCrawlOptions.addOption("s","seed", true, "path to the seed file");
		startCrawlOptions.addOption("m","modelDir", true, "model directory path");
		startCrawlOptions.addOption("l","langDetect", true, "path to language detection profile");
		
		buildModelOptions.addOption("c","targetStorageConfig", true, "config file path");
        buildModelOptions.addOption("t","trainingDataDir", true, "training data path");
        buildModelOptions.addOption("o","outputDir", true, "data output path");
        
        addSeedsOptions.addOption("o","outputDir", true, "data output path");
        addSeedsOptions.addOption("c","configDir", true, "config directory path");
        addSeedsOptions.addOption("s","seed", true, "path to the seed file");
        
        startLinkStorageOptions.addOption("o","outputDir", true, "data output path");
        startLinkStorageOptions.addOption("c","configDir", true, "config directory path");
        startLinkStorageOptions.addOption("s","seed", true, "path to the seed file");
        
        startTargetStorageOptions.addOption("o","outputDir", true, "data output path");
        startTargetStorageOptions.addOption("c","configDir", true, "config directory path");
        startTargetStorageOptions.addOption("m","modelDir", true, "model directory path");
        startTargetStorageOptions.addOption("p","profileDir", true, "profile directory path");
        
        startCrawlManagerOptions.addOption("c","configDir", true, "config directory path");
    }
	
	
	 public boolean checkArgs(String commandType, String[] args) {
	    	
		 	try {
	    		CommandLine cmd;
				
	    		if (commandType.equals("buildModel")) cmd = parser.parse(buildModelOptions, args);
	    		else if (commandType.equals("addSeeds")) cmd = parser.parse(addSeedsOptions, args);
	    		else if (commandType.equals("startLinkStorage")) cmd = parser.parse(startLinkStorageOptions, args);
	    		else if (commandType.equals("startTargetStorage")) cmd = parser.parse(startTargetStorageOptions, args);
	    		else cmd = parser.parse(startCrawlOptions, args);
	    		
	    		
	    	if (commandType.equals("buildModel") || commandType.equals("addSeeds") || commandType.equals("startLinkStorage") || commandType.equals("startTargetStorage") || commandType.equals("startCrawl") ) {
	            
				
	    	if ( (cmd.hasOption("outputDir") || cmd.hasOption("o"))) 
	    		return true;
	    	 else 
	    		return false;
	    	
	    	}  else if (commandType.equals("startCrawlManager")) {
	    		
	    		if ( (cmd.hasOption("configDir") || cmd.hasOption("c"))) 
	    		return true;
	    		else 
	    		return false;
	    		
	    	}
	    	
	    	} catch (ParseException e) {
				e.printStackTrace();
				return false;
			}
	    return false;
	    }
	 
	 
	 public String[] getArgs(String commandType, String[] args){
		String arguments[] = new String[5];
		try { 
		
			CommandLine cmd;
			
			
			if (commandType.equals("startCrawl")) {
			
				cmd = parser.parse(startCrawlOptions, args);
				if (cmd.hasOption("outputDir")) arguments[0] = cmd.getOptionValue("outputDir"); 
				else if (cmd.hasOption("o")) arguments[0] = cmd.getOptionValue("o");
				
				if (cmd.hasOption("configDir")) arguments[1] = cmd.getOptionValue("configDir"); 
				else if (cmd.hasOption("c")) arguments[1] = cmd.getOptionValue("c");
				
				if (cmd.hasOption("seed")) arguments[2]=cmd.getOptionValue("seed");
				else if (cmd.hasOption("s")) arguments[2]=cmd.getOptionValue("s");
				
				if (cmd.hasOption("modelDir")) arguments[3] = cmd.getOptionValue("modelDir");
				else if (cmd.hasOption("m")) arguments[3] = cmd.getOptionValue("m");
				
				if (cmd.hasOption("langDetect")) arguments[4] = cmd.getOptionValue("langDetect");
				else if (cmd.hasOption("l")) arguments[4] = cmd.getOptionValue("l");
				
				return arguments;
				
			} else if (commandType.equals("buildModel")) {
		
			cmd = parser.parse(buildModelOptions, args);
				
			if (cmd.hasOption("targetStorageConfig")) arguments[0] = cmd.getOptionValue("targetStorageConfig"); 
			else if (cmd.hasOption("c")) arguments[0] = cmd.getOptionValue("c");
			
			if (cmd.hasOption("trainingDataDir")) arguments[1] = cmd.getOptionValue("trainingDataDir"); 
			else if (cmd.hasOption("t")) arguments[1] = cmd.getOptionValue("t");
					
			if (cmd.hasOption("outputDir")) arguments[2] = cmd.getOptionValue("outputDir"); 
			else if (cmd.hasOption("o")) arguments[2] = cmd.getOptionValue("o");
			
			return arguments;
					
			} else if (commandType.equals("addSeeds")) {
			
				cmd = parser.parse(addSeedsOptions, args);
				
				if (cmd.hasOption("outputDir")) arguments[0] = cmd.getOptionValue("outputDir"); 
				else if (cmd.hasOption("o")) arguments[0] = cmd.getOptionValue("o");
				
				if (cmd.hasOption("configDir")) arguments[1] = cmd.getOptionValue("configDir"); 
				else if (cmd.hasOption("c")) arguments[1] = cmd.getOptionValue("c");
				
				if (cmd.hasOption("seed")) arguments[2]=cmd.getOptionValue("seed");
				else if (cmd.hasOption("s")) arguments[2]=cmd.getOptionValue("s");
				
				return arguments;	
				
			} else if (commandType.equals("startLinkStorage")) {
				
				cmd = parser.parse(startLinkStorageOptions, args);
				
				if (cmd.hasOption("outputDir")) arguments[0] = cmd.getOptionValue("outputDir"); 
				else if (cmd.hasOption("o")) arguments[0] = cmd.getOptionValue("o");
				
				if (cmd.hasOption("configDir")) arguments[1] = cmd.getOptionValue("configDir"); 
				else if (cmd.hasOption("c")) arguments[1] = cmd.getOptionValue("c");
				
				if (cmd.hasOption("seed")) arguments[2]=cmd.getOptionValue("seed");
				else if (cmd.hasOption("s")) arguments[2]=cmd.getOptionValue("s");
				
				return arguments;
				
			} else if (commandType.equals("startTargetStorage")) {
			
				cmd = parser.parse(startTargetStorageOptions, args);
				
				if (cmd.hasOption("outputDir")) arguments[0] = cmd.getOptionValue("outputDir"); 
				else if (cmd.hasOption("o")) arguments[0] = cmd.getOptionValue("o");
				
				if (cmd.hasOption("configDir")) arguments[1] = cmd.getOptionValue("configDir"); 
				else if (cmd.hasOption("c")) arguments[1] = cmd.getOptionValue("c");
				
				if (cmd.hasOption("modelDir")) arguments[2] = cmd.getOptionValue("modelDir");
				else if (cmd.hasOption("m")) arguments[2] = cmd.getOptionValue("m");
				
				if (cmd.hasOption("profileDir")) arguments[3] = cmd.getOptionValue("profileDir");
				else if (cmd.hasOption("p")) arguments[3] = cmd.getOptionValue("p");
			
				return arguments;
				
			} else if (commandType.equals("startCrawlManager")) {
				
				cmd = parser.parse(startCrawlManagerOptions, args);
				
				if (cmd.hasOption("configDir")) arguments[0] = cmd.getOptionValue("configDir"); 
				else if (cmd.hasOption("c")) arguments[0] = cmd.getOptionValue("c");
		
				return arguments;
			}
		 
		 
		 
		 } catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		 
		 return null;	   
	 }

}
