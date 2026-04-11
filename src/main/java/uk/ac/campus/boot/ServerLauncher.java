package uk.ac.campus.boot;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Application entry point.
 * Bootstraps an embedded Grizzly HTTP server with Jersey JAX-RS and Jackson JSON support.
 *
 * No external servlet container (Tomcat, Jetty, etc.) is required — the server
 * is entirely self-contained and launched from this class.
 */
public class ServerLauncher {

    private static final Logger LOG      = Logger.getLogger(ServerLauncher.class.getName());
    public  static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws Exception {

        ResourceConfig config = new ResourceConfig()
                .packages(
                        "uk.ac.campus.api",
                        "uk.ac.campus.errors.handlers",
                        "uk.ac.campus.middleware"
                )
                .register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        LOG.info("=======================================================");
        LOG.info("  Campus Hub API  —  http://localhost:8080/api/v1");
        LOG.info("  Discovery :  GET http://localhost:8080/api/v1");
        LOG.info("  Press CTRL+C to shut down.");
        LOG.info("=======================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown signal received — stopping server...");
            server.shutdownNow();
        }));

        Thread.currentThread().join();
    }
}
