package achecrawler;

import io.airlift.airline.Arguments;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.ParseException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "run", description = "Run any available utility tool")
public class RunCliTool implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(RunCliTool.class);

    @Arguments(description = "Tool to be executed followed by its parameters")
    public List<String> args;

    public static void main(String[] args) {
        @SuppressWarnings("unchecked")
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("ache-tools")
            .withDescription("ACHE Crawler Tools")
            .withCommands(
                RunCliTool.class
            );

        Cli<Runnable> acheParser = builder.build();
        try {
            acheParser.parse(args).run();
        }
        catch(ParseException e) {
            System.out.println("Unable to parse the input. "+e.getMessage()+"\n");
            Help.help(acheParser.getMetadata(), Arrays.asList());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("Failed to execute command.");
            e.printStackTrace(System.err);
        }
    }

    public void run() {
        if(args == null || args.size() == 0) {
            System.out.println("ERROR: Class name of command-line tool not specified.");
            System.exit(1);
        }

        String toolClass = args.get(0);
        Class<?> loadedClass = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                loadedClass = classLoader.loadClass("achecrawler.tools." + toolClass);
            } catch(ClassNotFoundException e) {
                // also try full class name
                loadedClass = classLoader.loadClass(toolClass);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to find CLI tool named "+toolClass);
            System.exit(1);
        }
        // Execute main() method of loaded class
        String[] params = args.subList(1, args.size()).toArray(new String[args.size()-1]);
        try {
            Method mainMethod = loadedClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) params);
        } catch (Exception e) {
            System.out.printf("Failed to run tool %s.\n\n", loadedClass.getName());
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

}