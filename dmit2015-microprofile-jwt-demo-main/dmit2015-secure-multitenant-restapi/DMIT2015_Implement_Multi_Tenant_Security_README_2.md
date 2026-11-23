# Lesson 30 — Secure Multi-Tenant REST API

## README 2 — Implement Multi-Tenant Security


By the end of this lesson, you should be able to:

- Configure MicroProfile JWT authentication for a REST API.
- Assign record ownership using the authenticated username.
- Filter database queries by username.
- Combine role-based authorization with ownership checks.
- Prevent users from retrieving, updating, or deleting another user's TodoItems.

## 1. Open the Lesson 30 Project

Open the existing project in IntelliJ IDEA:

`dmit2015-secure-multitenant-restapi`

All changes in this README belong to this project.

Keep your completed Lesson 29 project, `dmit2015-secure-rbac-restapi`, unchanged.

You will modify these files:

| File | Purpose |
|---|---|
| `TodoItem.java` | Store the record owner |
| `TodoItemDto.java` | Represent ownership in API responses |
| `TodoItemMapper.java` | Map ownership between entity and DTO |
| `TodoItemRepository.java` | Filter records by username |
| `TodoItemDtoResource.java` | Enforce role and ownership restrictions |
| `JaxRsApplication.java` | Enable JWT authentication |
| `TodoItemInitializer.java` | Update initial sample records |

You will also configure JWT verification and role-based security.

## 2. Configure JWT Authentication

In Lesson 29, you configured Keycloak and MicroProfile JWT.

We will use the same Keycloak server and realm for Lesson 30.

### Step 1 — Update JaxRsApplication.java

Open:

`src/main/java/dmit2015/config/JaxRsApplication.java`

Replace its contents with:

```java
package dmit2015.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"Sales", "Shipping", "Administration"})
@ApplicationPath("/restapi")
public class JaxRsApplication extends Application {
}
```

The `@LoginConfig` annotation enables MicroProfile JWT authentication.

The `@DeclareRoles` annotation declares the roles used by this application.

### Step 2 — Configure JWT Verification

Create this file if it does not exist:

`src/main/resources/META-INF/microprofile-config.properties`

Add:

```properties
mp.jwt.verify.issuer=http://localhost:8180/realms/dmit2015-realm
mp.jwt.verify.publickey.location=http://localhost:8180/realms/dmit2015-realm/protocol/openid-connect/certs
```

These values use the Keycloak realm configured during Lesson 29.

If your Keycloak realm differs, use the `issuer` and `jwks_uri` values from its OpenID Connect discovery document instead.

### Step 3 — Enable Role-Based Security

Open:

`src/main/webapp/WEB-INF/web.xml`

Add the following context parameter inside the existing `<web-app>` element:

```xml
<context-param>
    <param-name>resteasy.role.based.security</param-name>
    <param-value>true</param-value>
</context-param>
```

Do not replace the entire `web.xml` file.

## 3. Add Ownership to the TodoItem Entity

Open:

`src/main/java/dmit2015/entity/TodoItem.java`

Add this field inside the `TodoItem` class:

```java
@NotBlank(message = "Username cannot be blank.")
@Column(nullable = false, length = 100)
private String username;
```

The existing Lombok `@Getter` and `@Setter` annotations will generate `getUsername()` and `setUsername()`.

Every new TodoItem must have an owner.

The owner will be assigned by the REST API using the authenticated JWT, not by trusting the request body.

## 4. Update the TodoItem DTO

Open:

`src/main/java/dmit2015/dto/TodoItemDto.java`

Add:

```java
private String username;
```

The DTO will now include the owner in API responses.

**Important:** Although the DTO contains `username`, the server must not trust this value when creating or updating a TodoItem.

Because the DTO uses Lombok `@AllArgsConstructor`, adding this field also changes its generated all-arguments constructor.

## 5. Update the TodoItem Mapper

Open:

`src/main/java/dmit2015/mapper/TodoItemMapper.java`

Add a username mapping to both existing `@Mappings` annotations.

For `toDto()`:

```java
@Mapping(target = "username", source = "username"),
```

For `toEntity()`:

```java
@Mapping(target = "username", source = "username"),
```

Keep the existing mappings for `id`, `task`/`name`, `done`/`complete`, and `version`.

MapStruct will now transfer the username between the entity and DTO.

The resource must still overwrite the owner when creating a record and must not change the stored owner during updates.

## 6. Add an Owner-Filtered Repository Query

Open:

`src/main/java/dmit2015/repository/TodoItemRepository.java`

Add the following method:

```java
public List<TodoItem> findAllByUsername(String username) {
    return entityManager.createQuery(
            "SELECT o FROM TodoItem o WHERE o.username = :username",
            TodoItem.class
        )
        .setParameter("username", username)
        .getResultList();
}
```

This query retrieves only TodoItems belonging to the specified username.

Keep the existing `findAll()` method because Administration users need to retrieve all records.

## 7. Update the Initial Sample Data

Open:

`src/main/java/dmit2015/config/TodoItemInitializer.java`

The initializer currently creates three sample TodoItems without usernames.

Because the `username` field is now required, assign an owner to each sample record before calling `todoItemRepository.add()`.

For example:

```java
todo1.setUsername("colsen");
todo2.setUsername("colsen");
todo3.setUsername("colsen");
```

Use a username that exists in your Keycloak realm.

These are demonstration records. In normal REST requests, ownership must come from the authenticated JWT.

If the application database already contains TodoItems without owners, those records must be migrated or the development database reset before enforcing the non-null constraint.

## 8. Read the Authenticated Username

Open:

`src/main/java/dmit2015/resource/TodoItemDtoResource.java`

