package focusedCrawler.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.util.StringInputStream;

import focusedCrawler.util.WildcardMatcher;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.staticfiles.StaticFilesConfiguration;
import spark.utils.GzipUtils;

public class StaticFileHandlerFilter implements Filter {

    private static final String INDEX_PATH = "public/index.html";

    private StaticFilesConfiguration staticFilesHandler;
    private WildcardMatcher patterns;
    private String basePath;


    public StaticFileHandlerFilter(List<String> indexHtmlPaths, String basePath) {
        String path = basePath;
        path = (path != null && path.startsWith("/")) ? path : "/" + path;
        path = (path != null && path.endsWith("/")) ? path : path + "/";
        this.basePath = path;
        this.staticFilesHandler = new StaticFilesConfiguration();
        this.staticFilesHandler.configure("/public");
        this.patterns = WildcardMatcher.fromWhitelist(indexHtmlPaths);
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        String path = request.pathInfo();
        if (patterns.matches(path)) {
            renderIndexHtml(request, response);
        } else {
            staticFilesHandler.consume(request.raw(), response.raw());
        }
    }

    private void renderIndexHtml(Request request, Response response) {
        String indexHtml = readIndexHtmlFile();
        String authHeader = request.headers("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            String meta = String.format("<meta name=\"authorization\" content=\"%s\">", authHeader);
            indexHtml = indexHtml.replaceFirst("<head>", "<head>" + meta);
        }
        if(basePath != null && !basePath.isEmpty()) {
            String meta = String.format("<meta name=\"base_path\" content=\"%s\">", basePath);
            indexHtml = indexHtml.replaceFirst("<head>", "<head>" + meta);
            indexHtml = indexHtml.replaceAll("=\"./static/", "=\"" + basePath + "static/");
        }
        writeResponse(request, response, indexHtml);
    }

    private String readIndexHtmlFile() {
        InputStream is = RestServer.class.getClassLoader().getResourceAsStream(INDEX_PATH);
        try {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + INDEX_PATH, e);
        }
    }

    private void writeResponse(Request request, Response response, String file) {
        OutputStream wrappedOutputStream;
        try {
            response.header("Content-Type", "text/html");
            response.status(200);
            wrappedOutputStream = GzipUtils.checkAndWrap(request.raw(), response.raw(), false);
            IOUtils.copy(new StringInputStream(file), wrappedOutputStream);
            wrappedOutputStream.flush();
            wrappedOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTTP response", e);
        }
    }

}
