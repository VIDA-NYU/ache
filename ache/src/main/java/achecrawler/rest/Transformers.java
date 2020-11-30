package achecrawler.rest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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

    public static Route promethize(Route route){
        return (req,res) ->{
            res.header("Content-Type", "text/plain");
            Object result = route.handle(req,res);
            return result instanceof MetricRegistry ? parse(result) : null;
        };
    }

    @SuppressWarnings("rawtypes")
    public static String parse(Object result){
        MetricRegistry registry = (MetricRegistry) result;
        StringBuilder sb = new StringBuilder();

        Map<String, Counter> counters = registry.getCounters();
        for(Map.Entry<String, Counter> c : counters.entrySet()){
            sb.append(c.getKey().replace(".","_")+" "+c.getValue().getCount()+"\n");
        }

        Map<String, Timer> timers = registry.getTimers();
        for(Map.Entry<String, Timer> t : timers.entrySet()){
            sb.append(t.getKey().replace(".","_")+" "+t.getValue().getCount()+"\n");
        }

        Map<String, Gauge> gauges = registry.getGauges();
        for(Map.Entry<String, Gauge> g : gauges.entrySet()){
            sb.append(g.getKey().replace(".","_")+" "+g.getValue().getValue()+"\n");
        }

        Map<String, Histogram> histograms = registry.getHistograms();
        for(Map.Entry<String, Histogram> h : histograms.entrySet()){
            sb.append(h.getKey().replace(".","_")+" "+h.getValue().getCount()+"\n");
        }

        return sb.toString();
    }
}
