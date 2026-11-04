# DMIT2015 — REST Client


## Introduction — What are we building today?

In the previous lesson, we created a REST API that lets us create, read, update, and delete to-do items.

Today, we're building a separate Jakarta EE web application that communicates with that REST API.

Instead of accessing the database directly, our new application will send HTTP requests to the existing API.

In this README, we'll create three things:

1. `TodoItem` — a Java class representing the JSON data returned by the API.
2. `TodoItemMpRestClient` — an interface that defines HTTP requests to the API.
3. `BadRequestResponseMapper` — a class that handles validation errors returned by the API.


## Part 1 — Create the TodoItem model

### Why do we need it?

Our REST API sends and receives JSON. The client application needs a Java class with matching properties so MicroProfile can convert JSON into Java objects and vice versa.

### Step 1 — Create the class

In IntelliJ IDEA, open the `dmit2015-todo-restclient` module.

Under `src/main/java/dmit2015`, create a package called `restclient`. Inside it, create a Java class named `TodoItem`.

File: `src/main/java/dmit2015/restclient/TodoItem.java`

```java
package dmit2015.restclient;

import lombok.Data;

@Data
public class TodoItem {

    private Long id;
    private String name;
    private boolean complete;
    private Integer version;

}
```

### Understand the code

| Code       | Purpose                                                     |
| ---------- | ----------------------------------------------------------- |
| `@Data`    | Lombok generates getters, setters, and other common methods |
| `id`       | Identifies a to-do item                                     |
| `name`     | Stores the task description                                 |
| `complete` | Indicates whether the task is finished                      |
| `version`  | Tracks the record's version during updates                  |


## Part 2 — Create the MicroProfile REST Client

### Why do we need it?

Normally, calling a REST API from Java requires writing code to build HTTP requests and process responses.

MicroProfile REST Client simplifies this. We create an interface, annotate its methods with HTTP operations, and MicroProfile provides the implementation.

### Step 2 — Create the interface

Inside `dmit2015.restclient`, create a Java interface named `TodoItemMpRestClient`.

File: `src/main/java/dmit2015/restclient/TodoItemMpRestClient.java`

Start with:

```java
package dmit2015.restclient;

import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RequestScoped
@RegisterRestClient
public interface TodoItemMpRestClient {

}
```

Why these annotations?

- `@RegisterRestClient` tells MicroProfile to create a REST Client implementation for this interface.
- `@RequestScoped` defines its CDI scope.


### Step 3 — Configure the API URL

Before adding the HTTP methods, tell MicroProfile which server to contact.

Under the REST Client module, open:

`src/main/resources/META-INF/`

Create a file named `microprofile-config.properties` if it doesn't already exist.

Add:

```properties
dmit2015.restclient.TodoItemMpRestClient/mp-rest/url=http://localhost:8090/restapi/todos
```

Why? This configuration connects our interface to the existing REST API running on port `8090`.

Keeping the URL in a configuration file means we don't have to hard-code it into every method.

### Step 4 — Add the CRUD methods

Now we'll define the HTTP operations.

#### 4.1 Create an item — POST

Add the required JAX-RS imports:

```java
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
```

Inside the interface, add:

```java
@POST
Response create(TodoItem newTodoItem);
```

Why: `@POST` creates a new record. Returning `Response` lets us access the HTTP status and the `Location` header containing the new item's URL.

#### 4.2 Get all items — GET

Add:

```java
import java.util.List;
```

Then add:

```java
@GET
List<TodoItem> findAll();
```

Why: The API returns a JSON array, which MicroProfile converts into a `List<TodoItem>`.

#### 4.3 Get one item by ID — GET

Add:

```java
import java.util.Optional;
```

Then add:

```java
@GET
@Path("/{id}")
Optional<TodoItem> findById(@PathParam("id") Long id);
```

Why: `@Path("/{id}")` adds the ID to the URL, and `@PathParam("id")` supplies its value.

For example, an ID of `1` produces:

`http://localhost:8090/restapi/todos/1`

