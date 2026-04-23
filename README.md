# Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey) and Grizzly for managing rooms and sensors across a university smart campus. The system supports full CRUD operations for rooms and sensors, historical sensor readings, filtered queries, and comprehensive error handling.

---

## Table of Contents

- [API Overview](#api-overview)
- [Project Setup & Launch](#project-setup--launch)
- [Sample curl Commands](#sample-curl-commands)
- [Part 1: Service Architecture & Setup](#part-1-service-architecture--setup)
- [Part 2: Room Management](#part-2-room-management)
- [Part 3: Sensor Operations and Linking](#part-3-sensor-operations-and-linking)
- [Part 4: Deep Nesting with Sub-Resources](#part-4-deep-nesting-with-sub-resources)
- [Part 5: Advanced Error Handling, Exception Mapping & Logging](#part-5-advanced-error-handling-exception-mapping--logging)

---

## API Overview

### Architecture

The API is built using **JAX-RS** with a **Jersey** implementation, served via an embedded **Grizzly HTTP server**. All data is stored in-memory using `ConcurrentHashMap` structures managed by a central `DataStore` singleton.

### Resource Hierarchy

```
/api/v1
│
├── /rooms
│   ├── GET    /              - List all rooms
│   ├── POST   /              - Create a room
│   ├── GET    /{roomId}      - Get a specific room
│   ├── PUT    /{roomId}      - Update a room
│   └── DELETE /{roomId}      - Delete a room (blocked if sensors assigned)
│
├── /sensors
│   ├── GET    /              - List all sensors (supports ?type= and ?status= filters)
│   ├── POST   /              - Create a sensor (validates roomId exists)
│   ├── GET    /{sensorId}    - Get a specific sensor
│   ├── PUT    /{sensorId}    - Update a sensor
│   ├── DELETE /{sensorId}    - Delete a sensor
│   └── /{sensorId}/readings
│       ├── GET  /            - Get all readings for a sensor
│       ├── POST /            - Add a new reading (blocked if sensor is MAINTENANCE)
│       └── GET  /latest      - Get the most recent reading
│
└── GET /                     - Discovery endpoint (API metadata + resource links)
```

### Data Models

**Room**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": ["TEMP-001", "CO2-002"]
}
```

**Sensor**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.4,
  "roomId": "LIB-301"
}
```

**SensorReading**
```json
{
  "id": "uuid-generated",
  "timestamp": 1713000000000,
  "value": 21.4
}
```

### Error Handling

| Scenario | Exception | HTTP Status |
|---|---|---|
| Room deleted with sensors assigned | `RoomNotEmptyException` | 409 Conflict |
| Sensor created with non-existent roomId | `LinkedResourceNotFoundException` | 422 Unprocessable Entity |
| Reading posted to MAINTENANCE sensor | `SensorUnavailableException` | 403 Forbidden |
| Any unexpected runtime error | `GlobalExceptionMapper` | 500 Internal Server Error |

---

## Project Setup & Launch

### Prerequisites

Ensure the following are installed before proceeding:

- **Java JDK 23** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.8+** — [Download here](https://maven.apache.org/download.cgi)

Verify your installations by running:
```bash
java -version
mvn -version
```

### Step 1 — Clone the repository

```bash
git clone https://github.com/Dilagante/SmartCampusSystem.git
cd smart-campus-api
```

### Step 2 — Build the project

```bash
mvn clean install
```

Maven will download all dependencies (Jersey, Grizzly, Jackson) and compile the project. A successful build will print `BUILD SUCCESS`.

### Step 3 — Run the server

```bash
mvn exec:java "-Dexec.mainClass=com.smartcampus.api.Main"
```

Alternatively, if you have already built the JAR:
```bash
java -cp target/SmartCampusSystem-1.0-SNAPSHOT.jar com.smartcampus.api.Main
```

### Step 4 — Confirm the server is running

You should see the following output in your terminal:
```
Smart Campus API started and listening at http://localhost:8080/api/v1/
```

Open a browser or Postman and visit:
```
http://localhost:8080/api/v1
```

You should receive the discovery response with API metadata and resource links.

### Step 5 — Stopping the server

Press `Ctrl + C` in the terminal to shut down the Grizzly server.

---

## Sample curl Commands

> All commands assume the server is running on `http://localhost:8080`. On Windows, replace single quotes with double quotes and escape inner double quotes with `\"`.

### 1. Discover the API

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

**Expected response:** `200 OK` with API version, contact details, and a map of available resource endpoints.

---

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

**Expected response:** `201 Created` with the full room object.

---

### 3. Create a Sensor linked to that Room

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "roomId": "LIB-301"
  }'
```

**Expected response:** `201 Created` with the full sensor object.

---

### 4. Post a Reading to the Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 21.4
  }'
```

**Expected response:** `201 Created` with the new reading and updated `sensorCurrentValue`.

---

### 5. Retrieve all Sensors filtered by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

**Expected response:** `200 OK` with a list containing only Temperature sensors.

---

### 6. Attempt to delete a Room that still has Sensors (safety constraint)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected response:** `409 Conflict` with a JSON error explaining the room still has sensors assigned.

---

### 7. Set a Sensor to MAINTENANCE then attempt to post a Reading

```bash
# First update the sensor status
curl -X PUT http://localhost:8080/api/v1/sensors/TEMP-001 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "MAINTENANCE"
  }'

# Then try to add a reading — this should be blocked
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.0
  }'
```

**Expected response:** `403 Forbidden` with a JSON error and a hint to restore the sensor to ACTIVE.

---

### 8. Attempt to create a Sensor with a non-existent Room

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-999",
    "type": "CO2",
    "status": "ACTIVE",
    "roomId": "DOES-NOT-EXIST"
  }'
```

**Expected response:** `422 Unprocessable Entity` explaining the referenced room could not be found.

---

## Part 1: Service Architecture & Setup

### 1. Project & Application Configuration

By default, JAX-RS creates a new resource instance for every incoming HTTP request. This reflects HTTP's stateless nature. Since instances are destroyed immediately after the request, any data stored inside would be lost.

To solve this, the system uses a **Singleton Pattern**. By using a single, central `DataStore` instance to hold the data permanently, it survives across multiple requests. Additionally, since many clients may access the server at the same time, the system uses `ConcurrentHashMap` instead of a standard `HashMap` to safely manage the data and prevent race conditions.

### 2. The "Discovery" Endpoint

**Hypermedia** is considered a hallmark of advanced RESTful design since it indicates what actions are possible in addition to providing data.

This approach benefits client developers rather than the use of static documentation through the reduction of client-side logic, as complex business rules don't need to be hardcoded based on documentation. In addition, hypermedia APIs are self-documenting, ensuring that documentation is never out of date. A client-side developer can simply inspect a response and immediately see all available next steps and workflows.

---

## Part 2: Room Management

### 1. Room Resource Implementation

Returning only IDs is efficient for the user's bandwidth and consumes fewer resources on the server, but forces the client to constantly make an additional request every time it needs to get a specific room. Returning full objects may take more resources per request, but lets the client render a complete room listing in one request, especially since clients would rarely fetch a list of IDs without the need for the rest of the data. If bandwidth becomes a concern at a large scale, pagination can be an accepted solution.

### 2. Room Deletion & Safety Logic

The delete operation is not idempotent in this implementation. The first request would delete the room and return `204 No Content`, but subsequent requests will return `404 Not Found` as there is no such room to delete. Since the response code changes between calls, the operation cannot be considered idempotent.

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