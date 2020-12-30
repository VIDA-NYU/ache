package achecrawler.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.CliTool;
import achecrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="TargetClassifierTester", description="Classifies a pages using a given classifier")
public class TargetClassifierTester extends CliTool {

    @Option(name = "--input-file",
            required = true,
            description = "Path to file a file containing an HTML page (name of file should be a valid URL)")
    private String inputPath;

    @Option(name = "--model",
            required = true,
            description = "A path to the target classifier model")
    private String model;


    public static void main(String[] args) throws Exception {
        CliTool.run(args, new TargetClassifierTester());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading page content from file: " + inputPath);
        System.out.println("       Loading classifier from: " + model);
        
        String content = new String(Files.readAllBytes(Paths.get(inputPath)), "UTF-8");
        String url = "http://";
        try {
            String filename = new File(inputPath).getName();
            url = new URL(URLDecoder.decode(filename, "UTF-8")).toString();
        } catch (MalformedURLException e) {
            System.out.println("File name not recognized as valid URL.");
        }

        Page page = createPage(url, content);
        
        TargetClassifier classifier = TargetClassifierFactory.create(model);
        
        TargetRelevance result = classifier.classify(page);
        String label = result.isRelevant() ? "Relevant" : "Irrelevant";
        System.out.println("-------------------------");
        System.out.println("            Classified as: "+ label);
        System.out.println("Classification confidence: "+ String.format("%.4f",result.getRelevance()));
        System.out.println("-------------------------");
    }
    
    private Page createPage(String urlStr, String cont) throws MalformedURLException {
        URL url = new URL(urlStr);
        Page page1 = new Page(url, cont);
        page1.setParsedData(new ParsedData(new PaginaURL(page1)));
        return page1;
    }

}
