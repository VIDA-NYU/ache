package focusedCrawler.tools;

import focusedCrawler.Main;
import focusedCrawler.rest.RestServer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "startServer", description = "Start a REST API for to manage web crawls")
public class StartRestServer extends CliTool {

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    @Option(name = {"-d", "--data-path"}, required = true,
            description = "Path to folder where server should store its data")
    private String dataPath = "./data";

    @Option(name = {"-c", "--config"}, required = false,
            description = "Path to the configuration file")
    private String configPath = null;

    @Option(name = {"-e", "--elasticIndex"}, required = false,
            description = "Elasticsearch index name to be used")
    private String esIndexName;

    @Option(name = {"-t", "--elasticType"}, required = false,
            description = "Elasticsearch type name to be used")
    private String esTypeName;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new StartRestServer());
    }

    @Override
    public void execute() throws Exception {
        if (configPath != null && !configPath.isEmpty()) {
        }
        RestServer server = RestServer.create(configPath, dataPath, esIndexName, esTypeName);
        server.start();
    }

}
