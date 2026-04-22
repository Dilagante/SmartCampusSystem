package com.smartcampus.api.exception;

public class LinkedResourceNotFoundException extends RuntimeException{

    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with ID '" + resourceId + "' could not be found.");
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
