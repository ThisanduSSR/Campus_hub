package uk.ac.campus.middleware;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cross-cutting observability filter applied automatically to every API call.
 *
 * Implementing ContainerRequestFilter and ContainerResponseFilter on one class
 * means logging is guaranteed to fire for every endpoint without manual
 * Logger.info() calls scattered throughout each resource method.
 * This is a cleaner separation of concerns: resource classes handle business
 * logic, this filter handles operational visibility.
 */
@Provider
public class RequestAuditFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger AUDIT = Logger.getLogger("uk.ac.campus.audit");

    /** Logs the inbound request before any resource method executes. */
    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        AUDIT.info(String.format(
                ">> INBOUND  | %-6s | %s",
                req.getMethod(),
                req.getUriInfo().getAbsolutePath()
        ));
    }

    /** Logs the outbound response after the resource method completes. */
    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        AUDIT.info(String.format(
                "<< OUTBOUND | %-6s | %s | Status %d",
                req.getMethod(),
                req.getUriInfo().getAbsolutePath(),
                res.getStatus()
        ));
    }
}
