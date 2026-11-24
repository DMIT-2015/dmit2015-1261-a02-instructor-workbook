# Lesson 31 — Secure Faces REST Client with JWT

## README 3 — Test the Secure Faces REST Client

### Learning Objectives

In this README, we will verify:

- Authentication using Keycloak.
- Protected Jakarta Faces pages.
- Automatic JWT authorization headers.
- Role-based access control.
- Multi-tenant record ownership.
- Authentication and authorization error handling.
- Access token refresh.
- Logout and session invalidation.

## 1. Start the Required Applications

Before testing the Faces application, make sure the following applications are running.

| Application | Port |
|---|---:|
| Keycloak | 8180 |
| Secure RBAC REST API | 8090 |
| Secure Multi-Tenant REST API | 8091 |
| Secure Faces REST Client | 8080 |

### 1.1 Start Keycloak

Open the Keycloak Admin Console:

```text
http://localhost:8180
```

Verify that your existing realm, client, users and roles are available.

We will reuse the users created in Lesson 29 with the following roles:

- Sales
- Shipping
- Administration

### 1.2 Start the Secure RBAC REST API

Open the following project:

```text
dmit2015-secure-rbac-restapi
```

Start the application:

```bash
mvn wildfly:dev
```

The Student REST API endpoint is:

```text
http://localhost:8090/restapi/students
```

### 1.3 Start the Secure Multi-Tenant REST API

Open the following project:

```text
dmit2015-secure-multitenant-restapi
```

Start the application:

```bash
mvn wildfly:dev
```

The TodoItem REST API endpoint is:

```text
http://localhost:8091/restapi/todos
```

Keep both REST API applications running throughout this lesson.

### 1.4 Start the Secure Faces REST Client

Open:

```text
dmit2015-secure-restclient
```

Compile the application:

```bash
mvn clean compile
```

If the compilation is successful, start the application:

```bash
mvn wildfly:dev
```

Open the Faces application:

```text
http://localhost:8080
```

## 2. Test Unauthenticated Access

Before logging in, verify that the application protects the Student and TodoItem management pages.

### Test the Student Page

1. Open the Faces application.
2. Click the Student REST Client menu item.
3. Verify that you are redirected to the login page.

### Test the TodoItem Page

1. Click the Faces TodoItem REST Client menu item.
2. Verify that you are redirected to the login page.

### Expected Results

- The Login menu item is displayed.
- The Logout menu item is not displayed.
- The protected management pages cannot be accessed without authentication.

This verifies the functionality implemented in:

```java
LoginSession.checkForToken()
```

And the Faces metadata:

```xml
<f:viewAction
        action="#{loginSession.checkForToken}"
        onPostback="true"/>
```

## 3. Test Invalid Login Credentials

On the login page, enter a valid username with an incorrect password.

Click Login.

### Expected Results

The application displays an error message:

```text
Login failed. Verify your credentials and Keycloak configuration.
```

The user remains on the login page.

No authenticated session is established.

## 4. Test Successful Authentication

Log in using the Sales user created in Lesson 29.

Enter the username and password configured in Keycloak.

Click Login.

### Expected Results

- Authentication succeeds.
- The user is redirected to the home page.
- The Login menu item is hidden.
- The Logout menu item displays the username.
- The user can navigate to the protected management pages.

### Understanding What Happened

```text
Login Page
    |
    v
KeycloakLogin
    |
    v
KeycloakLoginMpRestClient
    |
    v
Keycloak
    |
    v
Access Token + Refresh Token
    |
    v
LoginSession
```

The access token and refresh token are now stored in the user's session.

There is no need to manually copy JWT access tokens from IntelliJ HTTP Client.

## 5. Test Role-Based Access Control

We will now verify that the Student REST API continues to enforce the role-based permissions implemented in Lesson 29.

Open the Student REST Client page.

### 5.1 Test the Sales User

While logged in as the Sales user, test the following operations:

1. View Student records.
2. Create a Student record.
3. Update a Student record.
4. Delete a Student record.

Compare the results with the role permissions implemented in Lesson 29.

### 5.2 Test the Shipping User

Log out and log in using the Shipping user.

Repeat the same four operations.

### 5.3 Test the Administration User

Log out and log in using the Administration user.

Repeat the same four operations.


### Important

The Faces application does not determine which roles can perform Student CRUD operations.

The secured Student REST API continues to enforce the authorization rules.

The Faces application automatically sends the JWT access token using:

```java
@RegisterClientHeaders(AuthHeaderFactory.class)
```

## 6. Test Multi-Tenant Access Control

