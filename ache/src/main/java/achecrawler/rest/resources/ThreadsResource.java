package achecrawler.rest.resources;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;

import com.codahale.metrics.jvm.ThreadDump;

import spark.Route;

public class ThreadsResource {

    private ThreadDump jvmThreadDump = new ThreadDump(ManagementFactory.getThreadMXBean());

    public Route threadDump = (request, response) -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jvmThreadDump.dump(baos);
        return baos.toString();
    };

}
