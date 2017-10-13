package focusedCrawler.rest;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.ResponseTransformer;
import spark.Route;

public class Transformers {
    
    private static final Logger logger = LoggerFactory.getLogger(Transformers.class);
    
    public static ResponseTransformer json = new ResponseTransformer() {

        private ObjectMapper jsonMapper;

        {
            final TimeUnit rateUnit = TimeUnit.SECONDS;
            final TimeUnit durationUnit = TimeUnit.MILLISECONDS;
            final boolean showSamples = false;
            jsonMapper = new ObjectMapper()
                    .registerModule(new MetricsModule(rateUnit, durationUnit, showSamples));
        }

        @Override
        public String render(Object model) {
            try {
                return jsonMapper.writeValueAsString(model);
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize response as JSON";
                logger.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }

    };

    public static Route text(Route route) {
        return (req, res) -> {
            res.header("Content-Type", "text/plain");
            Object result = route.handle(req, res);
            return result instanceof String ? result : result.toString();
        };
    }

    public static Route json(Route route) {
        return (req, res) -> {
            res.header("Content-Type", "application/json");
            return json.render(route.handle(req, res));
        };
    }

}
