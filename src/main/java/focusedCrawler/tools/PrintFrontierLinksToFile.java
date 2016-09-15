package focusedCrawler.tools;

import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.persistence.TupleIterator;


public class PrintFrontierLinksToFile extends CliTool {
    
    @Option(name="--input-data-path", usage="Path to ACHE data target folder", required=true)
    private String inputPath;
    
    @Option(name="--output-file", usage="The output file", required=false)
    private String outputFile;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new PrintFrontierLinksToFile());
    }

    @Override
    public void execute() throws Exception {
        if(outputFile == null) {
            printLinks(System.out);
        } else {
            try(PrintStream out = new PrintStream(outputFile)) {
                printLinks(out);
            }
        }
    }

    private void printLinks(PrintStream out) throws Exception {
        Frontier frontier = new Frontier(inputPath, 1000);
        try (TupleIterator<LinkRelevance> it = frontier.iterator()) {
            while (it.hasNext()) {
                out.println(it.next().getValue().getURL().toString());
            }
        }
        frontier.close();
    }

}
