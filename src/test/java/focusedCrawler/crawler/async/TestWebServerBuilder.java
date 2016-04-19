package focusedCrawler.crawler.async;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

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

        public OkHandler(String responseContent) {
            this.responseContent = responseContent;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
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
        return withHandler(path, new OkHandler(response));
    }

}