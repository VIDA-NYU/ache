package focusedCrawler.crawler.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class TestWebServerBuilder {

    public static final int port = 8345;
    public static final String address = "http://localhost:" + port;
    
    private HttpServer server;

    public TestWebServerBuilder() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
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
    
    static class StaticFileHandler implements HttpHandler {

        private final String staticFolder;
        private final String basePath;
        
        public StaticFileHandler(String basePath, String staticFolder) {
            this.basePath = basePath.endsWith("/")? basePath.substring(0, basePath.length()-1) : basePath;
            System.out.println(basePath);
            this.staticFolder = staticFolder;
        }
        
        public void handle(HttpExchange t) throws IOException {
            URI uri = t.getRequestURI();
            String path = uri.getPath().replaceFirst(basePath, "");
            File file = new File(staticFolder + path).getCanonicalFile();

            System.out.println("Requested file: "+ file.getCanonicalPath());
            if (!file.isFile()) {
              // Object does not exist or is not a file: reject with 404 error.
              String response = "404 (Not Found)\n";
              
              String mime = "text/html";
              Headers h = t.getResponseHeaders();
              h.set("Content-Type", mime);
              
              t.sendResponseHeaders(404, response.length());
              OutputStream os = t.getResponseBody();
              os.write(response.getBytes());
              os.close();
            } else {
              // Object exists and is a file: accept with response code 200.
              String mime = "text/html";
              if(path.substring(path.length()-3).equals(".js")) mime = "application/javascript";
              if(path.substring(path.length()-3).equals("css")) mime = "text/css";            

              Headers h = t.getResponseHeaders();
              h.set("Content-Type", mime);
              t.sendResponseHeaders(200, 0);              

              OutputStream os = t.getResponseBody();
              FileInputStream fs = new FileInputStream(file);
              final byte[] buffer = new byte[0x10000];
              int count = 0;
              while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
              }
              fs.close();
              os.close();
            }  
        }
        
    }

    public TestWebServerBuilder withRedirect(String origin, String redirect) {
        return withHandler(origin, new RedirectionHandler(redirect));
    }

    public TestWebServerBuilder with200OK(String path, String response) {
        return withHandler(path, new OkHandler(response));
    }

    public TestWebServerBuilder withStaticFolder(String path, String staticFolder) {
        return withHandler(path, new StaticFileHandler(path, staticFolder));
    }

}