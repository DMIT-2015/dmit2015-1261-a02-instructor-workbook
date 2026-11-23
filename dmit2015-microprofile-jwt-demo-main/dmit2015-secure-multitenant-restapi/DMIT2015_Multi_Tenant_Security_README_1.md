# Lesson 30 — Secure Multi-Tenant REST API

## README 1 — Introduction to Multi-Tenant Security and Project Setup

## 1. Open the Lesson 30 Project

In IntelliJ IDEA, open the existing Maven project:

`dmit2015-secure-multitenant-restapi`

This project is separate from the Lesson 29 project, `dmit2015-secure-rbac-restapi`.

The Lesson 30 project contains an existing TodoItem REST API. You will extend it with JWT authentication, role-based authorization, and record ownership.

## 2. Review the Existing REST API

Examine these files in `src/main/java`:

| File | Purpose |
| --- | --- |
| `dmit2015/entity/TodoItem.java` | Defines the TodoItem database entity |
| `dmit2015/dto/TodoItemDto.java` | Defines the TodoItem DTO |
| `dmit2015/mapper/TodoItemMapper.java` | Maps between the entity and DTO |
| `dmit2015/repository/TodoItemRepository.java` | Performs database CRUD operations |
| `dmit2015/resource/TodoItemDtoResource.java` | Defines the REST endpoints |
| `dmit2015/config/JaxRsApplication.java` | Configures the REST application |

The TodoItem entity currently contains fields such as:

```java
private Long id;
private String task;
private boolean done;
private Integer version;
```

The DTO uses `name` and `complete` for the corresponding task and completion values.

The project already supports CRUD operations, but the current implementation does not enforce record ownership.

## 3. Review the REST Endpoints

Open:

`http_request/TodoItemDtoHttpRequest.http`

The application uses port `8091` and the resource path `/restapi/todos`.

| HTTP Method | Endpoint | Operation |
| --- | --- | --- |
| GET | `/restapi/todos` | Retrieve all TodoItems |
| GET | `/restapi/todos/{id}` | Retrieve a TodoItem by ID |
| POST | `/restapi/todos` | Create a TodoItem |
| PUT | `/restapi/todos/{id}` | Update a TodoItem |
| DELETE | `/restapi/todos/{id}` | Delete a TodoItem |

The complete base URL, when deployed at the root context, is:

```text
http://localhost:8091/restapi/todos
```

The existing HTTP requests do not yet include JWT bearer tokens. You will update them later in this lesson.

## 4. Understand Multi-Tenant Security

In Lesson 29, you implemented role-based access control using `@RolesAllowed`.

RBAC determines whether a user has permission to perform an operation.

Multi-tenant security adds another restriction: **which records the user is allowed to access**.

Consider the following example:

| TodoItem ID | Task | Owner |
| --- | --- | --- |
| 1 | Complete Assignment 7 | colsen |
| 2 | Review REST Client | shipping-user |
| 3 | Test JWT Authentication | colsen |

If `colsen` retrieves TodoItems, the API should return records 1 and 3.

If `shipping-user` retrieves TodoItems, the API should return record 2.


## 5. Review the Required Roles and Permissions

The Lesson 30 project defines three roles:

- `Sales` — can create TodoItems and manage their own records.
- `Shipping` — can create TodoItems and manage their own records.
- `Administration` — can read TodoItems belonging to all users, but cannot modify them.

The required permissions are:

| Operation | Sales | Shipping | Administration |
| --- | --- | --- | --- |
| GET all | Own only | Own only | All |
| GET by ID | Own only | Own only | Any |
| POST | Allowed | Allowed | Denied |
| PUT | Own only | Own only | Denied |
| DELETE | Own only | Own only | Denied |

Notice that the Administration role is **read-only** in Lesson 30.

## 6. Understand the JWT `upn` Claim

In Lesson 29, you used Keycloak to issue JWT access tokens containing user information and roles.

In Lesson 30, the REST API will use the `upn` claim to identify the authenticated username.

MicroProfile JWT supports injecting this claim:

```java
@Inject
@Claim(standard = Claims.upn)
private ClaimValue<Optional<String>> optionalUsername;
```

The username can then be retrieved:

```java
String username = optionalUsername.getValue().orElseThrow();
```

The API must use the username from the verified JWT when assigning or checking record ownership.



## 7. Understand the Required Ownership Rules

You will implement these rules in the next README:

**Creating TodoItems**

The server assigns the authenticated username as the owner of every newly created TodoItem.

**Retrieving TodoItems**

Sales and Shipping users can retrieve only their own records. Administration users can retrieve all records.

**Retrieving by ID**

A regular user must not be able to retrieve another user's record by guessing its ID.

**Updating and deleting**

A regular user can update or delete a TodoItem only when the stored owner matches the authenticated username.

Administration users cannot create, update, or delete TodoItems.

## 8. Identify What Needs to Change

Before implementing security, examine the current code.

| File | Required Change |
| --- | --- |
| `TodoItem.java` | Add a username field for record ownership |
| `TodoItemDto.java` | Update the DTO to support the lesson's ownership model |
| `TodoItemRepository.java` | Add a query that filters TodoItems by username |
| `TodoItemDtoResource.java` | Add JWT identity, role checks, and ownership enforcement |
| `JaxRsApplication.java` | Configure JWT authentication and application roles |
| `TodoItemDtoHttpRequest.http` | Add bearer tokens and multi-user security tests |

Also review the project's JWT and deployment configuration using the working Lesson 29 project as a reference.

## What's Next?

In **README 2 — Implement Multi-Tenant Security**, you will add JWT authentication, role-based permissions, and record ownership to the existing TodoItem REST API.

In **README 3 — Test Multi-Tenant Security**, you will use multiple Keycloak accounts to verify allowed operations and confirm that users cannot access or modify another user's records.
