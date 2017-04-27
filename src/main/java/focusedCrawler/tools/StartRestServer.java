package focusedCrawler.tools;

import com.codahale.metrics.MetricRegistry;

import focusedCrawler.Main;
import focusedCrawler.rest.RestConfig;
import focusedCrawler.rest.RestServer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "StartRestServer", description = "Start a REST API for to manage web crawls")
public class StartRestServer extends CliTool {

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    @Option(name = {"-p", "--port"}, required = false,
            description = "Port at which the web server will be available")
    private int port = 8080;

    private String host = "0.0.0.0";

    private boolean enableCors = true;

    @Option(name = {"-d", "--data"}, required = false,
            description = "Path to folder where server should store its data")
    private String data = "server-data";


    public static void main(String[] args) throws Exception {
        CliTool.run(args, new StartRestServer());
    }

    @Override
    public void execute() throws Exception {
        MetricRegistry metricsRegistry = new MetricRegistry();
        RestConfig restConfig = new RestConfig(host, port, enableCors);
        RestServer server = new RestServer(restConfig, metricsRegistry);
        server.start();
    }

}
