package focusedCrawler.util;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

public abstract class CliTool {
    
    abstract public void execute() throws Exception;

    public static void run(String[] args, CliTool tool) {
        ParserProperties properties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(tool, properties);
        try {
            parser.parseArgument(args);
            tool.execute();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
    }
    
}