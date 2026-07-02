package com.moonlight.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catches anything not already handled as an ApiException so internal details
 * (stack traces, exception class names) never reach the client.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.log(Level.SEVERE, "Unhandled exception while processing request", exception);
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", "Error interno del servidor"))
                .build();
    }
}
