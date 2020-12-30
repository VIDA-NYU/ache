package achecrawler.memex.cdr;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.CliTool;
import achecrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="MergeCdrFiles", description="Merges multiple CDR files into one")
public class MergeCdrFiles extends CliTool {
    
    @Option(name="--input-path", description="Path to folder with multiple CDR files", required=true)
    private String inputPath;
    
    @Option(name="--output-file", description="Gziped output file containing data formmated as per CDR 2.0 schema", required=true)
    private String outputFile;
    
    @Option(name="--modelPath", description="Model path to filter pages out")
    private String modelPath;
    
    @Option(name="--dedup", description="Whether merge shoud filter duplications")
    private boolean dedup;

    private static final ObjectMapper mapper = new ObjectMapper();

    private TargetClassifier classifier;
    private PrintWriter out;
    private AtomicInteger processedPages = new AtomicInteger(0);
    private AtomicInteger discardedPages = new AtomicInteger(0);
    private HashSet<String> uniqueSet = new HashSet<>();
    private BufferedReader in;

    private Iterator<File> files;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new MergeCdrFiles());
    }
    
    public void execute() throws Exception {
        
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
        
        System.out.printf("%d discarded out of %d files processed.\n",
                          discardedPages.intValue(), processedPages.intValue());
    }
    
    class LineClassifier extends Thread {
        @Override
        public void run() {
            String line = readLine();
            while(line != null) {
                try {
                    CDR2Document doc = mapper.readValue(line, CDR2Document.class);
                    
                    boolean discard = true;
                    String hash = hashDocument(doc);
                    
                    if(!uniqueSet.contains(hash)) {
                        uniqueSet.add(hash);
                        TargetRelevance relevance = classify(doc);
                        if (relevance.isRelevant()) {
                            synchronized (out) {
                                discard = false;
                            }
                        }
                    }
                    
                    if(discard) {
                        discardedPages .incrementAndGet();
                    } else {
                        out.println(line);
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

    private TargetRelevance classify(CDR2Document doc) throws Exception {
        
        Page page = new Page(new URL(doc.getUrl()), new String(doc.getRawContent()));
        PaginaURL pageParser = new PaginaURL(page);
        page.setParsedData(new ParsedData(pageParser));

        TargetRelevance relevance = classifier.classify(page);
        int count = processedPages.intValue();
        if(count % 100 == 0) {
            System.out.printf("%d %.3f %s\n", count, relevance.getRelevance(), doc.getUrl());
        }
        return relevance;
    }

    private String hashDocument(CDR2Document doc) {
        String url = doc.getUrl();
        url = url.replaceFirst("https?://", "");
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String contentHash = DigestUtils.sha1Hex(doc.getRawContent());
        String urlHash= DigestUtils.sha1Hex(doc.getUrl());
        return DigestUtils.md5Hex(urlHash+contentHash);
    }

}
