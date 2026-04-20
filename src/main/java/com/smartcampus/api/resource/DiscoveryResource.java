package com.smartcampus.api.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        // We use a LinkedHashMap to preserve the order items are added
        Map<String, Object> response = new LinkedHashMap<>();

        // Versioning
        response.put("greeting","Welcome to the Smart Campus Sensor System!");
        response.put("version", "1.0.0");
        response.put("apiBase", "/api/v1");

        // Administrative Contact
        Map<String, String> contact = new LinkedHashMap<>();

        contact.put("dev","DilharaDS");
        contact.put("email","dilhara.12341234@iit.ac.lk");
        contact.put("documentation","Coming Soon!"); //Don't forget to add this!

        response.put("contact", contact);

        // Resource Collection Map

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "api/v1/rooms");
        resources.put("sensors", "api/v1/sensors");
        resources.put("readings", "api/v1/readings");

        response.put("resources", resources);

        return Response.ok(response).build();


    }


}
