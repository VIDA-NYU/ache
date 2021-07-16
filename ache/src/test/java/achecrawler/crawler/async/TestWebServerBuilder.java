package achecrawler.crawler.async;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TestWebServerBuilder {

    public static final int port = 8345;
    public static final String address = "http://localhost:" + port;
    
    private HttpServer server;

    public TestWebServerBuilder() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
    }
    
    public TestWebServerBuilder(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
    }

    public TestWebServerBuilder withHandler(String path, HttpHandler handler) {
        server.createContext(path, handler);
        return this;
    }

    public HttpServer start() {
        server.setExecutor(null); // creates a default executor
        server.start();
        return server;
    }
    
    static class OkHandler implements HttpHandler {
        
        private final String responseContent;
        private final String contentType;

        public OkHandler(String responseContent, String contentType) {
            this.responseContent = responseContent;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", contentType);
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseContent.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(responseContent.getBytes());
            os.close();
            t.close();
        }
    }
    
    static class RedirectionHandler implements HttpHandler {
        
        private String newLocation;

        public RedirectionHandler(String newLocation) {
            this.newLocation = newLocation;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Location", newLocation);
            t.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
            t.close();
        }
    }
    
    public TestWebServerBuilder withRedirect(String origin, String redirect) {
        return withHandler(origin, new RedirectionHandler(redirect));
    }

    public TestWebServerBuilder with200OK(String path, String response) {
        return withHandler(path, new OkHandler(response, "text/html; charset=utf-8"));
    }
    
    public TestWebServerBuilder with200OK(String path, String response, String contentType) {
        return withHandler(path, new OkHandler(response, contentType));
    }

    public TestWebServerBuilder withStaticFolder(Path path) {
        System.out.println("Loading static folder server: " + path.toString());
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            for (Path filePath : directory) {
                Path fileName = filePath.getFileName();
                String contentType = getContentType(fileName);
                String serverPath = "/" + fileName;
                String fileContent = new String(Files.readAllBytes(filePath)); 
                with200OK(serverPath, fileContent, contentType);
                System.out.println("Loading file: " + filePath.toString());
                System.out.println("     at path: " + serverPath);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read files from: "+path.toString(), e);
        }
        return this;
    }

    private String getContentType(Path fileName) {
        String contentType;
        if(fileName.endsWith(".txt")) {
            contentType = "text/plain";
        }
        if(fileName.endsWith(".xml")) {
            contentType = "text/xml";
        }
        else {
            contentType = "text/html";
        }
        return contentType;
    }

}