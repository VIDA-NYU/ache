package focusedCrawler.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import spark.ModelAndView;
import spark.TemplateEngine;
import spark.TemplateViewRoute;

public class StaticFileEngine extends TemplateEngine {

    public static final StaticFileEngine engine = new StaticFileEngine();

    public static final TemplateViewRoute noopRouter = (request, response) -> {
        return null;
    };

    @Override
    public String render(ModelAndView modelAndView) {
        String filename = "public/index.html";
        InputStream is = RestServer.class.getClassLoader().getResourceAsStream(filename);
        try {
            String file = IOUtils.toString(is, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.");
        }
    }

}