We will now verify the multi-tenant authorization rules implemented in Lesson 30.

Open the Faces TodoItem REST Client page.

### 6.1 Test the Sales User

Log in using the Sales user.

Perform the following operations:

1. View the TodoItem records.
2. Create a new TodoItem.
3. Verify that the Username column displays the authenticated username.
4. Update one of your TodoItems.
5. Delete one of your TodoItems.

### Expected Results

- The user can view only their own TodoItems.
- New TodoItems are automatically assigned to the authenticated user.
- The user can update their own TodoItems.
- The user can delete their own TodoItems.

Create at least one additional TodoItem and leave it in the database for the following tests.

### 6.2 Test the Shipping User

Log out and log in using the Shipping user.

Open the TodoItem management page.

### Expected Results

The Shipping user cannot see the TodoItems belonging to the Sales user.

Create a new TodoItem.

Verify that the Username column displays the Shipping user's username.

Update one of your TodoItems and verify that the operation succeeds.

Create or retain at least one TodoItem for the Administration test.

### 6.3 Test the Administration User

Log out and log in using the Administration user.

Open the TodoItem management page.

### Expected Results

The Administration user can view TodoItems belonging to different users.

The Username column allows us to identify the owner of each record.

Attempt to perform the following operations:

1. Create a TodoItem.
2. Update a TodoItem.
3. Delete a TodoItem.

All three operations should be denied.

### Expected Multi-Tenant Permissions

| Operation | Sales | Shipping | Administration |
|---|---|---|---|
| View own TodoItems | Allowed | Allowed | Allowed |
| View other users' TodoItems | Denied | Denied | Allowed |
| Create own TodoItems | Allowed | Allowed | Denied |
| Update own TodoItems | Allowed | Allowed | Denied |
| Delete own TodoItems | Allowed | Allowed | Denied |
| Modify another user's TodoItems | Denied | Denied | Denied |

These rules are enforced by the secured TodoItem REST API, not by the Faces application.

## 7. Test HTTP 403 Forbidden

Continue using the Administration user.

Open the TodoItem management page.

Attempt to create a new TodoItem.

### Expected Results

The REST API returns:

```http
403 Forbidden
```

The Faces application displays:

```text
Access denied. You do not have permission to perform this operation.
```


This verifies the functionality implemented in:

```text
ForbiddenResponseMapper.java
```

## 8. Test Access Token Refresh

When a user successfully authenticates, Keycloak provides an access token and a refresh token.

The access token has a limited lifetime.

Our `LoginSession` automatically attempts to refresh an expired access token.

## 9. Test HTTP 401 Unauthorized

A REST API returns `401 Unauthorized` when the supplied authentication information is missing or invalid.

Our Faces application should clear the authentication information and redirect the user to the login page.

### Understanding the Difference

| HTTP Status | Meaning | Application Behavior |
|---|---|---|
| 401 Unauthorized | Authentication is missing or invalid | Redirect to login |
| 403 Forbidden | Authenticated but not permitted | Display access denied |

An expired access token does not necessarily result in a 401 response because our application attempts to refresh it before sending the REST API request.

## 12. Review the Complete Security Workflow

```text
Jakarta Faces Application
          |
          v
   Keycloak Authentication
          |
          v
       JWT Tokens
          |
          v
      LoginSession
          |
          v
    AuthHeaderFactory
          |
          v
Authorization: Bearer JWT
          |
          v
     Secured REST API
          |
          +-- Role-Based Access Control
          |
          +-- Multi-Tenant Access Control
```

Each application has a different responsibility.

| Component | Responsibility |
|---|---|
| Keycloak | Authenticate users and issue tokens |
| Faces application | Provide the user interface |
| LoginSession | Store and refresh tokens |
| AuthHeaderFactory | Include JWT access tokens in REST requests |
| Student REST API | Enforce role-based access control |
| TodoItem REST API | Enforce multi-tenant access control |

## 13. Connection to Assignment 7

In Lessons 29, 30 and 31, we implemented three related security concepts.

| Lesson | Security Concept |
|---|---|
| Lesson 29 | Role-Based Access Control |
| Lesson 30 | Multi-Tenant Access Control |
| Lesson 31 | Secure Faces REST Client with JWT |

Assignment 7 applies these same concepts to the Bill application.

Instead of Student and TodoItem records, you will work with the Bill REST APIs.

Instead of Sales, Shipping and Administration, you will use the roles specified in the assignment.

Reuse the authentication and authorization patterns demonstrated in these lessons while following the specific Assignment 7 requirements.


## Lesson 31 Complete


You are now ready to apply these concepts to Assignment 7.