#### 4.4 Update an item — PUT

Add:

```java
@PUT
@Path("/{id}")
TodoItem update(@PathParam("id") Long id, TodoItem updatedTodoItem);
```

Why: The ID identifies which item to update, while `updatedTodoItem` contains the updated data sent to the API.

#### 4.5 Delete an item — DELETE

Add:

```java
@DELETE
@Path("/{id}")
void delete(@PathParam("id") Long id);
```

Why: This sends a DELETE request for the specified item. The method returns `void` because the successful response has no body.

### Step 5 — Check the completed interface so far

At this point, before error handling is added, `TodoItemMpRestClient.java` should contain:

```java
package dmit2015.restclient;

import dmit2015.model.TodoItem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Optional;

@RequestScoped
@RegisterRestClient
public interface TodoItemMpRestClient {

    @POST
    Response create(TodoItem newTodoItem);

    @GET
    List<TodoItem> findAll();

    @GET
    @Path("/{id}")
    Optional<TodoItem> findById(@PathParam("id") Long id);

    @PUT
    @Path("/{id}")
    TodoItem update(@PathParam("id") Long id, TodoItem updatedTodoItem);

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") Long id);

}
```

`TodoItem` is still in the `restclient` package, and the error mapper hasn't been registered yet.

## Part 3 — Handle HTTP 400 validation errors

### Why do we need it?

Our REST API validates incoming data. For example, if someone tries to save a to-do item with a blank name, the API can return HTTP 400 Bad Request along with validation messages.

We want our client application to read those messages and display them to the user instead of showing an unhelpful error.

MicroProfile lets us customize how error responses are handled using a `ResponseExceptionMapper`.

### Step 6 — Create BadRequestResponseMapper

In the `dmit2015.restclient` package, create a Java class named `BadRequestResponseMapper`.

File: `src/main/java/dmit2015/restclient/BadRequestResponseMapper.java`



```java
package dmit2015.restclient;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.omnifaces.util.Messages;

import java.util.HashMap;
import java.util.Map;

public class BadRequestResponseMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(Response response) {
        String responseBodyJson = response.readEntity(String.class);
        Map<String, String> responseBodyMap;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            responseBodyMap = jsonb.fromJson(responseBodyJson, new HashMap<String, String>() {}.getClass().getGenericSuperclass());
            responseBodyMap.forEach((key, value) -> Messages.create(value).error().add());
        } catch (Exception ignored) {

        }
        return null;
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == 400;
    }
}
```

### Understand the important code

| Code                                   | Purpose                                                          |
| -------------------------------------- | ---------------------------------------------------------------- |
| `ResponseExceptionMapper`              | Allows us to handle HTTP error responses                         |
| `handles(...)`                         | Selects responses with status `400`                              |
| `readEntity(String.class)`             | Reads the API's error response as a JSON string                  |
| `JsonbBuilder.create()`                | Creates a JSON-B converter                                       |
| `jsonb.fromJson(...)`                  | Converts the JSON into a Java map                                |
| `Messages.create(value).error().add()` | Adds the validation messages to the Jakarta Faces message system |



### Step 7 — Register the mapper

Creating the mapper class is not enough. We must tell the REST Client interface to use it.

Open `TodoItemMpRestClient.java` and add this import:

```text
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
```

Add the annotation above the interface:

```java
@RequestScoped
@RegisterProvider(BadRequestResponseMapper.class)
@RegisterRestClient
public interface TodoItemMpRestClient {
```

Why: `@RegisterProvider` connects our error-handling class to this REST Client. When an HTTP 400 response is received, the mapper can process its validation messages.

We don't need to change the five CRUD methods.

## What did we accomplish?

We created the Java model used to exchange data with the REST API, defined the five CRUD HTTP operations using MicroProfile REST Client, configured the API URL, and added handling for validation errors.

**Next — README 2:** We'll create the service layer and Jakarta Faces backing bean. That's when we moves `TodoItem` into the `model` package and later adds the DataFaker method required by the generated backing bean.