Add these imports:

```java
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ForbiddenException;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
```

The class already imports `jakarta.inject.Inject` and `java.util.Optional`.

Inside the resource class, add:

```java
@Inject
@Claim(standard = Claims.upn)
private ClaimValue<Optional<String>> optionalUsername;

@Inject
private JsonWebToken jwt;
```

Add a helper method to retrieve the authenticated username:

```java
private String getAuthenticatedUsername() {
    return optionalUsername.getValue()
            .orElseThrow(() ->
                new ForbiddenException("Username claim is required."));
}
```

The `upn` claim identifies the authenticated user.

The `jwt` object also allows the resource to inspect the authenticated user's roles.

## 9. Add an Ownership Check

Inside `TodoItemDtoResource`, add:

```java
private void checkOwnership(TodoItem todoItem) {
    String username = getAuthenticatedUsername();

    if (!username.equals(todoItem.getUsername())) {
        throw new ForbiddenException(
            "You cannot access another user's TodoItem.");
    }
}
```

This helper will be used when retrieving, updating, and deleting individual records.

For read operations, Administration is allowed to access all records. Add a separate helper:

```java
private void checkReadAccess(TodoItem todoItem) {
    if (!jwt.getGroups().contains("Administration")) {
        checkOwnership(todoItem);
    }
}
```

The Administration exception applies only to reading records.

## 10. Secure the POST Endpoint

Locate the existing `postTodoItem()` method.

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
```

Immediately after converting the DTO to an entity, assign the owner. Before URI todoItemsUri add the code below:

```java
TodoItem newTodoItem = TodoItemMapper.INSTANCE.toEntity(dto);

newTodoItem.setUsername(getAuthenticatedUsername());

todoItemRepository.add(newTodoItem);
```

Keep the existing validation and `201 Created` response.

Even if a client submits another username, the API overwrites it with the authenticated username.

## 11. Secure GET All

Locate `getTodoItems()`.

Replace the method with:

```java
@GET
@RolesAllowed({"Sales", "Shipping", "Administration"})
public Response getTodoItems() {
    boolean isAdministration =
            jwt.getGroups().contains("Administration");

    var todoItems = isAdministration
            ? todoItemRepository.findAll()
            : todoItemRepository.findAllByUsername(
                    getAuthenticatedUsername());

    return Response.ok(
            todoItems.stream()
                    .map(TodoItemMapper.INSTANCE::toDto)
                    .collect(Collectors.toList())
    ).build();
}
```

This method applies the required behavior:

- Sales users see only their TodoItems.
- Shipping users see only their TodoItems.
- Administration users see all TodoItems.

## 12. Secure GET by ID

Locate `getTodoItem()`.

Add:

```java
@RolesAllowed({"Sales", "Shipping", "Administration"})
```

After loading the existing TodoItem and confirming it exists, before TodoItemDto dto add below code:

```java
TodoItem existingTodoItem = optionalTodoItem.get();

checkReadAccess(existingTodoItem);
```

Keep the existing DTO mapping and response.

A regular user who requests another user's TodoItem will receive `403 Forbidden`.

An Administration user may retrieve any existing TodoItem.

## 13. Secure PUT

Locate `updateTodoItem()`.

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
```

After retrieving the existing TodoItem, add the ownership check **before changing any fields** before  if (!Objects.equals(existingTodoItem.getVersion(), dto.getVersion())):

```java
TodoItem existingTodoItem = optionalTodoItem.orElseThrow();

checkOwnership(existingTodoItem);
```

Keep the existing version check and update logic.

Do not copy `dto.getUsername()` into the existing entity.

The stored username must remain unchanged.

Also ensure that the returned DTO reflects the stored owner rather than a client-supplied username. Replace the final success response with:

```java
return Response.ok(
        TodoItemMapper.INSTANCE.toDto(existingTodoItem)
).build();
```

The repository's existing `update()` method already updates only the task and completion fields, which helps preserve ownership.

## 14. Secure DELETE

Locate `deleteTodoItem()`.

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
```

After loading the existing TodoItem and confirming it exists, before todoItemRepository.deleteById(id) add:

```java
TodoItem existingTodoItem = optionalTodoItem.get();

checkOwnership(existingTodoItem);
```

Then keep the existing deletion:

```java
todoItemRepository.deleteById(id);

return Response.noContent().build();
```

A user can now delete only their own TodoItems.

Administration users cannot delete TodoItems because the DELETE endpoint does not permit the Administration role.

## 15. Review the Completed Security Rules

Your resource methods should now have the following annotations and ownership behavior:

| Method | Allowed Roles | Ownership Check |
|---|---|---|
| `postTodoItem()` | Sales, Shipping | Server assigns owner |
| `getTodoItems()` | Sales, Shipping, Administration | Filter by owner, except Administration |
| `getTodoItem()` | Sales, Shipping, Administration | Owner or Administration |
| `updateTodoItem()` | Sales, Shipping | Owner only |
| `deleteTodoItem()` | Sales, Shipping | Owner only |

The REST API must enforce both the role and ownership restrictions.

## 16. Run the Application

Confirm that Keycloak is running.

In the Lesson 30 project terminal, run:

```bash
mvn wildfly:dev
```

The project's configured REST API port is `8091`.

The resource URL is:

```text
http://localhost:8091/restapi/todos
```

## What's Next?

In **README 3 — Test Multi-Tenant Security**, you will use IntelliJ HTTP Client and multiple Keycloak users to verify:

- JWT authentication.
- Role-based access.
- User-specific TodoItem filtering.
- Cross-user access denial.
- Administration read-only access.
- HTTP `401`, `403`, and successful CRUD responses.