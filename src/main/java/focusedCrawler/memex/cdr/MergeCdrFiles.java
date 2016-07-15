package focusedCrawler.memex.cdr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.PaginaURL;

public class MergeCdrFiles {
    
    @Option(name="--input-path", usage="Path to folder with multiple CDR files", required=true)
    private String inputPath;
    
    @Option(name="--output-file", usage="Gziped output file containing data formmated as per CDR 2.0 schema", required=true)
    private String outputFile;
    
    @Option(name="--modelPath", usage="Model path to filter pages out.")
    private String modelPath;

    private static final ObjectMapper mapper = new ObjectMapper();

    private TargetClassifier classifier;
    private PrintWriter out;
    private AtomicInteger processedPages = new AtomicInteger(0);
    private AtomicInteger discardedPages = new AtomicInteger(0);

    private BufferedReader in;

    private Iterator<File> files;
    
    public static void main(String[] args) throws Exception {
        new MergeCdrFiles().run(args);
    }
    
    public void run(String[] args) throws Exception {
        ParserProperties properties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, properties);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
        generateCdrFile();
    }

    private void generateCdrFile() throws Exception {
        
        System.out.println("Reading CDR files from: "+inputPath);
        System.out.println("Generating CDR file at: "+outputFile);
        System.out.println("     Target model path: "+modelPath);
        
        File file = new File(inputPath);
        if(file.isDirectory()) {
            files = Arrays.asList(file.listFiles()).iterator();
        } else {
            files = Arrays.asList(file).iterator();
        }
        
        classifier = TargetClassifierFactory.create(modelPath);
        in = openGzipFile(files.next());
        out = new PrintWriter(new GZIPOutputStream(new FileOutputStream(outputFile)), true);

        int threadNumber = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            LineClassifier t = new LineClassifier();
            t.start();
            threads[i] = t;
        }
        for (int i = 0; i < threadNumber; i++) {
            threads[i].join();
        }
        
        in.close();
        out.close();
        
        System.out.printf("%d files processed.", processedPages.intValue());
    }
    
    class LineClassifier extends Thread {
        @Override
        public void run() {
            String line = readLine();
            while(line != null) {
                try {
                    TargetRelevance relevance = classify(line);
                    if (relevance.isRelevant()) {
                        synchronized (out) {
                            out.println(line);
                        }
                    } else {
                        discardedPages .incrementAndGet();
                    }
                    int count = processedPages.incrementAndGet();
                    if (count % 100 == 0) {
                        System.out.printf("%d discarded pages out of %d processed pages\n", discardedPages.intValue(), count);
                    }
                } catch (Exception e) {
                    new Exception("Failed to classify page.", e).printStackTrace();
                }
                line = readLine();
            }
        }
    }
    
    private String readLine() {
        try {
            String line = in.readLine();
            if(line == null) {
                while(line == null && files.hasNext()) {
                    File file = files.next();
                    System.out.println("Opening file: "+file.getCanonicalPath());
                    in = openGzipFile(file);
                    line = in.readLine();
                }
            }
            return line;
        } catch (IOException e) {
            new Exception("Failed to read next line.", e).printStackTrace();
            return null;
        }
    }

    private BufferedReader openGzipFile(File file) throws IOException, FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file), 512*4096)));
    }

    private TargetRelevance classify(String line) throws Exception {
        
        CDRDocument doc = mapper.readValue(line, CDRDocument.class);
        
        Page page = new Page(new URL(doc.getUrl()), new String(doc.getRawContent()));
        PaginaURL pageParser = new PaginaURL(page.getURL(), page.getContent());
        page.setPageURL(pageParser);

        TargetRelevance relevance = classifier.classify(page);
        int count = processedPages.intValue();
        if(count % 100 == 0) {
            System.out.printf("%d %.3f %s\n", count, relevance.getRelevance(), doc.getUrl());
        }
        return relevance;
    }

}
