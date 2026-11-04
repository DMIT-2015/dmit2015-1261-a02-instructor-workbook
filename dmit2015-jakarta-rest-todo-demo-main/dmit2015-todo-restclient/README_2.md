# DMIT2015 — REST Client

README 2 of 3: Service Layer and Jakarta Faces Backing Bean



## Introduction — What are we building in this part?

In README 1, we created a MicroProfile REST Client that knows how to send HTTP requests to our existing REST API.

Now we need to connect that REST Client to the Jakarta Faces user interface.

We'll add two layers:

- Service layer: Gives our application simple methods for creating, reading, updating, and deleting to-do items.
- Faces backing bean: Holds the data displayed on the page and handles button actions such as Save and Delete.


## Part 1 — Create the service interface

### Why do we need it?

The REST Client deals with HTTP requests. The service interface gives the rest of our application a simpler way to work with to-do items.

For example, the Faces backing bean can call `getAllTodoItems()` without needing to know about `@GET` or HTTP URLs.

### Step 1 — Generate TodoItemService

In IntelliJ IDEA:

1. Under `src/main/java/dmit2015`, create a package named `service`.
2. Inside it, create a Java interface named `TodoItemService`.
3. Select the DMIT2015 Model Service Interface template.

The template expects `TodoItem` to be inside a `model` package.


### Step 2 — Check the generated service interface

File: `src/main/java/dmit2015/service/TodoItemService.java`


```java
package dmit2015.service;

import dmit2015.model.TodoItem;

import java.util.List;
import java.util.Optional;

public interface TodoItemService {

    TodoItem createTodoItem(TodoItem todoItem);

    Optional<TodoItem> getTodoItemById(Long id);

    List<TodoItem> getAllTodoItems();

    TodoItem updateTodoItem(TodoItem todoItem);

    void deleteTodoItemById(Long id);
}
```

### Understand the code

| Method                 | Purpose                     |
| ---------------------- | --------------------------- |
| `createTodoItem()`     | Creates a new item          |
| `getTodoItemById()`    | Finds one item              |
| `getAllTodoItems()`    | Retrieves the complete list |
| `updateTodoItem()`     | Updates an existing item    |
| `deleteTodoItemById()` | Deletes an item             |

`Optional<TodoItem>` represents a result that might not contain an item.

The interface only defines the methods. Next, we'll create the class that implements them.

## Part 2 — Implement the service using MicroProfile REST Client

### Why do we need it?

`TodoItemService` defines what our application can do. We now need a class that defines how those operations are performed.

Instead of accessing a database directly, this implementation will call the REST Client from README 1.

### Step 4 — Create MpRestClientTodoItemService

Inside `dmit2015.service`, create a Java class named:

`MpRestClientTodoItemService`

File: `src/main/java/dmit2015/service/MpRestClientTodoItemService.java`

Start with the class and dependency injection:

```java
package dmit2015.service;

import dmit2015.model.TodoItem;
import dmit2015.restclient.TodoItemMpRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Named("currentMpRestClientTodoItemService")
@ApplicationScoped
public class MpRestClientTodoItemService {

    @Inject
    @RestClient
    private TodoItemMpRestClient mpRestClient;

}
```

Why these annotations?

- `@Named` gives the service a name that the Faces backing bean can use to inject it.
- `@ApplicationScoped` makes the service application-scoped.
- `@Inject` and `@RestClient` tell CDI to provide the MicroProfile REST Client implementation.
- `implements TodoItemService` means this class must implement the service interface's methods.

### Step 5 — Implement createTodoItem()

Add this method inside the class:

```java
@Override
public TodoItem createTodoItem(TodoItem todoItem) {
    try (Response response = mpRestClient.create(todoItem)) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        } else {
            String location = response.getHeaderString("Location");
            int resourceIdIndex = location.lastIndexOf("/") + 1;
            Long resourceId = Long.parseLong(location.substring(resourceIdIndex));
            todoItem.setId(resourceId);
        }
    }
    return todoItem;
}
```

### Why is this method longer?

When the REST API creates an item, it returns HTTP 201 Created and a `Location` header containing the new item's URL.

The method:

1. Calls the REST API using `mpRestClient.create(todoItem)`.
2. Checks that the response status is `201`.
3. Reads the `Location` header.
4. Extracts the new item's ID from the end of the URL.
5. Sets that ID on the Java object and returns it.

The `try` statement also closes the HTTP response after use.

### Step 6 — Implement the remaining CRUD methods

Add:

```java
@Override
public Optional<TodoItem> getTodoItemById(Long id) {
    return mpRestClient.findById(id);
}

@Override
public List<TodoItem> getAllTodoItems() {
    return mpRestClient.findAll();
}

@Override
public TodoItem updateTodoItem(TodoItem todoItem) {
    return mpRestClient.update(todoItem.getId(), todoItem);
}

@Override
public void deleteTodoItemById(Long id) {
    mpRestClient.delete(id);
}
```

