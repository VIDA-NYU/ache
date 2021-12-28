package achecrawler.rest;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class SinglePageAppHandler implements Handler {

    private static final String INDEX_PATH = "public/index.html";

    private final String indexHtml;
    private final String basePath;

    public SinglePageAppHandler(String basePath) {
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
    public void handle(Context ctx) throws Exception {
        String indexHtml = this.indexHtml;
        indexHtml = injectAuthorizationMetaTag(ctx, indexHtml);
        indexHtml = injectBasePathMetaTag(indexHtml);
        ctx.html(indexHtml);
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

    private String injectAuthorizationMetaTag(Context ctx, String indexHtml) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return indexHtml;
        }
        String meta = String.format("<meta name=\"authorization\" content=\"%s\">", authHeader);
        return indexHtml.replaceFirst("<head>", "<head>" + meta);
    }
}
