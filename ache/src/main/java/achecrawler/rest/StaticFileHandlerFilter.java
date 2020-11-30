package achecrawler.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.util.StringInputStream;

import achecrawler.util.WildcardMatcher;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.staticfiles.StaticFilesConfiguration;
import spark.utils.GzipUtils;

public class StaticFileHandlerFilter implements Filter {

    private static final String INDEX_PATH = "public/index.html";

    private String indexHtml;
    private StaticFilesConfiguration staticFilesHandler;
    private WildcardMatcher patterns;
    private String basePath;

    public StaticFileHandlerFilter(List<String> indexHtmlPaths, String basePath) {
        this.staticFilesHandler = new StaticFilesConfiguration();
        this.staticFilesHandler.configure("/public");
        this.patterns = WildcardMatcher.fromWhitelist(indexHtmlPaths);
        this.basePath = normalizeBasePath(basePath);
        this.indexHtml = readIndexHtmlFile();
    }

    private String normalizeBasePath(String basePath) {
        String path = (basePath == null) ? "/" : basePath;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    private String readIndexHtmlFile() {
        InputStream is = RestServer.class.getClassLoader().getResourceAsStream(INDEX_PATH);
        try {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + INDEX_PATH, e);
        }
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        String path = request.pathInfo();
        boolean requestConsumed = false;
        if (patterns.matches(path)) {
            renderIndexHtml(request, response);
            requestConsumed = true;
        } else {
            requestConsumed = staticFilesHandler.consume(request.raw(), response.raw());
        }
        if(requestConsumed) {
            response.body(""); // needed to suppress spark-java route mapping error log
        }
    }

    private void renderIndexHtml(Request request, Response response) {
        String indexHtml = this.indexHtml;
        indexHtml = injectAuthorizationMetaTag(request, indexHtml);
        indexHtml = injectBasePathMetaTag(indexHtml);
        writeResponse(request, response, indexHtml);
    }

    private String injectBasePathMetaTag(String indexHtml) {
        if (basePath == null || basePath.isEmpty()) {
            return indexHtml;
        }
        String meta = String.format("<meta name=\"base_path\" content=\"%s\">", basePath);
        indexHtml = indexHtml.replaceFirst("<head>", "<head>" + meta);
        indexHtml = indexHtml.replaceAll("=\"./static/", "=\"" + basePath + "static/");
        return indexHtml;
    }

    private String injectAuthorizationMetaTag(Request request, String indexHtml) {
        String authHeader = request.headers("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return indexHtml;
        }
        String meta = String.format("<meta name=\"authorization\" content=\"%s\">", authHeader);
        return indexHtml.replaceFirst("<head>", "<head>" + meta);
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
