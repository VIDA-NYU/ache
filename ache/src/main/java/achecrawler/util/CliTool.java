package achecrawler.util;

import javax.inject.Inject;

import io.airlift.airline.Help;
import io.airlift.airline.HelpOption;
import io.airlift.airline.ParseException;
import io.airlift.airline.SingleCommand;
import io.airlift.airline.model.MetadataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CliTool implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CliTool.class);

    @Inject
    public HelpOption helpOption;
    
    public void run() {
        try {
            execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    abstract public void execute() throws Exception;

    public static void run(String[] args, CliTool tool) {
        try {
            SingleCommand<? extends CliTool> cmd = SingleCommand.singleCommand(tool.getClass());
            CliTool cli = cmd.parse(args);
            if (cli.helpOption.showHelpIfRequested()) {
                return;
            }
            cli.execute();
        }
        catch(ParseException e) {
            System.out.println("Unable to parse the input. "+e.getMessage()+"\n\n");
            Help.help(MetadataLoader.loadCommand(tool.getClass()));
            System.exit(1);
        }
        catch (Exception e) {
            logger.error("Failed to execute command.", e);
            System.exit(1);
        }
    }
    
}