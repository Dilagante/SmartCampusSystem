package com.smartcampus.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {


    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> error = new LinkedHashMap<>();

        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", ex.getMessage());
        error.put("resourceType", ex.getResourceType());
        error.put("resourceId", ex.getResourceId());
        error.put("hint", "Ensure the referenced " + ex.getResourceType()
                + " exists before creating a resource that depends on it.");

        return Response
                .status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();


    }
}