Why are these methods short? The MicroProfile REST Client already handles the HTTP requests. The service only needs to call the appropriate method.

For updates, `todoItem.getId()` identifies which record to modify.

At this point, all five methods in `TodoItemService` have implementations.

## Part 3 — Create the Jakarta Faces backing bean

### Why do we need it?

Our service can now communicate with the REST API, but the web page still needs a way to display data and respond to user actions.

A Faces backing bean connects the page to the service. It stores the list of to-do items, keeps track of the selected item, and handles actions such as New, Save, and Delete.

### Step 7 — Generate TodoItemCrudView

In IntelliJ:

1. Under `src/main/java/dmit2015`, create a package named `view`.
2. Create a Java class using the DMIT2015 Faces CRUD Bean template.
3. Use the following values when prompted:

| Template setting  | Value                                |
| ----------------- | ------------------------------------ |
| Model class       | `TodoItem`                           |
| Primary key type  | `Long`                               |
| Service bean name | `currentMpRestClientTodoItemService` |

Name the generated class `TodoItemCrudView`.

File: `src/main/java/dmit2015/view/TodoItemCrudView.java`

The template generates most of the code. We aren't writing all the event handlers from scratch.

### Step 8 — Understand the generated backing bean

The class uses these annotations and fields:

```java
@Named("currentTodoItemCrudView")
@ViewScoped
public class TodoItemCrudView implements Serializable {

    @Inject
    @Named("currentMpRestClientTodoItemService")
    private TodoItemService todoItemService;

    @Getter
    @Setter
    private TodoItem selectedTodoItem;

    @Getter
    @Setter
    private Long selectedId;

    @Getter
    private List<TodoItem> todoItems;
```

What they do:

- `@Named("currentTodoItemCrudView")` lets the XHTML page access this bean using Jakarta Faces expressions.
- `@ViewScoped` keeps the bean's state while the user interacts with the same page.
- `@Inject` provides the service implementation.
- `selectedTodoItem` stores the item being created or edited.
- `selectedId` identifies the selected record.
- `todoItems` stores the list displayed in the table.

The generated class also contains these methods:

| Method              | When it's used                              |
| ------------------- | ------------------------------------------- |
| `init()`            | Loads the list when the view is initialized |
| `onOpenNew()`       | Prepares a new empty item                   |
| `onGenerateData()`  | Generates sample data                       |
| `onSave()`          | Creates or updates an item                  |
| `onDelete()`        | Deletes the selected item                   |
| `handleException()` | Formats exception messages                  |

For example, the generated `init()` method calls the service:

```java
@PostConstruct
public void init() {
    try {
        todoItems = todoItemService.getAllTodoItems();
    } catch (Exception e) {
        Messages.addGlobalError("Error getting todoItems %s", e.getMessage());
    }
}
```

Why: When the view is initialized, the backing bean retrieves the to-do items so the page can display them.

The Save handler also uses `selectedId` to decide which operation to perform:

```java
if (selectedId == null) {
    TodoItem createdTodoItem = todoItemService.createTodoItem(selectedTodoItem);
    Messages.addGlobalInfo("Create was successful. {0}", createdTodoItem.getId());
    selectedTodoItem = null;
} else {
    todoItemService.updateTodoItem(selectedTodoItem);
    Messages.addGlobalInfo("Update was successful");
}
```

Why: A new item has no selected ID, so the handler creates it. An existing item has an ID, so the handler updates it.

The generated code also refreshes the table using PrimeFaces AJAX and closes the dialog after a successful save.

### Step 9 — Add the DataFaker method to TodoItem

 

```java
selectedTodoItem = TodoItem.of(faker);
```

That method doesn't exist in our model yet.

Open:

`src/main/java/dmit2015/model/TodoItem.java`

Add the import:

```java
import net.datafaker.Faker;
```

Then add this method inside the class:

```java
public static TodoItem of(Faker faker) {
    TodoItem newItem = new TodoItem();
    newItem.setName("Watch  " + faker.breakingBad().episode());
    newItem.setComplete(false);
    return newItem;
}
```

Why: DataFaker generates a sample task name. The method creates a `TodoItem` with that name and sets `complete` to `false`.

The backing bean can now generate sample data without manually entering a task name.

## What did we accomplish?

We connected our MicroProfile REST Client to a service layer, generated a Faces backing bean to handle CRUD actions, and added the DataFaker method required by that bean.

The Java side of our application is now ready for the user interface.

**Next — README 3:** We'll generate the PrimeFaces CRUD page, configure its fields and table columns, correct the layout location, add navigation, and run the application.
