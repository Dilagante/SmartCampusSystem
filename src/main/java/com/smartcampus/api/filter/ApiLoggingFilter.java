package com.smartcampus.api.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.io.IOException;
import java.util.logging.Logger;


public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    // Fires before the request is sent to a resource method
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
                "[REQUEST] --> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }

    // Fires after resource method returns
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
                "[RESPONSE] <-- %s %s | Status: %d %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase()
        ));
    }

}
