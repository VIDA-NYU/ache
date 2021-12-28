package achecrawler.rest.resources;

import com.codahale.metrics.jvm.ThreadDump;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;


public class ThreadsResource {

    private final ThreadDump jvmThreadDump = new ThreadDump(ManagementFactory.getThreadMXBean());

    public Handler threadDump = (Context ctx) -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jvmThreadDump.dump(baos);
        ctx.contentType("text/plain");
        ctx.result(baos.toString());
    };

}
