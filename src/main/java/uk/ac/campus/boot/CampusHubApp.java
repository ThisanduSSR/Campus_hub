package uk.ac.campus.boot;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * The @ApplicationPath annotation establishes the versioned base URI for all
 * resources. Jersey discovers resource classes automatically via the package
 * scan configured in ServerLauncher rather than by overriding getClasses().
 */
@ApplicationPath("/api/v1")
public class CampusHubApp extends Application {
    // Resource discovery is handled by ResourceConfig package scanning in ServerLauncher
}
