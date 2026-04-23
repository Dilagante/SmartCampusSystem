# Smart Campus System - Architecture & Setup Report

## Table of Contents

- [Part 1: Service Architecture & Setup](https://www.google.com/search?q=%23part-1-service-architecture--setup)
    
    - [1. Project & Application Configuration](https://www.google.com/search?q=%231-project--application-configuration)
        
    - [2. The “Discovery” Endpoint](https://www.google.com/search?q=%232-the-discovery-endpoint)
        
- [Part 2: Room Management](https://www.google.com/search?q=%23part-2-room-management)
    
    - [1. Room Resource Implementation](https://www.google.com/search?q=%231-room-resource-implementation)
        
    - [2. Room Deletion & Safety Logic](https://www.google.com/search?q=%232-room-deletion--safety-logic)
        
- [Part 3: Sensor Operations and Linking](https://www.google.com/search?q=%23part-3-sensor-operations-and-linking)
    
    - [1. Sensor Resource & Integrity](https://www.google.com/search?q=%231-sensor-resource--integrity)
        
    - [2. Filtered Retrieval & Search](https://www.google.com/search?q=%232-filtered-retrieval--search)
        
- [Part 4: Deep Nesting with Sub-Resources](https://www.google.com/search?q=%23part-4-deep-nesting-with-sub-resources)
    
    - [1. The Sub-Resource Locator Pattern](https://www.google.com/search?q=%231-the-sub-resource-locator-pattern)
        
- [Part 5: Advanced Error Handling, Exception Mapping & Logging](https://www.google.com/search?q=%23part-5-advanced-error-handling-exception-mapping--logging)
    
    - [1. Dependency Validation](https://www.google.com/search?q=%231-dependency-validation)
        
    - [2. The Global Safety Net](https://www.google.com/search?q=%232-the-global-safety-net)
        
    - [3. API Request & Response Logging Filters](https://www.google.com/search?q=%233-api-request--response-logging-filters)
        

---

## Part 1: Service Architecture & Setup

### 1. Project & Application Configuration

By default, JAX-RS creates a new resource instance for every incoming HTTP request. This reflects HTTP’s stateless nature. Since instances are destroyed immediately after the request, any data stored inside would be lost.

To solve this, the system uses a **Singleton Pattern**. By using a single, central `DataStore` instance to hold the data permanently, it survives across multiple requests. Additionally, since many clients may access the server at the same time, the system uses `ConcurrentHashMap` instead of a standard `HashMap` to safely manage the data and prevent race conditions.

### 2. The “Discovery” Endpoint

**Hypermedia** is considered a hallmark of advanced RESTful design since it indicates what actions are possible in addition to providing data.

This approach benefits client developers rather than the use of static documentation through the reduction of client-side logic, as complex business rules don’t need to be hardcoded based on documentation. In addition, hypermedia APIs are self-documenting, ensuring that documentation is never out of date. A client-side developer can simply inspect a response and immediately see all available next steps and workflows.

---

## Part 2: Room Management

### 1. Room Resource Implementation

Returning only IDs is efficient for the user’s bandwidth and consumes fewer resources on the server, but forces the client to constantly make an additional request every time it needs to get a specific room. Returning full objects may take more resources per request, but lets the client render a complete room listing in one request, especially since clients would rarely fetch a list of IDs without the need for the rest of the data. If bandwidth becomes a concern at a large scale, pagination can be an accepted solution.

### 2. Room Deletion & Safety Logic

The delete operation is not idempotent in this implementation. The first request would delete the sensor and return `204 No Content`, but subsequent requests will return `404 Not Found` as there is no such room to delete. Since the response code changes between calls, the operation cannot be considered idempotent.

However, returning `204 No Content` on subsequent Delete operations would imply that a resource is being deleted every time the request is sent, which is untrue. Returning `404 Not Found` would be the more accurate response as the room to be deleted cannot be found. Further, the issue of idempotency is unimportant here, as the state of the server is the same between the second and third call, and the data is not changed.

---

## Part 3: Sensor Operations and Linking

### 1. Sensor Resource & Integrity

If a client attempts to send data in an unaccepted format, the JAX-RS runtime will evaluate the `Content-Type` header of the request before it is sent to the resource method. Since the header does not match the `@Consumes(MediaType.APPLICATION_JSON)` annotation added to the Resource class, the framework will automatically reject the request and return the status code `415 Unsupported Media Type`. This filtering protects the application from attempting to parse incompatible payloads and saves developers from needing to manually write validation logic.

### 2. Filtered Retrieval & Search

Query parameters are the correct tool for filtering since they are completely optional, with `/api/v1/sensors` and `/api/v1/sensors?type=CO2` leading to different views of the same resource. Making type part of the URL (e.g., `/api/v1/sensors/type/CO2`) implies that it is a separate resource. It also creates the issue of being unable to combine multiple filters such as `?type=CO2&status=ACTIVE`.

---

## Part 4: Deep Nesting with Sub-Resources

### 1. The Sub-Resource Locator Pattern

Defining every nested path in a single controller means a single class is responsible for rooms, sensors, readings, and any future resources, making it difficult to test and maintain in the future. The Sub-Resource pattern solves this by treating each level of the hierarchy as its own entity being handed off to the next, with `SensorResource` handing off the request to `SensorReadingResource` only when necessary, such that each class can be developed and tested independently. This also prevents route conflicts, as sub-paths within the same class would require exact ordering to avoid JAX-RS matching to the wrong method.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### 1. Dependency Validation

Using a `404 Not Found` response usually implies that the URL does not exist, stating that the endpoint is invalid. When a request cannot be processed due to a missing or invalid reference in the body, it is not an issue with the endpoint itself, but with the data being sent and processed. Therefore, using `404 Not Found` would be incorrect, leading to the use of `422 Unprocessable Entity` since that communicates that the request was received, the body was valid, but it cannot be processed due to server logic or incorrect data.

### 2. The Global Safety Net

Exposing a raw Java stack trace leaks information that an attacker can directly exploit against the system. Class names and package names reveal internal architecture, and reveal what dependencies and technologies are used to run the system, making it simple to narrow down exploits to use. Exception messages also contain the exact data that caused the failure, and some exceptions may reveal internal workings of databases which can be used to steal user data.

End users have no use for internal data; it is only useful to developers who already have access to server logs, where full logging can be implemented securely.

### 3. API Request & Response Logging Filters

The use of a filter over writing independent lines of `Logger.info()` makes it such that if log formats need to be changed or frameworks need to be switched, it can be done through a single class to propagate throughout the system automatically. Filters also ensure that whenever new endpoints or functions are added, logging is automatically generated for it, rather than leaving the possibility for a developer to miss adding a few logging lines when adding functionality. This also ensures that resources are focused only on their logic, rather than being bloated with logging calls that could otherwise be written in one place.