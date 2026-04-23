package uk.ac.campus.boot;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application entry point for Tomcat.
 *
 * Extending ResourceConfig allows us to perform package scanning and
 * register features like Jackson JSON support dynamically.
 */
@ApplicationPath("/api/v1")
public class CampusHubApp extends ResourceConfig {
    public CampusHubApp() {
        // Register the packages containing our JAX-RS resources and providers
        packages(
                "uk.ac.campus.api",
                "uk.ac.campus.errors.handlers",
                "uk.ac.campus.middleware"
        );

        // Register Jackson for JSON serialization/deserialization
        register(JacksonFeature.class);
    }
}
